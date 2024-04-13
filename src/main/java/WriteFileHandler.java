import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class WriteFileHandler implements HttpRequestHandler {
    public static final String DIRECTORY = "--directory";

    @Override public boolean canHandle(HttpRequest request) {
        return request.getPath().startsWith("/files")
            && request.getMethod() == HttpRequest.RequestLine.Method.POST;
    }

    @Override public HttpResponse handle(HttpRequest request) {
        String dirName = HttpServer.env.getValue(DIRECTORY);
        String path = request.getPath();
        String fileName = path.split("/files/")[1];

        Path filePath = Path.of(dirName, fileName);
        Body body = request.getBody();

        try {
            Files.writeString(filePath, body.toString(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            System.err.println("Failed to write to file: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return HttpResponse.created();
    }
}
