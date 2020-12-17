package select.functional;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ResponseSender {
    private final static int RESPONSE_SIZE = 10;
    private final static byte SOCKS_VERSION = 0x05;
    private static final byte FAILURE = 0x01;
    private final static byte IPV4 = 0x01;
    private final String ADDRESS_REGEX = "\\.";

    public byte[] createResponse(SelectionKey key, byte state) {
        String address;
        int port;
        byte[] serverResponse = new byte[RESPONSE_SIZE];

        serverResponse[0] = SOCKS_VERSION;
        serverResponse[1] = state;
        serverResponse[3] = IPV4;
        SocketChannel socketChannel = (SocketChannel) key.channel();

        try {
            address = ((InetSocketAddress) socketChannel
                    .getRemoteAddress())
                    .getAddress()
                    .getHostAddress();
            port = ((InetSocketAddress) socketChannel
                    .getRemoteAddress())
                    .getPort();
            String[] strs = address.split(ADDRESS_REGEX);
            for (int i = 4; i < 8; ++i)
                serverResponse[i] = (byte) (Integer.parseInt(strs[i - 4]));
            serverResponse[8] = (byte) (port >> 8);
            serverResponse[9] = (byte) (port & 0xFF);
        } catch (Exception e) {
            serverResponse[1] = FAILURE;
        }
        return serverResponse;
    }
}
