package com.github.leocheung.javacrawler;

import java.io.IOException;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException, IOException {
        new Crawler().run();
    }
}
