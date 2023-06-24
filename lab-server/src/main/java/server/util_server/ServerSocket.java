package server.util_server;

import common.util_common.DeSerializer;
import common.util_common.Request;
import common.util_common.Response;
import common.util_common.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class ServerSocket {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerSocket.class);
    private final int defaultPort = 3741;
    private final int selectorDelay = 100;
    private Selector selector;
    private DatagramChannel datagramChannel;
    private SocketAddress socketAddress;
    private int port = defaultPort;

    public ServerSocket(int aPort) throws IOException {
        initialization(aPort);
    }

    public ServerSocket() throws IOException {
        initialization(this.defaultPort);
    }

    private void initialization(int aPort) throws IOException {
        datagramChannel = DatagramChannel.open();
        selector = Selector.open();
        datagramChannel.socket().bind(new InetSocketAddress(aPort));
        datagramChannel.configureBlocking(false);
        datagramChannel.register(selector, SelectionKey.OP_READ);
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void stopServer() throws IOException {
        selector.close();
        datagramChannel.close();
    }

    public AddressRequest listenForRequest() throws IOException, ClassNotFoundException {
        if (selector.select(selectorDelay) == 0) {
            return null;
        }
        Set<SelectionKey> readyKeys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = readyKeys.iterator();
        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            iterator.remove();
            if (key.isReadable()) {
                int arraySize = datagramChannel.socket().getReceiveBufferSize();
                ByteBuffer packet = ByteBuffer.allocate(arraySize);
                socketAddress = datagramChannel.receive(packet);
                ((Buffer) packet).flip();
                byte[] bytes = new byte[packet.remaining()];
                packet.get(bytes);
                return new AddressRequest(DeSerializer.deSerializeRequest(bytes), socketAddress);
            }
        }
        return null;
    }

    public void sendResponse(Response response, SocketAddress socketAddress) throws IOException {
        ByteBuffer bufferToSend = Serializer.serializeResponse(response);
        datagramChannel.send(bufferToSend, socketAddress);
    }
}
