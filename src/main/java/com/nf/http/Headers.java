package com.nf.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Headers {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_LENGTH = "Content-Length";

    List<Pair> headers;

    Headers() {
        this.headers = new ArrayList<>();
    }

    Headers(BufferedReader buffer) throws IOException {
        this(readHeaders(buffer));
    }

    Headers(Pair... pairs) {
        this.headers = Arrays.stream(pairs)
            .collect(Collectors.toList());
    }

    public static Headers withEmptyBody() {
        Headers head = new Headers();
        head.withContentLength(0);
        head.withContentType(ContentType.TEXT_PLAIN);
        return head;
    }

    Headers withContentType(ContentType contentType) {
        this.headers.add(new Pair(CONTENT_TYPE, contentType.value));
        return this;
    }

    Headers withContentLength(int length) {
        this.headers.add(new Pair(CONTENT_LENGTH, String.valueOf(length)));
        return this;
    }

    private static Pair[] readHeaders(BufferedReader br) throws IOException {
        var lines = new ArrayList<Pair>();
        String line;
        while (!(line = br.readLine()).isEmpty()) {
            String[] s = line.split(": ");
            lines.add(new Pair(s[0], s[1]));
        }
        System.out.println("readHeader:: headers: " + lines);

        return lines.toArray(Pair[]::new);
    }

    public String getValue(String key) {
        return this.headers.stream()
            .filter(pair -> pair.key.equals(key))
            .findFirst()
            .map(Pair::value)
            .orElse(null);
    }

    @Override
    public String toString() {
        return this.headers
            .stream()
            .map(Pair::toString)
            .collect(Collectors.joining("\r\n"));
    }

    public enum ContentType {
        TEXT_PLAIN("text/plain"),
        APPLICATION_OCTET_STREAM("application/octet-stream");

        final String value;

        ContentType(String value) {
            this.value = value;
        }
    }

    record Pair(String key, String value) {
        @Override
        public String toString() {
            return "%s: %s".formatted(key, value);
        }
    }
}
