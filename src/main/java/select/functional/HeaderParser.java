package select.functional;

import org.xbill.DNS.*;
import proxy.KeyStorage;
import proxy.Proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class HeaderParser {
    private static final byte SOCKS_VERSION = 0x05;
    private static final byte COMMAND_NOT_SUPPORTED = 0x07;
    private static final byte[] NO_AUTHENTICATION = new byte[]{ 0x05, 0x00 };
    private static final byte ESTABLISH_STREAM_CONNECTION = 0x01;
    private static final byte IPv4 = 0x01;
    private static final byte DOMAIN_NAME = 0x03;
    private static final byte GENERAL_FAILURE = 0x01;

    private final Proxy proxy;

    public HeaderParser(Proxy proxy) {
        this.proxy = proxy;
    }

    public void parse(SelectionKey key) {
        KeyStorage attachment = (KeyStorage) key.attachment();
        byte[] in = attachment.getInBuffer().array();
        if (!attachment.isConnectionEstablished()) {
            if (in[0] != SOCKS_VERSION || in[1] == 0x00) { //no authentication methods supported
                attachment.getOutBuffer().put(proxy.getResponseCreator().createServerResponse(key, COMMAND_NOT_SUPPORTED)).flip();
                key.interestOps(SelectionKey.OP_WRITE);
                return;
            }
            for (int i = 0; i < in[1]; ++i) {
                if (in[i + 2] == 0) {
                    attachment.getOutBuffer().put(NO_AUTHENTICATION).flip();
                    attachment.getInBuffer().clear();
                    key.interestOps(SelectionKey.OP_WRITE);
                    attachment.setConnectionEstablished(true);
                    return;
                }
            }
        }

        if (in[0] != SOCKS_VERSION || in[1] != ESTABLISH_STREAM_CONNECTION || attachment.getInBuffer().position() < 9) {
            attachment.getOutBuffer().put(proxy.getResponseCreator().createServerResponse(key, COMMAND_NOT_SUPPORTED)).flip();
            key.interestOps(SelectionKey.OP_WRITE);
        } else {
            int port = ((0xFF & in[attachment.getInBuffer().position() - 2]) << 8) + (0xFF & in[attachment.getInBuffer().position() - 1]);
            attachment.setPort(port);
            if (in[3] == IPv4) {
                byte[] address = new byte[]{in[4], in[5], in[6], in[7]};
                try {
                    InetAddress addr = InetAddress.getByAddress(address);
                    attachment.setAddress(addr);
                    proxy.getSocketChannelCreator().create(key);
                    System.out.println("redirected to " + addr.getHostName() + ":" + port);
                } catch (Exception e) {
                    key.interestOps(SelectionKey.OP_WRITE);
                    return;
                }
            } else if (in[3] == DOMAIN_NAME) {
                int nameLength = in[4];
                char[] address = new char[nameLength];
                for (int i = 0; i < nameLength; ++i)
                    address[i] = (char) in[i + 5];

                String domainName = String.valueOf(address) + ".";
                try {
                    Name name = Name.fromString(domainName);
                    Record record = Record.newRecord(name, Type.A, DClass.IN);
                    Message message = Message.newQuery(record);
                    proxy.getDNSChannel().write(ByteBuffer.wrap(message.toWire()));
                    proxy.getDNSMap().put(message.getHeader().getID(), key);
                } catch (IOException e) {
                    attachment.getOutBuffer().put(proxy.getResponseCreator().createServerResponse(key, GENERAL_FAILURE)).flip();
                    key.interestOps(SelectionKey.OP_WRITE);
                    return;
                }
            } else {
                key.interestOps(SelectionKey.OP_WRITE);
                return;
            }
            attachment.getInBuffer().clear();
        }
    }
}
