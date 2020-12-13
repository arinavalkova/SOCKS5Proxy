package select.handlers;

import java.nio.channels.SelectionKey;

public interface SelectHandler {
    void start(SelectionKey currentKey);
}
