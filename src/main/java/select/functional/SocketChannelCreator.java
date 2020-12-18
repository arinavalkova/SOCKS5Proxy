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
            KeyStorage attachment = (KeyStorage) key.attachment();
            SocketChannel peer = SocketChannel.open();
            peer.configureBlocking(false);
            peer.connect(new InetSocketAddress(attachment.getAddress(), attachment.getPort()));
            SelectionKey peerKey = peer.register(key.selector(), SelectionKey.OP_CONNECT);
            if (!key.isValid())
                return;
            key.interestOps(0);
            attachment.setNeighbourKey(peerKey);
            KeyStorage peerAttachment = new KeyStorage(peerKey);
            peerAttachment.setNeighbourKey(key);
            peerKey.attach(peerAttachment);
        } catch (IOException e) {
            System.out.println("could not create peer");
            proxy.getKeyCloser().close(key);
        }
    }
}
