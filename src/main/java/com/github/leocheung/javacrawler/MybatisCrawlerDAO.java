package com.github.leocheung.javacrawler;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MybatisCrawlerDAO implements CrawlerDAO {

    private static final String MYBATIS_CONFIG = "db/mybatis/config.xml";

    private SqlSessionFactory sqlSessionFactory;

    @Override
    public String getAndDeleteLinkFromDatabase() {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            String link = session.selectOne("com.github.leocheung.javacrawler.mapper.selectToBeProcessedLink");
            if (link != null) {
                session.delete("com.github.leocheung.javacrawler.mapper.deleteToBeProcessedLink", link);
                return link;
            }
            return null;
        }
    }

    @Override
    public void insertNewsIntoDataBase(String title, String content, String link) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.leocheung.javacrawler.mapper.insertNews", new News(title, content, link));
        }
    }

    @Override
    public boolean isLinkProcessed(String link) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            int count = session.selectOne("com.github.leocheung.javacrawler.mapper.countSpecialLinkInAlreadyProcessedLink", link);
            return count > 0;
        }
    }

    @Override
    public void connect() throws IOException {
        InputStream inputStream = Resources.getResourceAsStream(MYBATIS_CONFIG);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    @Override
    public void insertLinkToAlreadyProcessed(String link) {
        Map<String, String> params = new HashMap<>();
        params.put("tableName", "links_already_processed");
        params.put("link", link);
        insertLink(params);
    }

    @Override
    public void insertLinkToBeProcessed(String link) {
        Map<String, String> params = new HashMap<>();
        params.put("tableName", "links_to_be_processed");
        params.put("link", link);
        insertLink(params);
    }

    private void insertLink(Map<String, String> params) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.leocheung.javacrawler.mapper.insertLink", params);
        }
    }
}
