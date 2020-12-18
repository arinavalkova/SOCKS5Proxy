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
        socketChannel.finishConnect();

        try {
            KeyStorage keyStorage = (KeyStorage) key.attachment();
            keyStorage.getInBuffer().put(
                    proxy.getResponseCreator().createServerResponse(key, REQUEST_GRANTED)
            ).flip();
            keyStorage.setOut(
                    ((KeyStorage) keyStorage
                            .getNeighbourKey()
                            .attachment())
                            .getInBuffer()
            );
            ((KeyStorage) keyStorage
                    .getNeighbourKey()
                    .attachment()).setOut(keyStorage.getInBuffer());
            keyStorage
                    .getNeighbourKey()
                    .interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        } catch (Exception e) {
            proxy.getKeyCloser().close(key);
        }
    }

}
