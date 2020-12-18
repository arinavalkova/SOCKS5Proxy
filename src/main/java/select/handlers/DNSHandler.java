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
    @Override
    public void handle(SelectionKey key, Proxy proxy) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(1024);
        if (proxy.getDNSChannel().read(buf) <= 0)
            return;
        Message message = new Message(buf.array());
        Record[] records = message.getSectionArray(1);
        for (Record record : records) {
            if (record instanceof ARecord) {
                ARecord aRecord = (ARecord) record;
                int id = message.getHeader().getID();
                SelectionKey currentKey = proxy.getDNSMap().get(id);
                if (currentKey == null)
                    continue;
                KeyStorage attachment = (KeyStorage) currentKey.attachment();
                attachment.setAddress(aRecord.getAddress());
                System.out.println("dns resolved : " + aRecord.getAddress() + " " + attachment.getPort());
                proxy.getSocketChannelCreator().create(currentKey);
                return;
            }
        }
    }
}
