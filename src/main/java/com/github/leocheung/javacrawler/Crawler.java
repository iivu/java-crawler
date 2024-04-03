package com.github.leocheung.javacrawler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;

public class Crawler implements Runnable {
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0";
    private static final String HOME_PAGE_LINK = "https://sina.cn";
    private final CrawlerDAO DAO;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public Crawler(CrawlerDAO dao) {
        this.DAO = dao;
    }

    public void run() {
        String link;
        try {
            this.DAO.connect();
            while ((link = DAO.getAndDeleteLinkFromDatabase()) != null) {
                if (DAO.isLinkProcessed(link)) {
                    continue;
                }
                if (isInterestingLink(link)) {
                    System.out.println("processing link: " + link);
                    Document doc = getAndParseHtml(link);
                    collectPageLink(doc);
                    storeArticle(doc, link);
                    DAO.insertLinkToAlreadyProcessed(link);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void collectPageLink(Document doc) throws SQLException {
        List<Element> aTags = doc.select("a");
        for (Element aTag : aTags) {
            String href = aTag.attr("href").trim();
            if (
                    href.isEmpty()
                            || href.toLowerCase().startsWith("javascript")
                            || href.startsWith("#")
            ) {
                continue;
            }
            if (href.startsWith("//")) {
                href = "https:" + href;
            }
            // TODO: 使用事务批量提交
            DAO.insertLinkToBeProcessed(href);
        }
    }

    private void storeArticle(Document doc, String link) throws SQLException {
        List<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTag.select(".art_tit_h1").text();
                List<Element> artPs = articleTag.select(".art_p");
                String content = artPs.stream().map(Element::text).collect(Collectors.joining("\n"));
                DAO.insertNews(title, content, link);
                System.out.println(title);
            }
        }
    }

    private Document getAndParseHtml(String link) throws IOException {
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

    private boolean isInterestingLink(String link) {
        return (isHomePage(link) || isNewsPage(link)) && !isLoginPage(link);
    }

    private boolean isHomePage(String link) {
        return HOME_PAGE_LINK.equals(link);
    }

    private boolean isLoginPage(String link) {
        return link.contains("passport.sina.cn");
    }

    private boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }
}
