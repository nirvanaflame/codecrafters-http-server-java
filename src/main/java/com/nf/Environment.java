package com.nf;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    static Map<String, String> env;

    private Environment() {
        env = new HashMap<>();
    }

    static Environment fromArgs(String... args) {
        var environment = new Environment();

        for (int i = 0; i < args.length; i++) {
            env.put(args[i], args[++i]);
        }

        return environment;
    }

    public String getValue(String key) {
        return env.get(key);
    }
}
