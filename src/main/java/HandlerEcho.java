public class HandlerEcho implements HttpRequestHandler {
    @Override public boolean canHandle(HttpRequest request) {
        return request.getPath().startsWith("/echo");
    }

    @Override public HttpResponse handle(HttpRequest request) {
        String path = request.getPath();
        String value = path.split("/echo/")[1];
        return HttpResponse.ok(value);
    }
}
