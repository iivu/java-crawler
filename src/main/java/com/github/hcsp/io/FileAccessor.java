package com.github.hcsp.io;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileAccessor {
    /**
     * 使用InputStream逐个字节读取
     *
     * @param file the file to read
     * @return the list of file content per line
     * @throws IOException IOException
     */
    public static List<String> readFile1(File file) throws IOException {
        try (InputStream is = Files.newInputStream(file.toPath())) {
            StringBuilder sb = new StringBuilder();
            while (true) {
                int readLen = is.read();
                if (readLen == -1) {
                    break;
                }
                sb.append((char) readLen);
            }
            String content = sb.toString();
            return Arrays.asList(content.split("\\n|\\r\\n"));
        }
    }

    /**
     * 使用BufferedReader逐行读取
     *
     * @param file the file to read
     * @return the list of file content per line
     * @throws IOException IOException
     */
    public static List<String> readFile2(File file) throws IOException {
        try (FileReader fr = new FileReader(file)) {
            BufferedReader br = new BufferedReader(fr);
            List<String> result = new ArrayList<>();
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                result.add(line);
            }
            return result;
        }
    }

    /**
     * 使用Files工具读取
     *
     * @param file the file to read
     * @return the list of file content per line
     * @throws IOException IOException
     */
    public static List<String> readFile3(File file) throws IOException {
        return Files.readAllLines(file.toPath(), Charset.defaultCharset());
    }


    /**
     * 使用OutputStream逐字节写入
     *
     * @param lines the content to be written
     * @param file  the file to write
     * @throws IOException IOException
     */
    public static void writeLinesToFile1(List<String> lines, File file) throws IOException {
        try (OutputStream os = Files.newOutputStream(file.toPath())) {
            for (String line : lines) {
                os.write(line.getBytes(StandardCharsets.UTF_8));
                os.write('\n');
            }
        }
    }

    /**
     * 使用BufferedWriter逐行写入
     *
     * @param lines the content to be written
     * @param file  the file to write
     * @throws IOException IOException
     */
    public static void writeLinesToFile2(List<String> lines, File file) throws IOException {
        try (FileWriter fw = new FileWriter(file)) {
            BufferedWriter bw = new BufferedWriter(fw);
            for (String line : lines) {
                bw.append(line).append('\n').flush();
            }
        }
    }

    /**
     * 使用Files工具写入
     *
     * @param lines the content to be written
     * @param file  the file to write
     * @throws IOException IOException
     */
    public static void writeLinesToFile3(List<String> lines, File file) throws IOException {
        Files.write(file.toPath(), lines, Charset.defaultCharset());
    }

    public static void main(String[] args) throws IOException {
        File projectDir = new File(System.getProperty("basedir", System.getProperty("user.dir")));
        File testFile = new File(projectDir, "target/test.txt");
        List<String> lines = Arrays.asList("AAA", "BBB", "CCC");
        writeLinesToFile1(lines, testFile);
        writeLinesToFile2(lines, testFile);
        writeLinesToFile3(lines, testFile);

        System.out.println(readFile1(testFile));
        System.out.println(readFile2(testFile));
        System.out.println(readFile3(testFile));
    }
}
