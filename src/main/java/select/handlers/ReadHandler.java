package select.handlers;

import proxy.Proxy;
import select.KeyStorage;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ReadHandler implements SelectHandler {
    private final Proxy proxy;
    public ReadHandler(Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public void start(SelectionKey currentKey) throws IOException {
        SocketChannel channel = ((SocketChannel) currentKey.channel());
        KeyStorage keyStorage = ((KeyStorage) currentKey.attachment());
        if (keyStorage == null) {
            keyStorage = new KeyStorage(currentKey);
        }
        if (channel.read(keyStorage.getInBuffer()) < 1) {
            proxy.getKeyCloser().close(keyStorage.getKey());
        } else if (keyStorage.getNeighbourStorage() == null) {
            proxy.getHeaderParser().parse(keyStorage);
        } else {
//            if (!((Attachment) key.attachment()).getPeer().isValid())
//                return;
//            attachment.peer.interestOps(attachment.peer.interestOps() | SelectionKey.OP_WRITE);
//            key.interestOps(key.interestOps() ^ SelectionKey.OP_READ);
//            attachment.in.flip();
        }
    }
}
