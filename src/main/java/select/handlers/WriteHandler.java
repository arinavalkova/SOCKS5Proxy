package select.handlers;

import proxy.Proxy;
import select.KeyStorage;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class WriteHandler implements SelectHandler {
    private final Proxy proxy;
    public WriteHandler(Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public void start(SelectionKey currentKey) {
        KeyStorage keyStorage = (KeyStorage) currentKey.attachment();
        SocketChannel socketChannel = (SocketChannel) currentKey.channel();
        try {
            int a;
            if ((a = socketChannel.write(keyStorage.getOutBuffer())) == -1) {
                proxy.getKeyCloser().close(keyStorage);
            }
            else if (keyStorage.getOutBuffer().remaining() == 0) {
                keyStorage.getOutBuffer().clear();
                keyStorage.setInterestOps(SelectionKey.OP_READ);
                if (a > 0 && keyStorage.getNeighbourStorage() != null)
                    keyStorage.getNeighbourStorage().setInterestOps(
                            keyStorage.getNeighbourStorage().getKey().interestOps() | SelectionKey.OP_READ
                    );
            }
        } catch (IOException e) {
            proxy.getKeyCloser().close(keyStorage);
        }
    }
}
