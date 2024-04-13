package com.nf.handler;

import com.nf.http.HttpRequest;
import com.nf.http.HttpResponse;

public class UserAgentHandler implements HttpRequestHandler {
    @Override
    public boolean canHandle(HttpRequest request) {
        return request.getPath().startsWith("/user-agent");
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        String value = request.getHeaders().getValue("User-Agent");
        return HttpResponse.ok(value);
    }
}
