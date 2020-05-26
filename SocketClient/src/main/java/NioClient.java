import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

import static java.lang.Thread.sleep;

public class NioClient {
    private String host;
    private int port;
    SocketChannel channel = null;
    Selector selector = null;

    public NioClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() {
        try {
            selector = Selector.open();
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_CONNECT);
            if (channel.connect(new InetSocketAddress(host, port))) {
                channel.register(selector, SelectionKey.OP_READ);
                write(channel);
            } else {

            }
            while (true) {
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                SelectionKey key = null;
                while (iterator.hasNext()) {
                    try {
                        key = iterator.next();
                        handle(key, selector);
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (null != key) {
                            key.cancel();
                            if (null != key.channel()) {
                                key.channel().close();
                            }
                        }
                        return;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("will exit");
            e.printStackTrace();
        } finally {
            try {
                if (null != channel) {
                    channel.close();
                }
                if (null != selector) {
                    selector.close();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void write(SocketChannel channel) throws IOException, InterruptedException {
        sleep(2000);
        String message = "message" + System.currentTimeMillis();
        byte[] bytes = message.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
        byteBuffer.put(bytes);
        byteBuffer.flip();
        channel.write(byteBuffer);
        System.out.println("send: " + message);
    }

    private void handle(SelectionKey key, Selector selector) throws IOException, InterruptedException {
        if (key.isValid()) {
            SocketChannel channel = (SocketChannel) key.channel();
            if (key.isConnectable() && !(((SocketChannel) key.channel()).isConnected())) {
                channel.finishConnect();
                channel.register(selector, SelectionKey.OP_READ);
                System.out.println("connect to server");
                write(channel);
            }

            if (key.isReadable()) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                int readBytes = channel.read(byteBuffer);
                if (readBytes > 0) {
                    byteBuffer.flip();
                    byte[] bytes = new byte[byteBuffer.remaining()];
                    byteBuffer.get(bytes);
                    String message = new String(bytes, StandardCharsets.UTF_8);
                    System.out.println("received: " + message);
                    write(channel);
                }
            }
        }
    }


    public static void main(String[] args) {
        new NioClient("127.0.0.1", 6789).connect();
    }
}
