package select.handlers;

import proxy.KeyStorage;
import proxy.Proxy;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class WriteHandler implements Handler {
    @Override
    public void handle(SelectionKey key, Proxy proxy) throws IOException {
        try {
            KeyStorage keyStorage = (KeyStorage) key.attachment();
            SocketChannel socketChannel = (SocketChannel) key.channel();
            int ret;
            if ((ret = socketChannel.write(keyStorage.getOutBuffer())) == -1) {
                proxy.getKeyCloser().close(key);
            } else if (keyStorage.getOutBuffer().remaining() == 0) {
                keyStorage.getOutBuffer().clear();
                key.interestOps(SelectionKey.OP_READ);
                if (ret > 0 && keyStorage.getNeighbourKey() != null)
                    keyStorage
                            .getNeighbourKey()
                            .interestOps(keyStorage
                                    .getNeighbourKey()
                                    .interestOps() | SelectionKey.OP_READ
                            );
            }
        } catch (IOException e) {
            proxy.getKeyCloser().close(key);
        }
    }
}
