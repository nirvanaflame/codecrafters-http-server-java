import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        System.out.println("Logs from your program will appear here!");

        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        try {
            serverSocket = new ServerSocket(4221);
            serverSocket.setReuseAddress(true);
            clientSocket = serverSocket.accept(); // Wait for connection from
            // client.

            InputStream inputStream = clientSocket.getInputStream();

            DataOutputStream dataOutputStream = new DataOutputStream(
                    clientSocket.getOutputStream());

            byte[] message = "HTTP/1.1 200 OK\r\n\r\n".getBytes(
                    StandardCharsets.UTF_8);
            dataOutputStream.write(message);

            System.out.println("accepted new connection");
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
