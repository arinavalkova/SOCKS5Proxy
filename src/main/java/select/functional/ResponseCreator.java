package select.functional;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ResponseCreator {

    private static final byte SOCKS_VERSION = 0x05;
    private static final byte IPv4 = 0x01;
    private static final byte GENERAL_FAILURE = 0x01;
    public byte[] createServerResponse(SelectionKey key, byte status) {
        byte[] response = new byte[10];
        response[0] = SOCKS_VERSION;
        response[1] = status;
        response[3] = IPv4;
        SocketChannel socketChannel = (SocketChannel) key.channel();

        try {
            String address = ((InetSocketAddress) socketChannel.getRemoteAddress()).getAddress().getHostAddress();
            int port = ((InetSocketAddress) socketChannel.getRemoteAddress()).getPort();
            String[] strs = address.split("\\.");
            for (int i = 4; i < 8; ++i)
                response[i] = (byte) (Integer.parseInt(strs[i - 4]));
            response[8] = (byte) (port >> 8); //big endian
            response[9] = (byte) (port & 0xFF);
        } catch (Exception e) {
            System.out.println("could not get socket inetAddr");
            response[1] = GENERAL_FAILURE;
        }
        return response;
    }
}
