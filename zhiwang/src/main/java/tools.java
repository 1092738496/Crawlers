import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.SelectOption;
import dao.pressdao;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pojo.press;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @time: 2024/1/23 15:37
 * @description:
 */

public class tools {
    public void getPageurl() {
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream("mybatis-config.xml");

        } catch (IOException e) {
            e.printStackTrace();
        }
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        pressdao pressdao = sqlSession.getMapper(pressdao.class);

        //
        // String stealth = this.getClass().getClassLoader().getResource("stealth.min.js").getPath();
        Playwright playwright = Playwright.create();
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(false);
        Browser browser = playwright.chromium().connectOverCDP("http://localhost:9222/");
        BrowserContext Context = browser.contexts().get(0);

        Page page = Context.pages().get(0);
        //chrome.exe --remote-debugging-port=9222
        page.navigate("https://login.cnki.net/login/");
        page.waitForLoadState();
        if (!page.url().equals("https://www.cnki.net/")) {
            page.evaluate("document.querySelector(\"#TextBoxUserName\").value='13277310220'");
            page.evaluate("document.querySelector(\"#TextBoxPwd\").value='guanzizai3431'");
            page.evaluate("document.querySelector(\"#agreement\").click();");
            page.evaluate("document.querySelector(\"#Button1\").click();");
        }


        page.navigate("https://kns.cnki.net/knavi/newspapers/SJBD/detail?uniplatform=NZKPT");
        //    Context.addInitScript(stealth);
        page.locator("#yearlist").selectOption(new SelectOption().setLabel("2025年"));

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<ElementHandle> elements = page.querySelectorAll("#jcDate > div > li");
        //月份
        for (int i = 0; i <= elements.size(); i++) {
            String h1 = page.evaluate("document.querySelector(\"#jcDate > div > li:nth-child(" + (i + 1) + ") > h1\")" +
                    ".textContent").toString().replaceAll("月", "");
            System.out.println("h1:" + h1);
            //String h1 = elements.get(i).querySelector("h1").innerText().replaceAll("月", "");
            if (Integer.parseInt(h1) >= 0) {
                //elements.get(i).click();
                List<ElementHandle> dds = elements.get(i).querySelectorAll("dl > dd");
                //天数
                for (int j = 6; j < dds.size(); j++) {
                    // dds.get(j).click();
                    page.evaluate("document.querySelector(\"#jcDate > div > li:nth-child(" + (i + 1) + ") > dl > " +
                            "dd:nth-child" +
                            "(" + (j + 1) + ") > " +
                            "a\").click();");
                    //  String date = dds.get(j).innerText();
                    String date = page.evaluate("document.querySelector(\"#jcDate > div > li:nth-child(" + (i + 1) +
                            ") " +
                            "> dl > " +
                            "dd:nth-child" +
                            "(" + (j + 1) + ") > " +
                            "a\").text").toString();
                    System.out.println(date);
                    page.locator("#articleData > table > tbody > tr:nth-child(1) > td.name").waitFor();
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    int i1 = Integer.parseInt(page.querySelector("#partiallistcurrent").innerText());
                    int i2 = Integer.parseInt(page.querySelector("#partiallistcount2").innerText());


                    for (; i1 <= i2; i1++) {
                        List<ElementHandle> element = page.querySelectorAll("#articleData > table > tbody > tr");
                        //每条
                        for (ElementHandle el : element) {
                            press press = new press();
                            press.setDate(date);
                            String title = el.querySelector("td.name").innerText();
                            String url = el.querySelector("td.name > a").getAttribute("href");
                            String author = el.querySelector("td:nth-child(4)").innerText();
                            String version = el.querySelector("td:nth-child(5)").innerText();
                            String column = el.querySelector("td:nth-child(6)").innerText();
                            System.out.println(title);
                            press.setTitle(title);
                            press.setUrl(url);
                            press.setAuthor(author);
                            press.setVersion(version);
                            press.setColumn(column);
                            Page page1 = Context.newPage();
                            page1.navigate(url);
                            try {
                                page1.locator("#DownLoadParts > div > ul > div.download-btns > li.btn-html > a").waitFor();
                            } catch (Exception e) {
                                page1.close();
                            }
                            //#DownLoadParts > div > ul > div.download-btns > li.btn-html > a
                            page1.querySelector("#DownLoadParts > div > ul > div.download-btns > li.btn-html > a").click();

                            Page popup = page1.waitForPopup(() -> {

                            });
                            if (popup.title().equals("拼图校验-中国知网")) {
                                popup.waitForLoadState();
                                try {
                                    popup.locator("#download_input1").waitFor();
                                } catch (Exception e) {
                                }


                            }

                            try {
                                popup.locator("#download_input1").waitFor();
                                popup.querySelector("#download_input1").click();
                            } catch (Exception e) {
                            }
                            popup.waitForLoadState();


                       /* String s = popup.querySelector("#mainBody > div.main > div.content > div" +
                                ".top-title > p").innerText();
                        press.setSubtitle1(s);*/
                            popup.locator("#paperRead > div.js-studyAchievement > div:nth-child(1) > div.ChapterWrap" +
                                    ".ChapterH1 > h1").waitFor();
                            popup.locator("#paperRead > div.word-end > p.word-end-t").waitFor();

                            ElementHandle titleelement = popup.querySelector("#paperRead > div.js-studyAchievement > " +
                                    "div:nth-child" +
                                    "(1) " +
                                    "> div" +
                                    ".ChapterWrap.ChapterH1 > div");
                            if (titleelement != null) {
                                String title1 = popup.querySelector("#paperRead > div.js-studyAchievement > " +
                                        "div:nth-child" +
                                        "(1) " +
                                        "> div" +
                                        ".ChapterWrap.ChapterH1 > div").innerText();
                                press.setTitle1(title1);
                            }
                            List<ElementHandle> elementX = popup.querySelectorAll("#paperRead > div" +
                                    ".js-studyAchievement >" +
                                    " div:nth-child(3) > div > *");
                            //#paperRead > div.js-studyAchievement > div:nth-child(3) > div
                            String text = "";
                            for (int h = 0; h < elementX.size(); h++) {
                                ElementHandle elementHandle = elementX.get(h);
                                String style = elementHandle.getAttribute("style");

                                if (style == null) {
                                    Elements selects = Jsoup.parse(elementHandle.innerHTML()).select("body > *");
                                    for (Element select : selects) {
                                        String text1 = select.ownText();
                                        //System.out.println(text1);
                                       /* StringBuilder textx = new StringBuilder(text1);
                                        if (textx.charAt(select.text().length() - 1) == '0') {
                                            text1 = textx.deleteCharAt(select.text().length() - 1).toString();
                                        }*/
                                        if (select.is("P")) {
                                            text += text1 + "\n";
                                        } else {
                                            text += "\n" + text1 + "\n";
                                        }
                                    }
                                }
                            }
                            //  System.out.println(text);
                            press.setText(text);
                            popup.close();
                            page1.close();
                            pressdao.insert(press);
                            //System.out.println(press);

                        }
                        page.querySelector("#rightCatalog > div.optsbox.clearfix.opts-col > div.resultInfo.fr > a" +
                                ".page-next").click();
                        try {
                            TimeUnit.SECONDS.sleep(2);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    sqlSession.commit();

                    //  page.locator("#partiallistcount2").waitFor();
                  /*  Integer integer = Integer.valueOf(page.locator("#partiallistcount2").innerText());
                    for (int z = 1; z < integer; z++) {
                        page.evaluate("document.querySelector(\"#rightCatalog > div.listbox.listbox-col > div.pagebox" +
                                " > a.next\").click()");
                        *//*page.locator("#rightCatalog > div.optsbox.clearfix.opts-col > div.resultInfo.fr > a" +
                                ".page-next").click();*//*
                        page.locator("#articleData > table > tbody > tr:nth-child(1) > td.name").waitFor();
                        try {
                            TimeUnit.SECONDS.sleep(2);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        List<ElementHandle> element2 = page.querySelectorAll("#articleData > table > tbody > tr");
                        for (ElementHandle el : element2) {
                            press press = new press();
                            press.setDate(date);
                            String title = el.querySelector("td.name").innerText();
                            String url = el.querySelector("td.name > a").getAttribute("href");
                            String author = el.querySelector("td:nth-child(4)").innerText();
                            press.setTitle(title);
                            press.setUrl(url);
                            press.setAuthor(author);

                            Page page1 = Context.newPage();
                            page1.navigate(url);
                            try {
                                page1.locator("#DownLoadParts > div.operate-left > ul > li.btn-html > a").waitFor();
                            } catch (Exception e) {
                                page1.close();
                            }
                            page1.querySelector("#DownLoadParts > div.operate-left > ul > li.btn-html > a").click();

                            Page popup = page1.waitForPopup(() -> {

                            });
                            popup.waitForLoadState();

                            *//*String s = popup.querySelector("#mainBody > div.main > div.content > div" +
                                    ".top-title > p").innerText();
                            press.setSubtitle1(s);*//*


                            List<ElementHandle> elementX = popup.querySelectorAll("#paperRead > div" +
                                    ".js-studyAchievement > div:nth-child(3) > *");
                            String text = "";
                            for (int h = 3; h < elementX.size()-1; h++) {
                                text+=elementX.get(h).innerText();
                            }
                            press.setText(text);
                            popup.close();
                            page1.close();
                            pressdao.insert(press);
                             System.out.println(press);
                            System.out.println();

                        }
                       *//* page.evaluate("document.querySelector(\"#jcDate > div > li:nth-child("+i+") > dl >
                       dd:nth-child" +
                                "("+j+") > " +
                                "a\").click()");*//*
                    }*/

                }

            }
        }
    }


}
