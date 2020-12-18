import proxy.Proxy;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("usage: port");
            System.exit(1);
        }
        try {
            int port = Integer.parseInt(args[0]);
            Proxy proxy = new Proxy(port);
            proxy.start();
        } catch (NumberFormatException | IOException e) {
            System.exit(1);
        }
    }
}