package select.handlers;

import proxy.KeyStorage;
import proxy.Proxy;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ConnectHandler implements Handler {
    private static final byte REQUEST_GRANTED = 0x00;

    @Override
    public void handle(SelectionKey key, Proxy proxy) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        KeyStorage attachment = (KeyStorage) key.attachment();
        try {
            socketChannel.finishConnect();
            attachment.getInBuffer().put(proxy.getResponseCreator().createServerResponse(key, REQUEST_GRANTED)).flip();
            attachment.setOut(((KeyStorage) attachment.getNeighbourKey().attachment()).getInBuffer());
            ((KeyStorage) attachment.getNeighbourKey().attachment()).setOut(attachment.getInBuffer());
            attachment.getNeighbourKey().interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        } catch (Exception e) {
            System.out.println("connection refused");
            proxy.getKeyCloser().close(key);
        }
    }

}
