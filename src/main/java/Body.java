import java.io.BufferedReader;
import java.io.IOException;

public class Body {
    String content;

    private Body(String content) {
        this.content = content;
    }

    Body(BufferedReader buffer) throws IOException {

        StringBuilder sb = new StringBuilder();
        while (buffer.ready()) {
            int read = buffer.read();
            sb.append(Character.toChars(read));
        }

        this.content = sb.toString();
        System.out.println("Body:: init: " + content);
    }

    public static Body of(String content) {
        return new Body(content);
    }

    public static Body empty() {
        return new Body("");
    }

    @Override public String toString() {
        return content;
    }
}
