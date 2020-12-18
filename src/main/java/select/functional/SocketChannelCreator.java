package select.functional;

import proxy.KeyStorage;
import proxy.Proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class SocketChannelCreator {

    private final Proxy proxy;

    public SocketChannelCreator(Proxy proxy) {
        this.proxy = proxy;
    }

    public void create(SelectionKey key) throws IOException {
        try {
            KeyStorage keyStorage = (KeyStorage) key.attachment();

            SocketChannel neighbour = SocketChannel.open();
            neighbour.configureBlocking(false);
            neighbour.connect(new InetSocketAddress(keyStorage.getAddress(), keyStorage.getPort()));

            SelectionKey neighbourKey = neighbour.register(key.selector(), SelectionKey.OP_CONNECT);
            if (!key.isValid())
                return;
            key.interestOps(0);
            keyStorage.setNeighbourKey(neighbourKey);
            KeyStorage neighbourStorage = new KeyStorage(neighbourKey);
            neighbourStorage.setNeighbourKey(key);
            neighbourKey.attach(neighbourStorage);
        } catch (IOException e) {
            proxy.getKeyCloser().close(key);
        }
    }
}
