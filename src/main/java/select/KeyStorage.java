package select;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Map;

public class KeyStorage {
    private final static int BUFF_SIZE = 4096;
    private ByteBuffer in;
    private ByteBuffer out;
    private SelectionKey selectionKey;
    private KeyStorage neighbourStorage;
    private boolean connectionEstablished;

    private InetAddress address;
    private int port;

    public KeyStorage(SelectionKey currentKey) {
        this.in = ByteBuffer.allocate(BUFF_SIZE);
        this.out = ByteBuffer.allocate(BUFF_SIZE);
        this.selectionKey.attach(this);
        this.connectionEstablished = false;
    }

    public ByteBuffer getInBuffer() {
        return in;
    }

    public KeyStorage getNeighbourStorage() {
        return neighbourStorage;
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

    public void setInterestOps(int opWrite) {
        selectionKey.interestOps(opWrite);
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
