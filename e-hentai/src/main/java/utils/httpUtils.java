package utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.setting.dialect.Props;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;

import javax.net.ssl.SSLContext;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description:
 * @author: Andy
 * @time: 2023-9-4 16:07
 */

public class httpUtils {
    private static HttpClientBuilder httpClientBuilder = null;
    private static PoolingHttpClientConnectionManager pool;

    static {
        try {
            System.setProperty("java.net.useSystemProxies", "true");
            //证书全部信任 不做身份鉴定
            SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
            SSLContext sslContext = sslContextBuilder.loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();

            //使用谷歌浏览器查看网页使用的是哪一个SSL协议，SSLv2Hello需要删掉，不然会报握手失败，具体原因还不知道
            SSLConnectionSocketFactory sslConnectionSocketFactory =
                    new SSLConnectionSocketFactory(sslContext,
                            new String[]{"SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"}, null,
                            NoopHostnameVerifier.INSTANCE);

            // SSLConnectionSocketFactory  sslsf = new SSLConnectionSocketFactory(SSLContext.getDefault());
            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("https", sslConnectionSocketFactory)
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .build();
            //连接池
            pool = new PoolingHttpClientConnectionManager(registry);
            pool.setMaxTotal(32);
            pool.setDefaultMaxPerRoute(32);
            //连接参数
            RequestConfig requestConfig = RequestConfig.custom()
                    .setResponseTimeout(10, TimeUnit.SECONDS)
                    .setConnectTimeout(5, TimeUnit.SECONDS)
                    .setConnectionRequestTimeout(10, TimeUnit.SECONDS)
                    .build();

            httpClientBuilder = HttpClients.custom();
            httpClientBuilder
                    .setConnectionManager(pool);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CloseableHttpClient httpclient;

    public httpUtils(List<Header> headers) {
        httpclient = httpClientBuilder.setDefaultHeaders(headers)
                .setProxy(new HttpHost("localhost", 7890))
                .build();
    }

    public static String getStr(String key) {
        /*String property = System.getProperty("user.dir");
        cn.hutool.setting.dialect.Props props = new cn.hutool.setting.dialect.Props(property + "\\Settings
        .properties");*/

        String path = Props.class.getClassLoader().getResource("Settings.properties").getPath();
        Props props = new Props(path);
        return props.getStr(key);
    }

    public static void in_file(String cookie) {
        /*String Path = System.getProperty("user.dir")+ "\\Settings.properties";
        cn.hutool.setting.dialect.Props props = new cn.hutool.setting.dialect.Props(Path);*/

        String path = Props.class.getClassLoader().getResource("Settings.properties").getPath();
        Props props = new Props(path);

        Properties properties = props.toProperties();
        properties.setProperty("cookie", cookie);
        try {
            properties.store(new FileOutputStream(path), null); // 【存储的是汉字的unicode编码】
            //properties.store(new FileOutputStream(Path), null); // 【存储的是汉字的unicode编码】
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String regexStr(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        StringBuilder z = new StringBuilder();
        int i = 0;
        while (matcher.find()) {
            if (!matcher.group().equals("")) {
                i++;
                if (i == 1) {
                    z.append(matcher.group());
                }
            }
        }
        return z.toString();
    }

    public static List<List<String>> JsValue(String js, String args) {
        args = "(var " + args + " = \\[\\[)";
        String arr = regexStr(js, args + ".*;").replaceAll(args, "").replaceAll("\\]\\];", "").replaceAll("'", "");
        List<List<String>> lists = new LinkedList<>();
        String[] split = arr.split("\\],\\[");
        for (int i = 0; i < 6; i++) {
            List<String> list = new ArrayList<>();
            for (String s1 : split[i].split(",")) {
                list.add(s1);

            }
            lists.add(list);
        }
        return lists;
    }

    public void download(String url, String path, String... start_end) throws IOException {
        HttpGet httpGet = new HttpGet(url);

        String[] clone = start_end.clone();
        if (clone.length == 2) {
            String x = clone[0];
            String y = clone[1];
            if (y.equals("0")) {
                y = "";
            }
            String contentRange =
                    "bytes=" + x + "-" + y;
            System.out.println(contentRange);
            httpGet.setHeader("Range", contentRange);
        }
        CloseableHttpResponse response = null;

        response = httpclient.execute(httpGet);

        InputStream content = response.getEntity().getContent();
        BufferedInputStream bis = new BufferedInputStream(content);
        BufferedOutputStream outputStream = FileUtil.getOutputStream(path);
        byte[] bytes = new byte[1024];
        int len = -1;
        while ((len = bis.read(bytes)) != -1) {
            outputStream.write(bytes, 0, len);
        }
        outputStream.close();
        bis.close();
        content.close();

    }

    public String get(String url) {
        String html = "";
        HttpGet httpGet = new HttpGet(url);

        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            html = EntityUtils.toString(entity);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return html;
    }

    public Map<String, String> getfileName(String url) {
        Map<String, String> hashMap = new HashMap<String, String>();
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httpGet);
            String Length = response.getHeaders("Content-Length")[0].getValue();
            String fileName = null;
            String Type = null;
            Header[] Content_Disposition = response.getHeaders("Content-Disposition");
            httpGet.abort();
            if (Content_Disposition.length != 0) {
                String Name = new String(Content_Disposition[0].getValue().getBytes("ISO-8859-1"), "utf8");
                Pattern p = Pattern.compile("(?<=\").*?(?=\")");
                Matcher m = p.matcher(Name);
                StringBuilder s = new StringBuilder();
                while (m.find()) {
                    s.append(m.group());
                }
                String[] split = s.toString().split("\\.");
                fileName = split[0];
                Type = split[1];
            } else {
                String[] split = url.split("/");
                String name = split[split.length - 1];
                if (name.contains("?")) {
                    String[] split1 = name.split("\\?");
                    fileName = split1[0];
                    String[] split2 = split1[split1.length - 1].split("\\.");
                    Type = split2[split2.length - 1];
                } else {
                    String[] split1 = name.split("\\.");
                    Type = split1[split1.length - 1];
                    fileName = name.replaceAll("." + Type, "");
                }
            }
            hashMap.put("fileName", fileName);
            hashMap.put("Type", Type);
            hashMap.put("Length", Length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hashMap;
    }

    public String[] splitFileLength(long fileSize, int number) {
        long l = fileSize / number;
        String[] Strings = new String[number];
        for (int i = 1; i <= number; i++) {
            long x = (i - 1) * l;
            long y;
            if (i == number) {
                y = 0;
            } else {
                y = x + l;
            }

            if (x != 0) {
                x++;
            }
            Strings[i - 1] = x + "-" + y;
        }
        return Strings;
    }

    public void fileMerge(String inputPath, String filetempPath, String name, String type, String fileLength,
                          int fileNumber) {
        RandomAccessFile write = null;
        try {
            byte[] bytes = new byte[1024 * 1024];
            write = new RandomAccessFile(inputPath + "\\" + name + "." + type, "rw");
            for (int i = 1; i <= fileNumber; i++) {
                String filepath = filetempPath + name + "_" + i + ".temp";
                RandomAccessFile read = new RandomAccessFile(filepath, "r");
                int len = -1;
                while ((len = read.read(bytes)) != -1) {
                    write.write(bytes, 0, len);
                }
                read.close();
                FileUtil.del(filepath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert write != null;
                write.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
