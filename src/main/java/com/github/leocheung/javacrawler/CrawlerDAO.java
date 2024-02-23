package com.github.leocheung.javacrawler;

import java.sql.SQLException;

public interface CrawlerDAO {
    String getAndDeleteLinkFromDatabase() throws SQLException;
    void updateLinkDatabase(String link, String sql) throws SQLException;
    void insertNewsIntoDataBase(String title, String content, String link) throws SQLException;
    boolean isLinkProcessed(String link) throws SQLException;
    void connect() throws SQLException;

    void insertLinkToAlreadyProcessed(String link) throws SQLException;

    void insertLinkToBeProcessed(String link) throws SQLException;
}
