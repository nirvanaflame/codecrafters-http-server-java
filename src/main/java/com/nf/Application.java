package com.nf;

import com.nf.http.HttpServer;

public class Application {
    public static ApplicationContext context;
    public static Environment env;

    public static void main(String[] args) {
        context = new ApplicationContext();
        env = Environment.fromArgs(args);

        var httpServer = new HttpServer(4221);
        httpServer.start();
    }

    public static ApplicationContext getContext() {
        return context;
    }

    public static Environment getEnv() {
        return env;
    }
}
