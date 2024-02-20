package com.github.leocheung.javacrawler;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0";
    private static final String HOME_PAGE_LINK = "https://sina.cn";
    private static final List<String> linkPool = new ArrayList<>();
    private static final Set<String> processedLinks = new HashSet<>();

    static {
        linkPool.add(HOME_PAGE_LINK);
    }

    public static void main(String[] args) throws IOException {
        while (!linkPool.isEmpty()) {
            String link = linkPool.remove(linkPool.size() - 1);
            if (processedLinks.contains(link)) {
                continue;
            }
            if (isInterestingLink(link)) {
                Document doc = getAndParseHtml(link);
                collectPageLink(doc);
                storeArticle(doc);
                processedLinks.add(link);
            }
        }
    }

    private static void collectPageLink(Document doc) {
        List<Element> aTags = doc.select("a");
        aTags.stream().map(aTag -> aTag.attr("href")).forEach(linkPool::add);
    }

    private static void storeArticle(Document doc) {
        List<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            articleTags.forEach(articleTag -> {
                String title = articleTag.child(0).text();
                System.out.println(title);
            });
        }
    }

    private static Document getAndParseHtml(String link) throws IOException {
        if (link.startsWith("//")) {
            link = "https:" + link;
        }
        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("User-Agent", USER_AGENT);
        try (
                CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse httpResponse = httpClient.execute(httpGet)
        ) {
            HttpEntity httpEntity = httpResponse.getEntity();
            String html = EntityUtils.toString(httpEntity);
            EntityUtils.consume(httpEntity);
            return Jsoup.parse(html);
        }
    }

    private static boolean isInterestingLink(String link) {
        return (isHomePage(link) || isNewsPage(link)) && !isLoginPage(link);
    }

    private static boolean isHomePage(String link) {
        return HOME_PAGE_LINK.equals(link);
    }

    private static boolean isLoginPage(String link) {
        return link.contains("passport.sina.cn");
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }
}
