package select.handlers;

import proxy.Proxy;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface Handler {
    void handle(SelectionKey key, Proxy proxy) throws IOException;
}
