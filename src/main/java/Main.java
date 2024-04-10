import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Main {
    public static void main(String[] args) {
        System.out.println("Logs from your program will appear here!");

        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        try {
            serverSocket = new ServerSocket(4221);
            serverSocket.setReuseAddress(true);
            clientSocket = serverSocket.accept();

            byte[] response = createResponse(clientSocket.getInputStream());

            OutputStream outputStream = clientSocket.getOutputStream();
            outputStream.write(response);

            System.out.println("accepted new connection");
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static byte[] createResponse(InputStream inputStream) throws IOException {
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

        return path.equals("/") ? ok("") : notFound();
    }

    private static String[] lines(InputStream inputStream) throws IOException {
        return new BufferedReader(new InputStreamReader(inputStream))
                .readLine()
                .split("\r\n");
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

    private static byte[] ok(String text) {
        String response;
        if (text == null || text.isBlank()) {
            response = "HTTP/1.1 200 OK\r\n\r\n";
        } else {
            response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Content-Length: %d\r\n".formatted(text.length()) +
                    "\r\n" +
                    "%s".formatted(text);
        }

        return response.getBytes(UTF_8);
    }

    private static byte[] notFound() {
        return "HTTP/1.1 404 Not Found\r\n\r\n".getBytes(UTF_8);
    }
}
