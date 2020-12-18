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
        KeyStorage attachment = (KeyStorage) key.attachment();
        SocketChannel socketChannel = (SocketChannel) key.channel();
        try {
            int a = 0;
            if ((a = socketChannel.read(attachment.getInBuffer())) < 0) {
                proxy.getKeyCloser().close(key);
            } else if (a > 0) {
                if (attachment.getNeighbourKey() == null)
                    proxy.getHeaderParser().parse(key);
                else {
                    if (!((KeyStorage) key.attachment()).getNeighbourKey().isValid())
                        return;
                    attachment.getNeighbourKey().interestOps(attachment.getNeighbourKey().interestOps() | SelectionKey.OP_WRITE);
                    key.interestOps(key.interestOps() ^ SelectionKey.OP_READ);
                    attachment.getInBuffer().flip();
                }
            }
        } catch (IOException e) {
            System.out.println("connection refused");
            proxy.getKeyCloser().close(key);
        }
    }
}
