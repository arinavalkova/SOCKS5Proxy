import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: port");
            return;
        }
        int port = Integer.parseInt(args[0]);
        try {
            Proxy proxy = new Proxy(port);
            proxy.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
