import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    static Environment env;

    public static void main(String... args) {
        env = Environment.fromArgs(args);
        System.out.println("Logs from your program will appear here!");

        try (ServerSocket serverSocket = new ServerSocket(4221)) {
            serverSocket.setReuseAddress(true);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("accepted new connection");

                Thread.ofVirtual()
                      .start(() -> handleRequest(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleRequest(Socket clientSocket) {
        try {
            HttpRequest httpRequest = new HttpRequest(clientSocket.getInputStream());

            String path = httpRequest.getPath();
            HttpRequest.RequestLine.Method method = httpRequest.getMethod();

            HttpResponse httpResponse = switch (path) {
                case String p when p.equals("/") -> HttpResponse.ok();
                case String p when p.startsWith("/echo") -> Handlers.echo(httpRequest);
                case String p when p.startsWith("/user-agent") -> Handlers.userAgent(httpRequest);
                case String p when p.startsWith("/files") &&
                    method == HttpRequest.RequestLine.Method.GET -> Handlers.readFile(httpRequest);
                case String p when p.startsWith("/files") &&
                    method == HttpRequest.RequestLine.Method.POST -> Handlers.writeFile(httpRequest);
                default -> HttpResponse.notFound();
            };

            System.out.println("handleRequest:: response: " + httpResponse);
            clientSocket.getOutputStream()
                        .write(httpResponse.getBytes());
        } catch (IOException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
