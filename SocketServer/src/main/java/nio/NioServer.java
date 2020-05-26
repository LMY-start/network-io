package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class NioServer implements Runnable {
    private int port;
    ServerSocketChannel serverSocketChannel = null;
    Selector selector = null;

    public NioServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            //设置连接模式为非阻塞模式
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            //在selector上注册通道，监听连接事件
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                //设置selector 每隔一秒扫描所有channel
                int readyChannels = selector.select(1000);
                if (readyChannels == 0) {
                    continue;
                }
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterable = selectionKeys.iterator();
                SelectionKey key = null;
                while (iterable.hasNext()) {
                    key = iterable.next();
                    iterable.remove();
                    //对key进行处理
                    try {
                        handlerKey(key, selector);
                    } catch (Exception e) {
                        if (null != key) {
                            key.cancel();
                            if (null != key.channel()) {
                                key.channel().close();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != selector) {
                    selector.close();
                }
                if (null != serverSocketChannel) {
                    serverSocketChannel.close();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handlerKey(SelectionKey key, Selector selector) throws IOException {
        if (key.isValid()) {
            //判断是否是连接请求，对所有连接请求进行处理
            if (key.isAcceptable()) {
                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                SocketChannel socketChannel = serverSocketChannel.accept();
                socketChannel.configureBlocking(false);
                //在selector上注册通道，监听读事件
                socketChannel.register(selector, SelectionKey.OP_READ);
                System.out.println("accept: " + socketChannel.socket().getPort());
            } else if (key.isReadable()) {
                SocketChannel socketChannel = (SocketChannel) key.channel();
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                int readBytes = socketChannel.read(byteBuffer);
                if (readBytes > 0) {
                    //从写模式切换到读模式
                    byteBuffer.flip();
                    byte[] bytes = new byte[byteBuffer.remaining()];
                    byteBuffer.get(bytes);
                    String message = new String(bytes, StandardCharsets.UTF_8);
                    System.out.println("received: " + message + " from " + socketChannel.socket().getPort());
                    message = "answer: " + message;
                    write(socketChannel, message);
                }  else if (readBytes == -1) {
                    // -1 代表连接已经关闭
                    socketChannel.close();
                }
            }
        }
    }

    private void write(SocketChannel channel, String message) throws IOException {
        byte[] responseByte = message.getBytes();
        ByteBuffer writeBuffer = ByteBuffer.allocate(responseByte.length);
        writeBuffer.put(responseByte);
        writeBuffer.flip();
        channel.write(writeBuffer);
    }
}
