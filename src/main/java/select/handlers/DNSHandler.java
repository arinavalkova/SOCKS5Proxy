package select.handlers;

import org.xbill.DNS.ARecord;
import org.xbill.DNS.Message;
import org.xbill.DNS.Record;
import proxy.KeyStorage;
import proxy.Proxy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class DNSHandler implements Handler {
    private final static int BUFF_SIZE = 1024;

    @Override
    public void handle(SelectionKey key, Proxy proxy) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(BUFF_SIZE);
        if (proxy.getDnsChannel().read(buf) <= 0)
            return;
        Message message = new Message(buf.array());
        Record[] records = message.getSectionArray(1);
        for (Record record : records) {
            if (record instanceof ARecord) {
                ARecord aRecord = (ARecord) record;
                SelectionKey currentKey = proxy
                        .getDnsCollection()
                        .get(message
                                .getHeader()
                                .getID()
                        );
                if (currentKey == null)
                    continue;
                KeyStorage keyStorage = (KeyStorage) currentKey.attachment();
                keyStorage.setAddress(aRecord.getAddress());
                System.out.println("DNS Resolved : " + aRecord.getAddress() + " " + keyStorage.getPort());
                proxy.getSocketChannelCreator().create(currentKey);
                return;
            }
        }
    }
}
