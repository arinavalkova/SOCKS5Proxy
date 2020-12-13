package select.handlers;

import proxy.Proxy;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class AcceptHandler implements SelectHandler{
    private final Proxy proxy;
    public AcceptHandler(Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public void start(SelectionKey currentKey) {
        SocketChannel acceptConnectionSocketChannel;
        try {
            acceptConnectionSocketChannel = ((ServerSocketChannel) currentKey.channel()).accept();
            acceptConnectionSocketChannel.configureBlocking(false);
            acceptConnectionSocketChannel.register(proxy.getSelector(), SelectionKey.OP_READ);

            System.out.println("ACCEPT " + acceptConnectionSocketChannel.getRemoteAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
