package select.handlers;

import proxy.Proxy;

import java.nio.channels.SelectionKey;

public class DNSHandler implements SelectHandler {
    private final Proxy proxy;
    public DNSHandler(Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public void start(SelectionKey currentKey) {

    }
}
