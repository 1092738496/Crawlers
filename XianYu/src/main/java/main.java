import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import utils.httpUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * @time: 2024/10/30 21:24
 * @description:
 */

public class main {


    public static void main(String[] args) throws UnsupportedEncodingException, URISyntaxException,
            NoSuchAlgorithmException, JsonProcessingException, InterruptedException {
        List<Header> headers = new ArrayList<>();
        String Cookie = "t=1d7c57ad5ab1ff5591169aac262b0ba5; " +
                "tracknick=%E4%B9%B0%E8%A1%A3%E6%9C%8Dm%E8%A3%A4%E5%AD%9028%E9%9E" +
                "%8B%E5%AD%9040; mtop_partitioned_detect=1; _m_h5_tk=41b46f631de4d04e184568ceb1d013b6_1733833275709; " +
                "_m_h5_tk_enc=53cdbc13e0240c1790ec8a190a71c98c; cookie2=1d2653d9dad0c8d7f4a144b7a5875582; " +
                "_samesite_flag_=true; _tb_token_=ea630bb37d748; unb=2456556960; " +
                "sgcookie=E100vcIxphEfCB6JMsUQb4%2FA0AQtc%2FR%2BcJcukv9ivn9FhCfMKdcqzN8XuYC8LzHlOzo4Hyue4CdD" +
                "%2BPDMPRiKG3Ol4WtUWUE8fKbC%2FUNDm%2Fl%2F8A0%3D; csg=a5147ada; " +
                "havana_lgc2_77" +
                "=eyJoaWQiOjI0NTY1NTY5NjAsInNnIjoiZDg1MzZmYzRhMDEzODQ3OGM1MzhjODk5NWE2MzE0MTQiLCJzaXRlIjo3NywidG9rZW4iOiIxR1JRNUx4aXBpcUNjamZjTXhRMHhPdyJ9; _hvn_lgc_=77; havana_lgc_exp=1736414851441; isg=BHp6l2nTya2pEEW7mF75dXXny6CcK_4FiGeaboRzXo3YdxuxbLtvFVDMxwOrZ3ad; sdkSilent=1733909253596";
        String[] s = Cookie.split(" ");
        headers.add(new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"));
        headers.add(new BasicHeader("Referer", "https://www.goofish.com/"));
        headers.add(new BasicHeader("Accept", "application/json"));
        headers.add(new BasicHeader("Accept-Language", "zh-CN,zh;q=0.9"));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("Sec-Ch-Ua", "\"Google Chrome\";v=\"123\", \"Not:A-Brand\";v=\"8\", \"Chromium\";" +
                "v=\"123\""));
        headers.add(new BasicHeader("Accept-Encoding", "gzip, deflate, br, zstd"));
        headers.add(new BasicHeader("Origin", "https://www.goofish.com"));
        headers.add(new BasicHeader("Accept", "application/json"));
        headers.add(new BasicHeader("Cookie", Cookie));
        httpUtils httpUtils = new httpUtils(headers);
        String h = "34839810";
        String token = "";
        for (String s1 : s) {
            if (s1.indexOf("_m_h5_tk") != -1) {
                token = s1.split("=")[1].split("_")[0];
                break;
            }
        }
        System.out.println("token:" + token);

        String ijs = "";
        try (InputStream inputStream = main.class.getClassLoader().getResourceAsStream("i.js");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            if (inputStream == null) {
                System.out.println("File not found in resources folder.");
                return;
            }
            // 读取文件内容并打印
            String line;
            while ((line = reader.readLine()) != null) {
                ijs += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 50; i++) {
            String j = String.valueOf(System.currentTimeMillis());
            //  System.out.println("时间戳:" + j);

            String data = "{\"pageNumber\":" + i + ",\"keyword\":\"钓鱼竿\",\"fromFilter\":false," +
                    "\"rowsPerPage\":30," +
                    "\"sortValue\":\"\",\"sortField\":\"\",\"customDistance\":\"\",\"gps\":\"\",\"propValueStr\":{}," +
                    "\"customGps\":\"\",\"searchReqFromPage\":\"pcSearch\"}";
            String k = "'" + token + "&" + j + "&" + h + "&" + data + "'";
            //System.out.println("k:" + k);

            String sign = md5xx(ijs, k);


            //System.out.println("sign:" + sign);
            List<NameValuePair> formParams = new ArrayList<>();
            //String encode = URLEncoder.encode(data, StandardCharsets.UTF_8.toString());
            formParams.add(new BasicNameValuePair("data", data));

            URI uri = new URIBuilder("https://h5api.m.goofish.com/h5/mtop.taobao.idlemtopsearch.pc.search/1.0/")
                    .addParameter("jsv", "2.7.2")
                    .addParameter("appKey", "34839810")
                    .addParameter("t", j)
                    .addParameter("sign", sign)
                    .addParameter("v", "1.0")
                    .addParameter("type", "originaljson")
                    .addParameter("accountSite", "xianyu")
                    .addParameter("dataType", "json")
                    .addParameter("timeout", "20000")
                    .addParameter("api", "mtop.taobao.idlemtopsearch.pc.search")
                    .addParameter("sessionOption", "AutoLoginOnly")
                    .addParameter("spm_cnt", "a21ybx.search.0.0")
                    .addParameter("spm_pre", "a21ybx.search.searchInput.0")
                    .build();
            String post = httpUtils.post(uri.toString(), formParams);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readValue(post, JsonNode.class);
            JsonNode data1 = jsonNode.get("data").get("resultList");
            if (jsonNode.get("ret").size() == 2) {
                System.out.println(post);
                return;
            }
            if (jsonNode.get("data").size() == 0) {
                System.out.println(post);
                return;
            }

            for (JsonNode node : data1) {
                System.out.println("---------------------------------------------------------------");
                JsonNode data2 = node.get("data");
                String title = data2.get("title").textValue();
                String description = data2.get("description").textValue();
                String firstModified = data2.get("firstModified").textValue();
                String price = data2.get("price").textValue();
                String city = data2.get("city").textValue();
                String area = data2.get("area").textValue();
                String detailFrom = data2.get("detailFrom").textValue();
                if (Double.parseDouble(price) < 50) {
                    System.out.println(title);
                    System.out.println(description);
                    System.out.println(firstModified);
                    System.out.println(price);

                }
            }
        }
    }

    public static String md5xx(String js, String k) {
        String sign = "";
        try {
            // 创建 ProcessBuilder 实例，运行 node 进程
            ProcessBuilder pb = new ProcessBuilder("node");
            Process process = pb.start();

            // 获取输出流，用于发送 JavaScript 代码
            OutputStream outputStream = process.getOutputStream();

            // JavaScript 代码
            String jsCode = js;
            jsCode += "console.log(i(" + k + "));";
            // 向 Node.js 进程写入 JavaScript 代码
            outputStream.write((jsCode).getBytes());
            outputStream.flush();


            outputStream.close(); // 关闭输出流，结束输入
            String output = "";
            // 读取输出流
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                output += (line) + "\n";
            }
            // 等待进程结束
            process.waitFor();
            process.destroy();
            reader.close();
            // 输出 i 函数的返回值
            sign = output.trim();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sign;
    }

}
