package select.functional;

import org.xbill.DNS.*;
import proxy.Proxy;
import select.KeyStorage;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class HeaderParser {
    private static final byte SOCKS_VERSION = 0x05;
    private static final byte COMMAND_NOT_SUPPORTED = 0x07;
    private static final byte NO_AUTHENTICATION_METHODS = 0x00;
    private static final byte ESTABLISH_STREAM_CONNECTION = 0x01;
    private static final byte DOMAIN_NAME = 0x03;
    private static final byte FAILURE = 0x01;
    private final static byte IPV4 = 0x01;

    private final static String DOT = ".";

    private static final byte[] NO_AUTHENTICATION = new byte[] {0x05, 0x00};

    private final Proxy proxy;

    public HeaderParser(Proxy proxy) {
        this.proxy = proxy;
    }

    public void parse(KeyStorage keyStorage) {
        byte[] in = keyStorage.getInBuffer().array();
        if (!keyStorage.isConnectionEstablished()) {
            if (in[0] != SOCKS_VERSION || in[1] == NO_AUTHENTICATION_METHODS) {
                keyStorage.getOutBuffer().put(
                        proxy.getResponseSender().createResponse(keyStorage.getKey(), COMMAND_NOT_SUPPORTED)
                ).flip();
                keyStorage.setInterestOps(SelectionKey.OP_WRITE);
                return;
            }
            for (int i = 0; i < in[1]; i++) {
                if (in[i + 2] == 0) {
                    keyStorage.getOutBuffer().put(NO_AUTHENTICATION).flip();
                    keyStorage.getInBuffer().clear();
                    keyStorage.setInterestOps(SelectionKey.OP_WRITE);
                    keyStorage.setConnectionEstablished(true);
                    return;
                }
            }
        }

        if (in[0] != SOCKS_VERSION || in[1] != ESTABLISH_STREAM_CONNECTION
                || keyStorage.getInBuffer().position() < 9) {
            keyStorage.getOutBuffer().put(
                    proxy.getResponseSender().createResponse(keyStorage.getKey(), COMMAND_NOT_SUPPORTED)
            ).flip();
            keyStorage.setInterestOps(SelectionKey.OP_WRITE);
        } else {
            int port = ((0xFF & in[keyStorage.getInBuffer().position() - 2]) << 8)
                    +
                    (0xFF & in[keyStorage.getInBuffer().position() - 1]);
            if (in[3] == IPV4) {
                try {
                    InetAddress address = InetAddress.getByAddress(
                            new byte[] { in[4], in[5], in[6], in[7] }
                            );
                    proxy.getSocketChannelCreator().create(keyStorage, address, port);
                    System.out.println("Gotten destination address " + address.getHostName() + ":" + port);
                } catch (Exception e) {
                   keyStorage.getOutBuffer().put(
                           proxy.getResponseSender().createResponse(keyStorage.getKey(), FAILURE)
                   ).flip();
                    keyStorage.setInterestOps(SelectionKey.OP_WRITE);
                    return;
                }
            } else if (in[3] == DOMAIN_NAME) {
                int lengthOfName = in[4];
                char[] address = new char[lengthOfName];
                for (int i = 0; i < lengthOfName; i++)
                    address[i] = (char) in[i + 5];

                String domainName = String.valueOf(address) + DOT;
                try {
                    Name name = Name.fromString(domainName);
                    Record record = Record.newRecord(name, Type.A, DClass.IN);
                    Message message = Message.newQuery(record);
                    proxy.getDnsChannel().write(ByteBuffer.wrap(message.toWire()));
                    proxy.getDnsCollection().put(message.getHeader().getID(), keyStorage.getKey());
                } catch (IOException e) {
                    keyStorage.getOutBuffer().put(
                            proxy.getResponseSender().createResponse(keyStorage.getKey(), FAILURE)
                    ).flip();
                    keyStorage.setInterestOps(SelectionKey.OP_WRITE);
                    return;
                }
            }
            keyStorage.getInBuffer().clear();
        }
    }
}
