package select.handlers;

import proxy.KeyStorage;
import proxy.Proxy;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ReadHandler implements Handler {
    @Override
    public void handle(SelectionKey key, Proxy proxy) throws IOException {
        if (key.attachment() == null) {
            key.attach(new KeyStorage(key));
        }
        try {
            KeyStorage keyStorage = (KeyStorage) key.attachment();
            SocketChannel socketChannel = (SocketChannel) key.channel();
            int ret;
            if ((ret = socketChannel.read(keyStorage.getInBuffer())) < 0) {
                proxy.getKeyCloser().close(key);
            } else if (ret > 0) {
                if (keyStorage.getNeighbourKey() == null)
                    proxy.getHeaderParser().parse(key);
                else {
                    if (!((KeyStorage) key.attachment()).getNeighbourKey().isValid())
                        return;
                    keyStorage
                            .getNeighbourKey()
                            .interestOps(keyStorage
                                    .getNeighbourKey()
                                    .interestOps() | SelectionKey.OP_WRITE
                            );
                    key.interestOps(key.interestOps() ^ SelectionKey.OP_READ);
                    keyStorage.getInBuffer().flip();
                }
            }
        } catch (IOException e) {
            proxy.getKeyCloser().close(key);
        }
    }
}
