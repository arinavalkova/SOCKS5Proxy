package select.functional;

import org.xbill.DNS.*;
import proxy.KeyStorage;
import proxy.Proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class HeaderParser {
    private final static byte SOCKS_VERSION = 0x05;
    private final static byte COMMAND_NOT_SUPPORTED = 0x07;
    private final static byte ESTABLISH_STREAM_CONNECTION = 0x01;
    private final static byte IPV4 = 0x01;
    private final static byte DOMAIN_NAME = 0x03;
    private final static byte GENERAL_FAILURE = 0x01;
    private final static byte NO_AUTHENTICATION_METHODS_SUPPORTED = 0x00;
    private final static byte NO_AUTHENTICATION = 0x00;
    private final static String DOT = ".";

    private final Proxy proxy;

    public HeaderParser(Proxy proxy) {
        this.proxy = proxy;
    }

    public void parse(SelectionKey key) {
        KeyStorage keyStorage = (KeyStorage) key.attachment();
        byte[] in = keyStorage.getInBuffer().array();
        if (!keyStorage.isConnectionEstablished()) {
            if (in[0] != SOCKS_VERSION || in[1] == NO_AUTHENTICATION_METHODS_SUPPORTED) {
                keyStorage.getOutBuffer().put(
                        proxy.getResponseCreator().createServerResponse(key, COMMAND_NOT_SUPPORTED)
                ).flip();
                key.interestOps(SelectionKey.OP_WRITE);
                return;
            }
            for (int i = 0; i < in[1]; i++) {
                if (in[i + 2] == 0) {
                    keyStorage.getOutBuffer().put(
                            new byte[] { SOCKS_VERSION, NO_AUTHENTICATION }
                            ).flip();
                    keyStorage.getInBuffer().clear();
                    key.interestOps(SelectionKey.OP_WRITE);
                    keyStorage.setConnectionEstablished(true);
                    return;
                }
            }
        }

        if (in[0] != SOCKS_VERSION ||
                in[1] != ESTABLISH_STREAM_CONNECTION
                || keyStorage.getInBuffer().position() < 9) {
            keyStorage.getOutBuffer().put(
                    proxy.getResponseCreator().createServerResponse(key, COMMAND_NOT_SUPPORTED)
            ).flip();
            key.interestOps(SelectionKey.OP_WRITE);
            return;
        }

        int port =
                ((0xFF & in[keyStorage.getInBuffer().position() - 2]) << 8)
                        +
                        (0xFF & in[keyStorage.getInBuffer().position() - 1]);
        keyStorage.setPort(port);

        if (in[3] == IPV4) {
            try {
                InetAddress addr = InetAddress.getByAddress(new byte[]{ in[4], in[5], in[6], in[7] });
                keyStorage.setAddress(addr);
                proxy.getSocketChannelCreator().create(key);
                System.out.println("Redirected to address" + addr.getHostName() + ":" + port);
            } catch (Exception e) {
                key.interestOps(SelectionKey.OP_WRITE);
                return;
            }
        } else if (in[3] == DOMAIN_NAME) {
            char[] address = new char[in[4]];
            for (int i = 0; i < in[4]; i++)
                address[i] = (char) in[i + 5];

            String domainName = String.valueOf(address) + DOT;
            try {
                Message message = Message.newQuery(
                        Record.newRecord(
                                Name.fromString(domainName), Type.A, DClass.IN)
                );
                proxy.getDnsChannel().write(ByteBuffer.wrap(message.toWire()));
                proxy.getDnsCollection().put(message.getHeader().getID(), key);
            } catch (IOException e) {
                keyStorage.getOutBuffer().put(
                        proxy.getResponseCreator().createServerResponse(key, GENERAL_FAILURE)
                ).flip();
                key.interestOps(SelectionKey.OP_WRITE);
                return;
            }
        } else {
            key.interestOps(SelectionKey.OP_WRITE);
            return;
        }
        keyStorage.getInBuffer().clear();
    }
}
