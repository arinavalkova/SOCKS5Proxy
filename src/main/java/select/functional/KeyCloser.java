package select.functional;

import select.KeyStorage;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public class KeyCloser {
    public void close(KeyStorage keyStorage) {
        try {
            keyStorage.getKey().cancel();
            keyStorage.getKey().channel().close();
            if (!keyStorage.getKey().isValid())
                return;
            if (keyStorage.getNeighbourStorage() != null) {
                SelectionKey neighbourKey = keyStorage.getNeighbourStorage().getKey();
                keyStorage.setNeighbourStorage(null);
                neighbourKey.cancel();
                neighbourKey.channel().close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
