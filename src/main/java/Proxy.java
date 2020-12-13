import org.xbill.DNS.ResolverConfig;
import select.handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

public class Proxy {
    private final static int DNS_PORT = 53;

    private final int port;
    private final Selector selector;

    public Proxy(int port) throws IOException {
        this.port = port;
        this.selector = Selector.open();
    }

    public void start() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector,
                SelectionKey.OP_ACCEPT |
                        SelectionKey.OP_CONNECT |
                        SelectionKey.OP_READ |
                        SelectionKey.OP_WRITE
        );

        DatagramChannel dnsChannel = DatagramChannel.open();
        dnsChannel.configureBlocking(false);
        String DNSServer = ResolverConfig.getCurrentConfig().server();
        dnsChannel.connect(new InetSocketAddress(DNSServer, DNS_PORT));
        SelectionKey dnsKey = dnsChannel.register(selector, SelectionKey.OP_READ);

        while (true) {
            selector.select();
            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()) {
                SelectionKey currentKey = keyIterator.next();

                if (!currentKey.isValid()) {
                    keyIterator.remove();
                    continue;
                }

                if (currentKey.isReadable() && currentKey == dnsKey) {
                    new DNSHandler().start(currentKey);
                    continue;
                }

                if (currentKey.isAcceptable()) {
                    new AcceptHandler().start(currentKey);
                }

                if (currentKey.isConnectable()) {
                    new ConnectHandler().start(currentKey);
                }

                if (currentKey.isReadable()) {
                    new ReadHandler().start(currentKey);
                }

                if (currentKey.isWritable()) {
                    new WriteHandler().start(currentKey);
                }

                keyIterator.remove();
            }
        }
    }
}
