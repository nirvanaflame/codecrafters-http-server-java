package com.nf;

import com.nf.handler.*;

import java.util.HashMap;
import java.util.List;

public class ApplicationContext {

    HashMap<Class, Object> context;

    ApplicationContext() {
        this.context = new HashMap<>();

        var handlerEcho = new HandlerEcho();
        context.put(HandlerEcho.class, handlerEcho);

        var readFileHandler = new ReadFileHandler();
        context.put(ReadFileHandler.class, readFileHandler);

        var rootHandler = new RootHandler();
        context.put(RootHandler.class, rootHandler);

        var userAgentHandler = new UserAgentHandler();
        context.put(UserAgentHandler.class, userAgentHandler);

        var writeFileHandler = new WriteFileHandler();
        context.put(WriteFileHandler.class, writeFileHandler);
    }

    public <T> T getBean(Class<T> clazz) {
        return (T) context.get(clazz);
    }

    public <T extends HttpRequestHandler> List<T> getHandlers() {
        return context.entrySet()
            .stream()
            .filter(entry -> entry.getValue() instanceof HttpRequestHandler)
            .map(entry -> (T) getBean(entry.getKey()))
            .toList();
    }
}
