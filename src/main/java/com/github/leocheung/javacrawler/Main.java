package com.github.leocheung.javacrawler;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpGet httpGet = new HttpGet("https://sina.cn");
        try (
                CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse httpResponse = httpClient.execute(httpGet)
        ) {
            HttpEntity httpEntity = httpResponse.getEntity();
            System.out.println(httpResponse.getStatusLine());
            System.out.println(EntityUtils.toString(httpEntity));
            EntityUtils.consume(httpEntity);
        }
    }
}
