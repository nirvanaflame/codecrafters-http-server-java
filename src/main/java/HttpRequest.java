import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HttpRequest {
    RequestLine requestLine;
    Headers headers;
    Body body;

    HttpRequest(InputStream inputStream) throws RuntimeException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream), 1024);

        try {
            this.requestLine = new RequestLine(br);
            this.headers = new Headers(br);
            this.body = new Body(br);
        } catch (IOException e) {
            System.err.println("Cannot construct HttpRequest: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    String getPath() {
        return requestLine.path;
    }

    RequestLine.Method getMethod() {
        return requestLine.method;
    }

    Body getBody() {
        return body;
    }

    @Override public String toString() {
        return "%s\r\n%s\r\n\r\n%s".formatted(requestLine.toString(), headers.toString(), body.toString());
    }

    public static class RequestLine {
        Method method;
        String path;
        String version;

        public RequestLine(BufferedReader buffer) throws IOException {
            String requestLine = buffer.readLine();
            System.out.println("requestLine: " + requestLine);
            if (requestLine == null) throw new IOException("RequestLine is null");

            String[] arr = requestLine.split(" ");

            this.method = Method.valueOf(arr[0]);
            this.path = arr[1];
            this.version = arr[2];
        }

        public enum Method {
            GET,
            POST
        }

        @Override public String toString() {
            return "%s %s %s".formatted(method.name(), path, version);
        }
    }
}

