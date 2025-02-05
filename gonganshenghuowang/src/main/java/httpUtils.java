import cn.hutool.core.io.FileUtil;
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
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description:
 * @author: Andy
 * @time: 2023-9-4 16:07
 */

public class httpUtils {
    public static HttpClientBuilder httpClientBuilder = null;

    static {
        try {
            System.setProperty("java.net.useSystemProxies", "true");
            //System.setProperty("javax.net.debug", "all");
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
            //// SSLConnectionSocketFactory  sslsf = new SSLConnectionSocketFactory(SSLContext.getDefault());
            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("https", sslConnectionSocketFactory)
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .build();
            //连接池
            PoolingHttpClientConnectionManager pool = new PoolingHttpClientConnectionManager(registry);
            pool.setMaxTotal(32);
            pool.setDefaultMaxPerRoute(32);
            //连接参数
            RequestConfig requestConfig = RequestConfig.custom()
                    .setResponseTimeout(10, TimeUnit.SECONDS)
                    .setConnectTimeout(5, TimeUnit.SECONDS)
                    .setConnectionRequestTimeout(10, TimeUnit.SECONDS)

                    .build();

            httpClientBuilder = HttpClients.custom();
            httpClientBuilder.setConnectionManager(pool);
            // .setDefaultRequestConfig(requestConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Header> headers;
    private CloseableHttpClient httpclient;

    public httpUtils() {
    }

    public httpUtils(List<Header> headers) {
        this.headers = headers;
        httpclient = httpClientBuilder.setDefaultHeaders(headers)
                .build();
    }

    public static String[] splitFileLength(long fileSize, int number) {
        String[] Strings = new String[number];
        if (number != 1) {
            long l = fileSize / number;
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
        } else {
            Strings[0] = "0-" + fileSize;
        }
        return Strings;
    }

    public static void fileMerge(String inputPath, String filetempPath, String name, String type,
                                 int fileNumber) throws IOException {

        byte[] bytes = new byte[1024 * 1024];
        RandomAccessFile write = new RandomAccessFile(inputPath + "\\" + name + "." + type, "rw");
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
        write.close();
    }

    public static String updataFileName(String fileName) {
        String pattern = "/|\\|:|\\*|\\?|\"|<|>|\\|";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(fileName);
        return m.replaceAll("").replaceAll(" ", "");
    }

    public String get(String url, List<Header>... headers) throws IOException, ParseException {
        HttpGet httpGet = new HttpGet(url);

        List<Header>[] clone = headers.clone();
        if (clone.length != 0) {
            for (Header header : clone[0]) {
                httpGet.setHeader(header);
            }
        }
        CloseableHttpResponse response = null;

        response = httpclient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String html = EntityUtils.toString(entity, "utf-8");
        response.close();
        return html;
    }

    public Map<String, String> getfileName(String url, List<Header>... headers) throws IOException {
        Map<String, String> hashMap = new HashMap<String, String>();
        HttpGet httpGet = new HttpGet(url);
        List<Header>[] clone = headers.clone();
        if (clone.length != 0) {
            for (Header header : clone[0]) {
                httpGet.setHeader(header);
            }
        }
        CloseableHttpResponse response = httpclient.execute(httpGet);
        httpGet.abort();
        String Length = response.getHeaders("Content-Length")[0].getValue();
        String fileName = null;
        String Type = null;
        System.out.println("asda");

        Header[] Content_Disposition = response.getHeaders("Content-Disposition");
        String value = null;
        if (Content_Disposition.length != 0) {
            value = Content_Disposition[0].getValue();
        }
        //获取百度云第三方链接
        if (value != null && !value.equals("attachment")) {
            String Name = new String(Content_Disposition[0].getValue().getBytes(StandardCharsets.ISO_8859_1),
                    StandardCharsets.UTF_8);
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
            //拆链接 取名字
            String[] split = url.split("/");
            String name = split[split.length - 1];
            if (name.contains("?")) {
                String[] split1 = name.split("\\?");
                String[] split2 = split1[0].split("\\.");
                Type = split2[split2.length - 1];
                fileName = split1[0].replaceAll("." + Type, "");
            } else {
                String[] split1 = name.split("\\.");
                Type = split1[split1.length - 1];
                fileName = name.replaceAll("." + Type, "");
            }
        }
        hashMap.put("Name", fileName);
        hashMap.put("Type", Type);
        hashMap.put("Length", Length);

        return hashMap;
    }

}
