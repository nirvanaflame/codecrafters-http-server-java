import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Handlers {

    public static final String DIRECTORY = "--directory";

    static HttpResponse echo(HttpRequest request) {
        String path = request.getPath();
        String value = path.split("/echo/")[1];
        return HttpResponse.ok(value);
    }

    static HttpResponse userAgent(HttpRequest request) {
        String value = request.headers.getValue("User-Agent");
        return HttpResponse.ok(value);
    }

    static HttpResponse readFile(HttpRequest httpRequest) throws IOException {
        String dirName = HttpServer.env.getValue(DIRECTORY);
        String path = httpRequest.getPath();
        String fileName = path.split("/files/")[1];

        Optional<String> result = Files
            .walk(Path.of(dirName))
            .filter(Files::isRegularFile)
            .filter(p -> p.endsWith(fileName))
            .findFirst()
            .map(p -> readAllLines(p))
            .map(list -> String.join("\n", list));

        return result.map(s -> HttpResponse.ok(s, Headers.ContentType.APPLICATION_OCTET_STREAM))
                     .orElseGet(HttpResponse::notFound);
    }

    private static List<String> readAllLines(Path p) {
        try {
            return Files.readAllLines(p);
        } catch (IOException e) {
            System.err.println("cannot read file");
            return Collections.emptyList();
        }
    }

    static HttpResponse writeFile(HttpRequest httpRequest) throws IOException {
        String dirName = HttpServer.env.getValue(DIRECTORY);
        String path = httpRequest.getPath();
        String fileName = path.split("/files/")[1];

        Path filePath = Path.of(dirName, fileName);
        Body body = httpRequest.getBody();

        Files.writeString(filePath, body.toString(), StandardOpenOption.CREATE);

        return HttpResponse.created();
    }
}
