public class RootHandler implements HttpRequestHandler {

    @Override public boolean canHandle(HttpRequest request) {
        return "/".equals(request.getPath());
    }

    @Override public HttpResponse handle(HttpRequest request) {
        return HttpResponse.ok();
    }
}
