package select.handlers;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface SelectHandler {
    void start(SelectionKey currentKey) throws IOException;
}
