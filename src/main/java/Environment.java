import java.util.HashMap;
import java.util.Map;

class Environment {
    Map<String, String> env;

    private Environment() {
        env = new HashMap<>();
    }

    static Environment fromArgs(String... args) {
        Environment environment = new Environment();
        var env = environment.env;

        for (int i = 0; i < args.length; i++) {
            env.put(args[i], args[++i]);
        }
        return environment;
    }

    String getValue(String key) {
        return env.get(key);
    }
}
