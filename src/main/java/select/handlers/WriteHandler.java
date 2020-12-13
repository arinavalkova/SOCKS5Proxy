package select.handlers;

import proxy.Proxy;

import java.nio.channels.SelectionKey;

public class WriteHandler implements SelectHandler {
    private final Proxy proxy;
    public WriteHandler(Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public void start(SelectionKey currentKey) {

    }
}
