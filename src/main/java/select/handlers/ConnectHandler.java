package select.handlers;

import proxy.Proxy;
import select.KeyStorage;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ConnectHandler implements SelectHandler {
    private static final byte REQUEST_GRANTED = 0x00;

    private final Proxy proxy;

    public ConnectHandler(Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public void start(SelectionKey currentKey) {
        SocketChannel socketChannel = (SocketChannel) currentKey.channel();
        KeyStorage keyStorage = (KeyStorage) currentKey.attachment();
        try {
            socketChannel.finishConnect();
            keyStorage.getInBuffer().put(
                    proxy.getResponseSender().createResponse(currentKey, REQUEST_GRANTED)
            ).flip();
            keyStorage.setOut(keyStorage.getNeighbourStorage().getInBuffer());
            keyStorage.getNeighbourStorage().setOut(keyStorage.getInBuffer());
            keyStorage.getNeighbourStorage().setInterestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        } catch (Exception e) {
            proxy.getKeyCloser().close(keyStorage);
        }
    }
}
