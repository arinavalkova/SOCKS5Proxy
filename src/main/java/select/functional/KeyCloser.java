package select.functional;

import proxy.KeyStorage;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public class KeyCloser {
    public void close(SelectionKey key) throws IOException {
        key.cancel();
        key.channel().close();
        if (!key.isValid())
            return;
        if (((KeyStorage) key.attachment()).getNeighbourKey() != null) {
            SelectionKey neighbourKey = ((KeyStorage) key.attachment()).getNeighbourKey();
            ((KeyStorage) key.attachment()).setNeighbourKey(null);
            neighbourKey.cancel();
            neighbourKey.channel().close();
        }
    }
}
