package select.handlers;

import proxy.Proxy;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class AcceptHandler implements Handler {
    @Override
    public void handle(SelectionKey key, Proxy proxy) {
        try {
            SocketChannel clientSocketChannel = ((ServerSocketChannel) key.channel()).accept();
            clientSocketChannel.configureBlocking(false);
            System.out.println("accept connection from " + clientSocketChannel.getRemoteAddress());

            clientSocketChannel.register(proxy.getSelector(), SelectionKey.OP_READ);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
