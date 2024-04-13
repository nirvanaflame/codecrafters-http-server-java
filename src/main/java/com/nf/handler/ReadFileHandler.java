package com.nf.handler;

import com.nf.Application;
import com.nf.http.Headers;
import com.nf.http.HttpRequest;
import com.nf.http.HttpResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ReadFileHandler implements HttpRequestHandler {
    private static final String DIRECTORY = "--directory";

    private static List<String> readAllLines(Path p) {
        try {
            return Files.readAllLines(p);
        } catch (IOException e) {
            System.err.println("cannot read file");
            return Collections.emptyList();
        }
    }

    @Override
    public boolean canHandle(HttpRequest request) {
        return request.getPath().startsWith("/files")
            && request.getMethod() == HttpRequest.RequestLine.Method.GET;
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        String dirName = Application.getEnv().getValue(DIRECTORY);

        String path = request.getPath();
        String fileName = path.split("/files/")[1];

        Optional<String> result;
        try {
            result = Files
                .walk(Path.of(dirName))
                .filter(Files::isRegularFile)
                .filter(p -> p.endsWith(fileName))
                .findFirst()
                .map(p -> readAllLines(p))
                .map(list -> String.join("\n", list));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result.map(s -> HttpResponse.ok(s, Headers.ContentType.APPLICATION_OCTET_STREAM))
            .orElseGet(HttpResponse::notFound);
    }
}
