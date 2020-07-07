package com.intgroup.htmlcheck.service.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class LoadHtmlPageService {
    public Document load(String url) throws IOException {
        return Jsoup
                .connect(url)
                .timeout(15000)
                .ignoreContentType(true)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("https://google.com.ru").get();
    }

    public boolean saveURLToFile(String url, File file) {
        try ( FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(getUrlAsBytes(url));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public byte[] getUrlAsBytes(String url) {
        try {
            return Jsoup
                    .connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 6.2; Win64; x64)")
                    .header("Accept-Language", "en-US")
                    .header("Accept-Encoding", "gzip,deflate,sdch")
                    .ignoreContentType(true)
                    .execute()
                    .bodyAsBytes();
        } catch (Exception e) {
            return null;
        }
    }

    public String selectFirstOrNull(Document document, String selector) {
        Element element = document.selectFirst(selector);

        if (element == null) {
            return null;
        }

        return element.text();
    }

    public static void main(String[] args) throws IOException {
        String url = "https://play.google.com/store/apps/details?id=biznes.soft.zvit";
//        String url = "https://image.winudf.com/v2/image1/anAubmF2ZXIubGluZS5hbmRyb2lkX2ljb25fMTU0NTA0MjcwNV8wNzY/icon.png?w=170&fakeurl=1&type=.png";
       // boolean result = new LoadHtmlPageService().saveURLToFile(url, new File("/home/integer/test.png"));
       // System.out.println(result);
//        System.out.println(doc.html());

        System.out.println(new LoadHtmlPageService().load(url));
    }
}
