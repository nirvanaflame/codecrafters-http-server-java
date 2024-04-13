package com.nf.http;

import com.nf.Application;
import com.nf.ApplicationContext;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {

    int port;
    ApplicationContext context;

    public HttpServer(int port) {
        this.port = port;
    }

    public void start() {
        System.out.println("Logs from your program will appear here!");
        this.context = Application.getContext();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
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

    private void handleRequest(Socket clientSocket) {
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
