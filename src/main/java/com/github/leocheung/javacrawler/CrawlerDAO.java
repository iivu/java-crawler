package com.github.leocheung.javacrawler;

import java.sql.SQLException;

public interface CrawlerDAO {
    String getAndDeleteLinkFromDatabase() throws SQLException;

    void insertNews(String title, String content, String link) throws SQLException;

    boolean isLinkProcessed(String link) throws SQLException;

    void insertLinkToAlreadyProcessed(String link) throws SQLException;

    void insertLinkToBeProcessed(String link) throws SQLException;

    void connect() throws Exception;
}
