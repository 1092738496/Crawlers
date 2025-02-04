import cn.hutool.core.io.file.FileWriter;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.message.BasicHeader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @time: 2024/5/2 18:53
 * @description:
 */

public class test {


    public static void pageclient(Page page, String url) {
        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"));
        headers.add(new BasicHeader("Cookie", "\n" +
                "__ddg1_=1XgekJcC77t5Fq0DEXdj; _ga=GA1.1.211615917.1736834069; " +
                "cf_clearance=MqXjiuwbzIDKxNXtTFPpP7VH1g2k7u8JrEqUWc0lGKs-1736838172-1.2.1" +
                ".1-QqsDgcsUf1phmDz6zgjzuP0ag1l2AQneecGTTc83trOJHdZXdz881DO8Czb_XeZa2pXDBr6evSBxndyaTlNBBi5ofI0GIue2lZq.CFvzah08ez4QIQx66ksyagLYAqBpD1n4f6JDuPr_hyeMSDXzFFS3kq7WSE1HTY9wsYNrdi8_bJav3VTaVrEfaGMz2FGGY79ApGqKTZ_vph55GM1q0KvSNX89BmxFvn_PDWABvB41spSHU1fnLhgDeqnLWkUVSUmmyQiOVRwqQTHWX5V8ZMX13ATRaxNsR138ZMlpjFUXWXUfF3VhLiueynTvL8AjkxCimBuRp8E8sadNMkR5rg; _ga_3QEFGLT467=GS1.1.1736834069.1.1.1736838061.0.0.0; __ddg8_=XxDScfig2VvBFZfP; __ddg9_=162.158.179.35; __ddg10_=1736838184"));

        httpUtils httpUtils = new httpUtils(headers);
        page.navigate(url);
        String title = page.locator("#chapter-heading").innerText();
        new File("D:\\" + title).mkdir();
        String s = page.innerHTML("body > div.wrap > div > div.site-content > div > div > div > div > div > div > div" +
                ".c-blog-post > div.entry-content > div > div > div.reading-content");
        Elements divs = Jsoup.parse(s).select("div");
        for (int i = 0; i < divs.size(); i++) {
            Element element = divs.get(i);
            Elements img = element.select("div > img");
            System.out.println(img.attr("id"));
            System.out.println(img.attr("data-src").trim());
            String src = img.attr("data-src").trim();
            httpUtils.download(src, "D:\\" + title + "\\" + i + ".jpg");
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        }
    }

    @Test
    public void test1() throws IOException {

        File file = new File("D:\\Game\\iniRePather-Skyrim\\Mod Organizer 2\\mods\\Legacy of the Dragonborn - " +
                "Creation Club Patch Hub Simplified Chinese translation");
        File[] files = file.listFiles();
        for (File file1 : files) {
            if (file1.isFile()) {
                String aa =
                        file1.getPath().replaceAll(file1.getName(), "") + file1.getName().replaceAll(".esp", "") + 1 +
                                ".esp";


                //System.out.println(file.getAbsolutePath());
                byte[] bytes = new byte[1024];
                int p = 0;
                FileInputStream inputStream = new FileInputStream(file1.getPath());
                FileOutputStream outputStream = new FileOutputStream(aa);
                while ((p = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes);
                }

                inputStream.close();
                outputStream.close();
                file1.delete();

            }
        }
    }

    @Test
    public void test2() throws IOException, ParseException {


        Playwright playwright = Playwright.create();
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(false);
        Browser browser = playwright.chromium().connectOverCDP("http://localhost:9222");
        BrowserContext Context = browser.contexts().get(0);



        Page page = Context.pages().get(0);
        List<String> urls = new ArrayList<>();

        /*urls.add("https://allporncomic.com/porncomic/princess-and-5-goblins-jared999d/6-1-princess-and-5-goblins/");
        urls.add("https://allporncomic.com/porncomic/princess-and-5-goblins-jared999d/6-2-princess-and-5-goblins" +
                "-textless/");
        urls.add("https://allporncomic.com/porncomic/princess-and-5-goblins-jared999d/6-3-princess-and-5-goblins-alt" +
                "-version/");
        urls.add("https://allporncomic.com/porncomic/princess-and-5-goblins-jared999d/6-4-bonus/");
        urls.add("https://allporncomic.com/porncomic/princess-and-5-goblins-jared999d/7-1-princess-and-5-goblins/");
        urls.add("https://allporncomic.com/porncomic/princess-and-5-goblins-jared999d/7-2-princess-and-5-goblins" +
                "-textless/");*/
        urls.add("https://allporncomic.com/porncomic/princess-and-5-goblins-jared999d/7-3-extra/");
        urls.add("https://allporncomic.com/porncomic/princess-and-5-goblins-jared999d/8-1-princess-and-5-goblins/");
        urls.add("https://allporncomic.com/porncomic/princess-and-5-goblins-jared999d/8-2-princess-and-5-goblins" +
                "-textless/");
        urls.add("https://allporncomic.com/porncomic/princess-and-5-goblins-jared999d/8-3-bonus/");

        for (String url : urls) {
            pageclient(page, url);

        }


    }

    @Test
    public void test4() throws IOException, ParseException {
        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"));
        headers.add(new BasicHeader("Cookie", "\n" +
                "__ddg1_=1XgekJcC77t5Fq0DEXdj; _ga=GA1.1.211615917.1736834069; " +
                "cf_clearance=MqXjiuwbzIDKxNXtTFPpP7VH1g2k7u8JrEqUWc0lGKs-1736838172-1.2.1" +
                ".1-QqsDgcsUf1phmDz6zgjzuP0ag1l2AQneecGTTc83trOJHdZXdz881DO8Czb_XeZa2pXDBr6evSBxndyaTlNBBi5ofI0GIue2lZq.CFvzah08ez4QIQx66ksyagLYAqBpD1n4f6JDuPr_hyeMSDXzFFS3kq7WSE1HTY9wsYNrdi8_bJav3VTaVrEfaGMz2FGGY79ApGqKTZ_vph55GM1q0KvSNX89BmxFvn_PDWABvB41spSHU1fnLhgDeqnLWkUVSUmmyQiOVRwqQTHWX5V8ZMX13ATRaxNsR138ZMlpjFUXWXUfF3VhLiueynTvL8AjkxCimBuRp8E8sadNMkR5rg; _ga_3QEFGLT467=GS1.1.1736834069.1.1.1736838061.0.0.0; __ddg8_=XxDScfig2VvBFZfP; __ddg9_=162.158.179.35; __ddg10_=1736838184"));
        httpUtils httpUtils = new httpUtils(headers);

        Playwright playwright = Playwright.create();
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(false);
        Browser browser = playwright.chromium().connectOverCDP("http://localhost:9222");
        BrowserContext Context = browser.contexts().get(0);

        Page page = Context.pages().get(0);
        page.navigate("https://18comic.vip/photo/119616");
        String title = page.title();
        new File("D:\\" + "魅魔吸精").mkdir();
        String s = page.innerHTML("#wrapper > div:nth-child(25) > div:nth-child(3) > div > div > div.panel-body > div");
        Elements divs = Jsoup.parse(s).select("div");
        int j = 0;
        for (int i = 0; i < divs.size(); i++) {
            Element element = divs.get(i);
            String aClass = element.attr("class").trim();
            if (aClass.equals("center scramble-page spnotice_chk")) {
                String attr = element.select("img").attr("data-original");
                System.out.println(attr);
                httpUtils.download(attr, "D:\\" + "魅魔吸精" + "\\" + j + ".jpg");
                j++;
            }

        }
    }

    /**
     * @title:文本段落格式化
     * @descr:
     */
    @Test
    public void test5() {
        File file = new File("C:\\Users\\Administrator\\Desktop\\荒野求操   作者：温窈窕（1-36 番外 完）.txt");
        cn.hutool.core.io.file.FileReader fileReader = new cn.hutool.core.io.file.FileReader(file);

        String s = fileReader.readString();
        StringBuilder stringBuffer = new StringBuilder(s);
        char[] chars = s.toCharArray();
        List<String> regex = tools.regex(s, "(　*第.*章.?)|(第.+章.+)");
        List<String> titles = new ArrayList<>();
        for (int i = 0; i < regex.size(); i++) {
            String[] split = regex.get(i).split(",");
            Integer integer0 = Integer.valueOf(split[0]);
            Integer integer1 = Integer.valueOf(split[1]);
            String title = "";
            for (int i1 = integer0; i1 < integer1; i1++) {
                title += chars[i1];
            }
            titles.add(title);
            /*stringBuffer.insert(integer0-1, "\n");
            stringBuffer.insert(integer1+1, "\n");*/
        }
        for (int i = 0; i < titles.size(); i++) {
            System.out.println(titles.get(i));
            int i1 = stringBuffer.indexOf(titles.get(i));
            stringBuffer.insert(i1 - 1, "\n");
            stringBuffer.insert(i1 + titles.get(i).length() + 1, "\n");

        }
        System.out.println("```````````");


        s = stringBuffer.toString().replaceAll("((?<=[\\u4e00-\\u9fff])\\r\\n(?=[\\u4e00-\\u9fff]))|(，\\r\\n" +
                "(?=[\\u4e00-\\u9fff]))|((?<=[\\u4e00-\\u9fff])\\r\\n，)|((?<=[\\u4e00-\\u9fff])\\r\\n”)|((?<=[\\u4e00" +
                "-\\u9fff])\\r\\n (?=[\\u4e00-\\u9fff]))", "");
        FileWriter writer = new FileWriter(file);
        writer.write(s);
        //System.out.println(s);

    }

    /**
     * @title:
     * @descr:
     */
    @Test
    public void test6() {

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"));
        headers.add(new BasicHeader("Cookie", "__ddg1_=1XgekJcC77t5Fq0DEXdj; _ga=GA1.1.211615917.1736834069; " +
                "cf_clearance=MqXjiuwbzIDKxNXtTFPpP7VH1g2k7u8JrEqUWc0lGKs-1736838172-1.2.1" +
                ".1-QqsDgcsUf1phmDz6zgjzuP0ag1l2AQneecGTTc83trOJHdZXdz881DO8Czb_XeZa2pXDBr6evSBxndyaTlNBBi5ofI0GIue2lZq.CFvzah08ez4QIQx66ksyagLYAqBpD1n4f6JDuPr_hyeMSDXzFFS3kq7WSE1HTY9wsYNrdi8_bJav3VTaVrEfaGMz2FGGY79ApGqKTZ_vph55GM1q0KvSNX89BmxFvn_PDWABvB41spSHU1fnLhgDeqnLWkUVSUmmyQiOVRwqQTHWX5V8ZMX13ATRaxNsR138ZMlpjFUXWXUfF3VhLiueynTvL8AjkxCimBuRp8E8sadNMkR5rg; _ga_3QEFGLT467=GS1.1.1736834069.1.1.1736838061.0.0.0; __ddg8_=XxDScfig2VvBFZfP; __ddg9_=162.158.179.35; __ddg10_=1736838184"));
        httpUtils httpUtils = new httpUtils(headers);
        for (int i = 1; i <= 324; i++) {
            String page = "";
            if (i < 10) {
                page = "00" + i;
            } else if (i < 100) {
                page = "0" + i;
            } else {
                page = i + "";
            }

            httpUtils.download("https://img5.qy0.ru/data/2861/95/" + page + ".jpg", "E:\\全家桶23\\" + page + ".jpg");
        }

    }

    /**
     * @title:
     * @descr:
     */
    @Test
    public void test() {

    }

}
