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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
            byte[] response = createResponse(clientSocket.getInputStream(),
                                             args);
            OutputStream outputStream = clientSocket.getOutputStream();
            outputStream.write(response);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    private static byte[] createResponse(InputStream inputStream, String[] args)
            throws IOException {
        String[] request = lines(inputStream);

        String path = request[0].split(" ")[1];
        if (path.startsWith("/echo")) {
            String text = path.split("/echo/")[1];

            return ok(text);
        }

        if (path.startsWith("/user-agent")) {
            String userAgent = headers(request).get("User-Agent");

            return ok(userAgent);
        }

        if (path.startsWith("/files")) {
            String fileName = path.split("/files/")[1];

            int i = indexOf("--directory", args);
            String dirName = args[i + 1];

            System.out.println("/files: dirname:" + dirName);
            System.out.println("/files: fileName:" + fileName);

            Optional<Path> optionalPath = Files
                    .walk(Path.of(dirName))
                    .peek(System.out::println)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.endsWith(fileName))
                    .findFirst();


            if (optionalPath.isPresent()) {
                String fileText = Files.lines(optionalPath.get())
                                      .collect(Collectors.joining("\n"));
                return ok(fileText, "application/octet-stream");
            }
        }

        return path.equals("/") ? ok("") : notFound();
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

    private static byte[] ok(String text) {
        return ok(text, "text/plain");
    }

    private static byte[] ok(String text, String contentType) {
        if (text == null || text.isBlank()) {
            return "HTTP/1.1 200 OK\r\n\r\n".getBytes(UTF_8);
        }

        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: %s\r\n".formatted(contentType) +
                "Content-Length: %d\r\n".formatted(text.length()) +
                "\r\n" +
                "%s".formatted(text);
        return response.getBytes(UTF_8);
    }

    private static byte[] notFound() {
        return "HTTP/1.1 404 Not Found\r\n\r\n".getBytes(UTF_8);
    }
}
