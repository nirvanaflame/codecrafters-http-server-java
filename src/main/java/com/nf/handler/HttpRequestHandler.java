package com.nf.handler;

import com.nf.http.HttpRequest;
import com.nf.http.HttpResponse;

public interface HttpRequestHandler {

    boolean canHandle(HttpRequest request);

    HttpResponse handle(HttpRequest request);
}
