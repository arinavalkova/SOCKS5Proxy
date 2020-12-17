package select.handlers;

import org.xbill.DNS.ARecord;
import org.xbill.DNS.Message;
import org.xbill.DNS.Record;
import proxy.Proxy;
import select.KeyStorage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class DNSHandler implements SelectHandler {
    private final static int BUFF_SIZE = 1024;

    private final Proxy proxy;
    public DNSHandler(Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public void start(SelectionKey currentKey) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(BUFF_SIZE);
        if (proxy.getDnsChannel().read(buf) <= 0)
            return;
        Message message = new Message(buf.array());
        Record[] records = message.getSectionArray(1);
        for (Record record : records) {
            if (record instanceof ARecord) {
                ARecord aRecord = (ARecord) record;
                int id = message.getHeader().getID();
                SelectionKey key = proxy.getDnsCollection().get(id);
                if (key == null)
                    continue;
                KeyStorage keyStorage = (KeyStorage) key.attachment();
                keyStorage.setAddress(aRecord.getAddress());
                System.out.println("DNS resolved : " + aRecord.getAddress() + " " + keyStorage.getPort());
                proxy.getSocketChannelCreator().create(keyStorage);
                return;
            }
        }
    }
}
