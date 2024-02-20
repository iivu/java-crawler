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
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0";
    private static final String HOME_PAGE_LINK = "https://sina.cn";
    private static final String JDBC_URL = "jdbc:h2:file:/Users/naliankeji/leo-space/hcsp/java-crawler/db/sina_news";
    private static final String DATABASE_USER = "root";
    private static final String DATABASE_PASSWORD = "root";
    private static List<String> linkPool;

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        Connection connection = DriverManager.getConnection(JDBC_URL, DATABASE_USER, DATABASE_PASSWORD);
        linkPool = loadLinksFromDatabase(connection, "SELECT `link` FROM `links_to_be_processed`;");
        if (linkPool.isEmpty()) {
            linkPool.add(HOME_PAGE_LINK);
        }
        while (!linkPool.isEmpty()) {
            String link = linkPool.remove(linkPool.size() - 1);
            insertLinkIntoDatabase(connection, link, "DELETE FROM `links_to_be_processed` WHERE `link` = ?;");
            if (isLinkProcessed(connection, link)) {
                continue;
            }
            if (isInterestingLink(link)) {
                Document doc = getAndParseHtml(link);
                collectPageLink(connection, doc);
                storeArticle(connection, doc);
                insertLinkIntoDatabase(connection, link, "INSERT INTO `links_already_processed` (`link`) VALUES (?);");
            }
        }
    }

    private static void collectPageLink(Connection connection, Document doc) throws SQLException {
        List<Element> aTags = doc.select("a");
        for (Element aTag : aTags) {
            String href = aTag.attr("href");
            linkPool.add(href);
            // TODO: 使用事务批量提交
            insertLinkIntoDatabase(connection, href, "INSERT INTO `links_to_be_processed` (`link`) VALUES (?);");
        }
    }

    private static void storeArticle(Connection connection, Document doc) {
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

    private static List<String> loadLinksFromDatabase(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            List<String> result = new ArrayList<>();
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                result.add(resultSet.getString(1));
            }
            return result;
        }
    }

    private static void insertLinkIntoDatabase(Connection connection, String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    private static boolean isLinkProcessed(Connection connection, String link) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM `links_already_processed` WHERE `link` = ?;")) {
            statement.setString(1, link);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        }
    }
}
