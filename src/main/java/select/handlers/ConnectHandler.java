package select.handlers;

import proxy.Proxy;

import java.nio.channels.SelectionKey;

public class ConnectHandler implements SelectHandler {
    private final Proxy proxy;
    public ConnectHandler(Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public void start(SelectionKey currentKey) {

    }
}
