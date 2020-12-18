package select.handlers;

import proxy.KeyStorage;
import proxy.Proxy;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class WriteHandler implements Handler {
    @Override
    public void handle(SelectionKey key, Proxy proxy) throws IOException {
        KeyStorage attachment = (KeyStorage) key.attachment();
        SocketChannel socketChannel = (SocketChannel) key.channel();
        try {
            int a = 0;
            if ((a = socketChannel.write(attachment.getOutBuffer())) == -1) {
                proxy.getKeyCloser().close(key);
            } else if (attachment.getOutBuffer().remaining() == 0) {
                attachment.getOutBuffer().clear();
                key.interestOps(SelectionKey.OP_READ);
                if (a > 0 && attachment.getNeighbourKey() != null)
                    attachment.getNeighbourKey().interestOps(attachment.getNeighbourKey().interestOps() | SelectionKey.OP_READ);
            }
        } catch (IOException e) {
            System.out.println("connection refused");
            proxy.getKeyCloser().close(key);
        }
    }
}
