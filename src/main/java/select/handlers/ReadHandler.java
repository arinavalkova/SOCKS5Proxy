package select.handlers;

import proxy.Proxy;

import java.nio.channels.SelectionKey;

public class ReadHandler implements SelectHandler {
    private final Proxy proxy;
    public ReadHandler(Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public void start(SelectionKey currentKey) {

    }
}
