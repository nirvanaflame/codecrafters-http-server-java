package com.nf.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Headers {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String CONTENT_ENCODING = "Content-Encoding";
    private static final String ACCEPT_ENCODING = "Accept-Encoding";

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

    static Headers withEmptyBody() {
        Headers head = new Headers();
        head.withContentLength(0);
        head.withContentType(ContentType.TEXT_PLAIN);
        return head;
    }

    Headers appendHeaders(Headers headers) {
        headers.stream()
            .forEach(h -> {
                if (!this.headers.contains(h)) {
                    this.headers.add(h);
                }
            });
        return this;
    }

    Headers withContentType(ContentType contentType) {
        this.headers.add(new Pair(CONTENT_TYPE, contentType.value));
        return this;
    }

    Headers withContentLength(int length) {
        this.headers.add(new Pair(CONTENT_LENGTH, String.valueOf(length)));
        return this;
    }

    Headers withContentEncoding(Encoding encoding) {
        this.headers.add(new Pair(CONTENT_ENCODING, encoding.getEncoding()));
        return this;
    }

    private static Pair[] readHeaders(BufferedReader br) throws IOException {
        var lines = new ArrayList<Pair>();
        String line;
        while (!(line = br.readLine()).isEmpty()) {
            String[] s = line.split(": ");
            var h = filterRequestHeader(s);
            if (h != null) {
                lines.add(h);
            }
        }
        System.out.println("readHeader:: headers: " + lines);

        return lines.toArray(Pair[]::new);
    }

    private static Pair filterRequestHeader(String... s) {
        return switch (s[0]) {
            case ACCEPT_ENCODING -> Encoding.contains(s[1]) ? new Pair(CONTENT_ENCODING, s[1]) : null;
            default -> null;
        };
    }

    public String getValue(String key) {
        return this.headers.stream()
            .filter(pair -> pair.key.equals(key))
            .findFirst()
            .map(Pair::value)
            .orElse(null);
    }

    Stream<Pair> stream() {
        return this.headers.stream();
    }

    @Override
    public String toString() {
        return this.headers
            .stream()
            .map(Pair::toString)
            .collect(Collectors.joining("\r\n"));
    }

    public enum Encoding {
        GZIP("gzip");

        private final String encoding;

        Encoding(String encoding) {
            this.encoding = encoding;
        }

        public String getEncoding() {
            return encoding;
        }

        public static boolean contains(String encoding) {
            return Arrays.stream(values()).anyMatch(x -> x.encoding.equals(encoding));
        }
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
