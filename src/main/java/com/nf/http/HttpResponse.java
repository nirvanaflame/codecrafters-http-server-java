package com.nf.http;

import java.io.Serializable;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpResponse implements Serializable {
    StatusLine statusLine;
    Headers headers;
    Body body;

    public HttpResponse(StatusLine statusLine, Headers headers, Body body) {
        this.statusLine = statusLine; this.headers = headers; this.body = body;
    }

    public byte[] getBytes() {
        return this.toString()
                   .getBytes(UTF_8);
    }

    public static HttpResponse ok() {
        return new HttpResponse(StatusLine.ok(), Headers.withEmptyBody(), Body.empty());
    }

    public static HttpResponse ok(String content) {
        return ok(content, Headers.ContentType.TEXT_PLAIN);
    }

    public static HttpResponse ok(String content, Headers.ContentType contentType) {
        Headers head = new Headers()
            .withContentType(contentType)
            .withContentLength(content.length());
        return new HttpResponse(StatusLine.ok(), head, Body.of(content));
    }

    public static HttpResponse created() {
        return new HttpResponse(StatusLine.created(), Headers.withEmptyBody(), Body.empty());
    }

    public static HttpResponse notFound() {
        return new HttpResponse(StatusLine.notFound(), Headers.withEmptyBody(), Body.empty());
    }

    @Override public String toString() {
        return "%s\r\n%s\r\n\r\n%s".formatted(statusLine.toString(), headers.toString(), body.toString());
    }

    record StatusLine(String version, int code, String status) {
        public static final String HTTP_VERSION = "HTTP/1.1";

        public static StatusLine ok() {
            return new StatusLine(HTTP_VERSION, 200, "OK");
        }

        public static StatusLine created() {
            return new StatusLine(HTTP_VERSION, 201, "Created");
        }

        public static StatusLine notFound() {
            return new StatusLine(HTTP_VERSION, 404, "Not Found");
        }

        @Override public String toString() {
            return "%s %d %s".formatted(version, code, status);
        }
    }
}
