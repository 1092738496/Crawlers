package com.meditation.utils;

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
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;

import javax.net.ssl.SSLContext;
import java.io.IOException;
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

public class httpUtils2 {
    private static HttpClientBuilder httpClientBuilder = null;
    private static PoolingHttpClientConnectionManager pool;

    static {
        try {
            //System.setProperty("javax.net.debug", "all");
            //证书全部信任 不做身份鉴定
            StringBuilder stringBuffer = new StringBuilder();

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
            HttpHost host = new HttpHost("127.0.0.1", 7890);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setResponseTimeout(10, TimeUnit.SECONDS)
                    .setConnectTimeout(5, TimeUnit.SECONDS)
                    .setConnectionRequestTimeout(10, TimeUnit.SECONDS)
                    .setProxy(host)
                    .build();
            httpClientBuilder = HttpClients.custom();
            httpClientBuilder
                    .setConnectionManager(pool)
                    .setProxy(host)
                    .setDefaultRequestConfig(requestConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CloseableHttpClient httpclient;

    public httpUtils2() {
    }


    public httpUtils2(List<Header> headers) {
        httpclient = httpClientBuilder.setDefaultHeaders(headers).build();
    }

    public void setHeaders(List<Header> headers) {
        httpclient = httpClientBuilder.setDefaultHeaders(headers).build();
    }


    public String get(String url, String Charset) throws IOException, ParseException {
        String html = "";
        HttpGet httpGet = new HttpGet(url);

        CloseableHttpResponse response = httpclient.execute(httpGet);
        html = EntityUtils.toString(response.getEntity(), "utf-8");

   /*     // 读取整个响应体
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
        Charset))) {
            StringBuilder responseBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBody.append(line);
            }
            html = responseBody.toString();
        }*/

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


}
