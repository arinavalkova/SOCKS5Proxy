package proxy;

import org.xbill.DNS.ResolverConfig;
import select.functional.HeaderParser;
import select.functional.KeyCloser;
import select.functional.ResponseSender;
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

    private final KeyCloser keyCloser;
    private final HeaderParser headerParser;
    private final ResponseSender responseSender;

    public Proxy(int port) throws IOException {
        this.port = port;
        this.selector = Selector.open();
        this.keyCloser = new KeyCloser();
        this.headerParser = new HeaderParser(this);
        this.responseSender = new ResponseSender();
    }

    public void start() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

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

                keyIterator.remove();
                if (!currentKey.isValid()) {
                    continue;
                }

                if (currentKey.isReadable() && currentKey == dnsKey) {
                    new DNSHandler(this).start(currentKey);
                    continue;
                }

                if (currentKey.isAcceptable()) {
                    new AcceptHandler(this).start(currentKey);
                    continue;
                }

                if (currentKey.isConnectable()) {
                    new ConnectHandler(this).start(currentKey);
                    continue;
                }

                if (currentKey.isReadable()) {
                    new ReadHandler(this).start(currentKey);
                }

                if (currentKey.isWritable()) {
                    new WriteHandler(this).start(currentKey);
                }
            }
        }
    }

    public Selector getSelector() {
        return selector;
    }

    public KeyCloser getKeyCloser() {
        return keyCloser;
    }

    public HeaderParser getHeaderParser() {
        return headerParser;
    }

    public ResponseSender getResponseSender() {
        return responseSender;
    }
}
