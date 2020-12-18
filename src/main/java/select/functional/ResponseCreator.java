package select.functional;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ResponseCreator {
    private final static String ADDRESS_REG = "\\.";
    private static final int RESPONSE_LEN = 10;
    private static final byte SOCKS_VERSION = 0x05;
    private static final byte IPv4 = 0x01;
    private static final byte FAILURE = 0x01;

    public byte[] createServerResponse(SelectionKey key, byte state) {
        byte[] response = new byte[RESPONSE_LEN];
        response[0] = SOCKS_VERSION;
        response[1] = state;
        response[3] = IPv4;
        SocketChannel socketChannel = (SocketChannel) key.channel();
        try {
            int port = ((InetSocketAddress) socketChannel.getRemoteAddress()).getPort();
            String[] splitAddress = (
                    (InetSocketAddress) socketChannel
                    .getRemoteAddress())
                    .getAddress()
                    .getHostAddress()
                    .split(ADDRESS_REG);
            for (int i = 4; i < 8; ++i)
                response[i] = (byte) (Integer.parseInt(splitAddress[i - 4]));
            response[8] = (byte) (port >> 8);
            response[9] = (byte) (port & 0xFF);
        } catch (Exception e) {
            response[1] = FAILURE;
        }
        return response;
    }
}
