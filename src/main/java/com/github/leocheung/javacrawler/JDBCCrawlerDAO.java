package com.github.leocheung.javacrawler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.*;

public class JDBCCrawlerDAO implements CrawlerDAO {
    private static final String JDBC_URL = "jdbc:h2:file:/Users/naliankeji/leo-space/hcsp/java-crawler/db/sina_news";
    private static final String DATABASE_USER = "root";
    private static final String DATABASE_PASSWORD = "root";
    private Connection connection;

    public String getAndDeleteLinkFromDatabase() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT `link` FROM `links_to_be_processed` LIMIT 1;")) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String link = resultSet.getString(1);
                updateLinkDatabase(link, "DELETE FROM `links_to_be_processed` WHERE `link` = ?;");
                return link;
            }
            return null;
        }
    }

    public void updateLinkDatabase(String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    public void insertNewsIntoDataBase(String title, String content, String link) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO `news` (`title`,`content`,`link`) VALUES (?,?,?)")) {
            statement.setString(1, title);
            statement.setString(2, content);
            statement.setString(3, link);
            statement.executeUpdate();
        }
    }

    public boolean isLinkProcessed(String link) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM `links_already_processed` WHERE `link` = ?;")) {
            statement.setString(1, link);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        }
    }

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public void connect() throws SQLException {
        if (connection != null) {
            return;
        }
        this.connection = DriverManager.getConnection(JDBC_URL, DATABASE_USER, DATABASE_PASSWORD);
    }
}
