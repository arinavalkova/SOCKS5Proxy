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
    private static final int DNSPORT = 53;

    private final Selector selector;
    private final InetSocketAddress localSocket;

    private final KeyCloser keyCloser;
    private final SocketChannelCreator socketChannelCreator;
    private final ResponseCreator responseCreator;
    private final HeaderParser headerParser;


    DatagramChannel DNSChannel;
    HashMap<Integer, SelectionKey> DNSMap = new HashMap<>();
    String DNSServer = ResolverConfig.getCurrentConfig().server(); //getting address of recursive resolver
    SelectionKey DNSKey;


    public Proxy(int port) throws IOException {
        this.selector = Selector.open();
        this.localSocket = new InetSocketAddress(port);
        this.keyCloser = new KeyCloser();
        this.socketChannelCreator = new SocketChannelCreator(this);
        this.responseCreator = new ResponseCreator();
        this.headerParser = new HeaderParser(this);
    }

    public void start() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(localSocket);
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        DNSChannel = DatagramChannel.open();
        DNSChannel.configureBlocking(false);
        DNSChannel.connect(new InetSocketAddress(DNSServer, DNSPORT));
        DNSKey = DNSChannel.register(selector, SelectionKey.OP_READ);

        while (!Thread.interrupted()) {
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
                                        key.isReadable() && key == DNSKey ?
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

    public DatagramChannel getDNSChannel() {
        return DNSChannel;
    }

    public HashMap<Integer, SelectionKey> getDNSMap() {
        return DNSMap;
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