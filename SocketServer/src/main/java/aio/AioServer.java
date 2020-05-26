package aio;

import lombok.SneakyThrows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AioServer implements Runnable {
    private int port;

    public AioServer(int port) {
        this.port = port;
    }

    @SneakyThrows
    @Override
    public void run() {
        // 实例化，并监听端口
        try {
            AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(port));
            // 自己定义 aio.Attachment 类，用于传递一些信息
            Attachment attachment = new Attachment();
            attachment.setServer(server);
            server.accept(attachment, new CompletionHandler<AsynchronousSocketChannel, Attachment>() {
                @Override
                public void completed(AsynchronousSocketChannel client, Attachment att) {
                    try {
                        SocketAddress clientAddr = client.getRemoteAddress();
                        System.out.println("accept a new connect：" + clientAddr);
                        //收到新的连接后，server 应该重新调用 accept 方法等待新的连接进来
                        att.getServer().accept(att, this);
                        read(client);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void failed(Throwable t, Attachment att) {
                    System.out.println("accept failed");
                }
            });
        } catch (
                IOException e) {
            e.printStackTrace();
        }

        // 为了防止 main 线程退出
        try {
            Thread.currentThread().join();
        } catch (
                InterruptedException e) {
        }
    }

    private void read(AsynchronousSocketChannel client) {
        Attachment newAtt = new Attachment();
        newAtt.setClient(client);
        newAtt.setReadMode(true);
        newAtt.setBuffer(ByteBuffer.allocate(2048));
        client.read(newAtt.getBuffer(), newAtt, new ChannelHandler());
    }


}
