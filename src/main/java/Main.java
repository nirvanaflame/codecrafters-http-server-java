import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Main {
    public static void main(String... args) {
        System.out.println("Logs from your program will appear here!");

        try (ServerSocket serverSocket = new ServerSocket(4221)) {
            serverSocket.setReuseAddress(true);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("accepted new connection");

                Thread.ofVirtual()
                      .start(() -> handleRequest(clientSocket, args));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleRequest(Socket clientSocket, String... args) {
        try {
            byte[] response = createResponse(clientSocket.getInputStream(), args);
            OutputStream outputStream = clientSocket.getOutputStream();
            outputStream.write(response);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    private static byte[] createResponse(InputStream inputStream, String[] args) throws IOException {
        String[] request = lines(inputStream);

        String[] requestLine = request[0].split(" ");
        String method = requestLine[0];
        String path = requestLine[1];

        switch (path) {
            case String p when p.equals("/") -> ok();
            case String p when p.startsWith("/echo") -> ok(path.split("/echo/")[1]);
            case String p when p.startsWith("/user-agent") -> ok(headers(request).get("User-Agent"));
            case String p when p.startsWith("/files") && method.equals("GET") -> {
                String fileContent = readFile(path, args);
                return fileContent.isEmpty() ? notFound() : ok(fileContent, "application/octet-stream");
            }
            case String p when p.startsWith("/files") && method.equals("POST") -> {

            }
            default -> notFound();
        }

        return path.equals("/") ? ok("") : notFound();
    }

    private static String readFile(String path, String... args) throws IOException {
        String fileName = path.split("/files/")[1];

        int i = indexOf("--directory", args);
        String dirName = args[i + 1];

        return Files
            .walk(Path.of(dirName))
            .filter(Files::isRegularFile)
            .filter(p -> p.endsWith(fileName))
            .map(p -> readAllLines(p))
            .flatMap(Collection::stream)
            .collect(Collectors.joining("\n"));
    }

    private static List<String> readAllLines(Path p) {
        try {
            return Files.readAllLines(p);
        } catch (IOException e) {
            System.out.println("cannot read file");
            return Collections.emptyList();
        }
    }

    private static String[] lines(InputStream inputStream) throws IOException {
        BufferedReader br = new BufferedReader(
            new InputStreamReader(inputStream));

        List<String> strings = new ArrayList<>();
        String line = br.readLine();
        while (!line.isEmpty()) {
            strings.add(line);
            line = br.readLine();
        }

        return strings.toArray(String[]::new);
    }

    private static Map<String, String> headers(String[] in) {
        Map<String, String> headers = new HashMap<>();
        for (int i = 1; i < in.length; i++) {
            String str = in[i];

            if (str.equals("\r\n")) {break;}

            String[] kv = str.split(": ");
            headers.put(kv[0], kv[1]);
        }
        return headers;
    }

    private static int indexOf(String key, String... array) {
        int i = 0;
        while (i < array.length) {
            if (key.equals(array[i])) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private static byte[] ok() {
        return ok("");
    }

    private static byte[] ok(String text) {
        return ok(text, "text/plain");
    }

    private static byte[] ok(String text, String contentType) {
        if (text == null || text.isBlank()) {
            return "HTTP/1.1 200 OK\r\n\r\n".getBytes(UTF_8);
        }

        String response = STR."""
            HTTP/1.1 200 OK\r
            Content-Type: \{contentType}\r
            Content-Length: \{text.length()}\r
            \r
            \{text}
            """.trim();

        return response.getBytes(UTF_8);
    }

    private static byte[] notFound() {
        String message = "HTTP/1.1 404 Not Found\r\n" +
            "Content-Length: 0\r\n" +
            "\r\n";
        return message.getBytes(UTF_8);
    }
}
