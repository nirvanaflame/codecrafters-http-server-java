import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

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

            InputStream inputStream = clientSocket.getInputStream();
            String[] in = convertToString(inputStream);

            String path = in[1];
            byte[] response = createResponse(path);

            OutputStream outputStream = clientSocket.getOutputStream();
            outputStream.write(response);

            System.out.println("accepted new connection");
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static byte[] createResponse(String path) {
        if (path.startsWith("/echo")) {
            String text = path.split("/")[2];

            return ("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Content-Length: %d\r\n".formatted(text.length()) +
                    "\r\n" +
                    "%s".formatted(text))
                    .getBytes(UTF_8);
        }

        return path.equals("/")
                ? "HTTP/1.1 200 OK\r\n\r\n".getBytes(UTF_8)
                : "HTTP/1.1 404 Not Found\r\n\r\n".getBytes(UTF_8);
    }

    private static String[] convertToString(InputStream inputStream) throws IOException {
        return new BufferedReader(new InputStreamReader(inputStream))
                .readLine()
                .split(" ");
    }
}
