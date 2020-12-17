package select.functional;

import proxy.Proxy;
import select.KeyStorage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class SocketChannelCreator {
    private final Proxy proxy;

    public SocketChannelCreator(Proxy proxy) {
        this.proxy = proxy;
    }

    public void create(KeyStorage keyStorage, InetAddress address, int port) {
        try {
            SocketChannel neighbourChannel = SocketChannel.open();
            neighbourChannel.configureBlocking(false);
            neighbourChannel.connect(new InetSocketAddress(address, port));
            SelectionKey neighbourKey = neighbourChannel.register(
                    keyStorage.getKey().selector(),
                    SelectionKey.OP_CONNECT
            );
            if (!keyStorage.getKey().isValid())
                return;
            keyStorage.setInterestOps(0);
            KeyStorage neighbourStorage = new KeyStorage(neighbourKey);
            neighbourStorage.setNeighbourStorage(keyStorage);
            keyStorage.setNeighbourStorage(neighbourStorage);
        } catch (IOException e) {
            proxy.getKeyCloser().close(keyStorage);
        }
    }
}
