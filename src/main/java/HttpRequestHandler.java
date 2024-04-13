public interface HttpRequestHandler {

    boolean canHandle(HttpRequest request);

    HttpResponse handle(HttpRequest request);
}
