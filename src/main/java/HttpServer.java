import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    public static ApplicationContext context;

    static Environment env;

    public static void main(String... args) {
        context = new ApplicationContext();
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

            var handlers = context.getHandlers();
            HttpResponse httpResponse = handlers.stream()
                                       .filter(h -> h.canHandle(httpRequest))
                                       .findFirst()
                                       .map(h -> h.handle(httpRequest))
                                       .orElseGet(HttpResponse::notFound);

            System.out.println("handleRequest:: response: " + httpResponse);
            clientSocket.getOutputStream()
                        .write(httpResponse.getBytes());
        } catch (IOException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
