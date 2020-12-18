package proxy;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class KeyStorage {
    private final static int BUFF_SIZE = 4096;

    private final ByteBuffer in;
    private ByteBuffer out;
    private final SelectionKey selectionKey;
    private SelectionKey neighbourKey;
    private boolean connectionEstablished;

    private int port;
    private InetAddress address;

    public KeyStorage(SelectionKey currentKey) {
        this.selectionKey = currentKey;
        this.selectionKey.attach(this);
        this.in = ByteBuffer.allocate(BUFF_SIZE);
        this.out = ByteBuffer.allocate(BUFF_SIZE);
        this.connectionEstablished = false;
    }

    public ByteBuffer getInBuffer() {
        return in;
    }

    public SelectionKey getNeighbourKey() {
        return neighbourKey;
    }

    public SelectionKey getKey() {
        return selectionKey;
    }

    public boolean isConnectionEstablished() {
        return connectionEstablished;
    }

    public void setConnectionEstablished(boolean connectionEstablished) {
        this.connectionEstablished = connectionEstablished;
    }

    public ByteBuffer getOutBuffer() {
        return out;
    }

    public void setNeighbourKey(SelectionKey key) {
        this.neighbourKey = key;
    }

    public void setOut(ByteBuffer out) {
        this.out = out;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public InetAddress getAddress() {
        return address;
    }
}
