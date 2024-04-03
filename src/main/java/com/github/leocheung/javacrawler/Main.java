package com.github.leocheung.javacrawler;

public class Main {
    public static void main(String[] args) {
        int threadNum = 6;
        CrawlerDAO dao = new MybatisCrawlerDAO();
        for (int i = 0; i < threadNum; i++) {
            new Thread(new Crawler(dao)).start();
        }
    }
}
