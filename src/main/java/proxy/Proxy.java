package proxy;

import org.xbill.DNS.ResolverConfig;
import select.SelectHandlers;
import select.functional.HeaderParser;
import select.functional.KeyCloser;
import select.functional.ResponseCreator;
import select.functional.SocketChannelCreator;
import select.handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Proxy {
    private static final int DNS_PORT = 53;

    private final Selector selector;
    private final InetSocketAddress localSocket;

    private final HashMap<Integer, SelectionKey> dnsCollection;
    private final String dnsServer;
    private DatagramChannel dnsChannel;
    private SelectionKey dnsKey;

    private final KeyCloser keyCloser;
    private final SocketChannelCreator socketChannelCreator;
    private final ResponseCreator responseCreator;
    private final HeaderParser headerParser;

    public Proxy(int port) throws IOException {
        this.selector = Selector.open();
        this.localSocket = new InetSocketAddress(port);
        this.keyCloser = new KeyCloser();
        this.socketChannelCreator = new SocketChannelCreator(this);
        this.responseCreator = new ResponseCreator();
        this.headerParser = new HeaderParser(this);
        this.dnsCollection = new HashMap<>();
        this.dnsServer = ResolverConfig.getCurrentConfig().server();
    }

    public void start() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(localSocket);
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        dnsChannel = DatagramChannel.open();
        dnsChannel.configureBlocking(false);
        dnsChannel.connect(new InetSocketAddress(dnsServer, DNS_PORT));
        dnsKey = dnsChannel.register(selector, SelectionKey.OP_READ);

        while (true) {
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                SelectHandlers handlerName = getHandlerName(key);
                if (handlerName == null) continue;
                selectHandlersMap.get(handlerName).handle(key, this);
            }
        }
    }

    public SelectHandlers getHandlerName(SelectionKey key) {
        return !key.isValid() ?
                null
                :
                key.isWritable() ?
                        SelectHandlers.WRITE
                        :
                        key.isConnectable() ?
                                SelectHandlers.CONNECT
                                :
                                key.isAcceptable() ?
                                        SelectHandlers.ACCEPT
                                        :
                                        key.isReadable() && key == dnsKey ?
                                                SelectHandlers.DNS
                                                :
                                                key.isReadable() ?
                                                        SelectHandlers.READ
                                                        :
                                                        null;
    }

    private static final Map<SelectHandlers, Handler> selectHandlersMap = Map.of(
            SelectHandlers.ACCEPT, new AcceptHandler(),
            SelectHandlers.CONNECT, new ConnectHandler(),
            SelectHandlers.DNS, new DNSHandler(),
            SelectHandlers.READ, new ReadHandler(),
            SelectHandlers.WRITE, new WriteHandler()
    );

    public Selector getSelector() {
        return selector;
    }

    public DatagramChannel getDnsChannel() {
        return dnsChannel;
    }

    public HashMap<Integer, SelectionKey> getDnsCollection() {
        return dnsCollection;
    }

    public KeyCloser getKeyCloser() {
        return keyCloser;
    }

    public SocketChannelCreator getSocketChannelCreator() {
        return socketChannelCreator;
    }

    public ResponseCreator getResponseCreator() {
        return responseCreator;
    }

    public HeaderParser getHeaderParser() {
        return headerParser;
    }
}