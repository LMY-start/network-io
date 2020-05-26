package aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AioClient {

    public void connect() throws IOException, ExecutionException, InterruptedException {
        AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
        Future<?> future = client.connect(new InetSocketAddress(6789));
        future.get();
        while (true) {
            write(client);
            Thread.sleep(2000);
        }
    }


    private void write(AsynchronousSocketChannel client) {
        Attachment att = new Attachment();
        att.setClient(client);
        att.setReadMode(false);
        att.setBuffer(ByteBuffer.allocate(2048));
        byte[] data = ("I am " + System.currentTimeMillis()).getBytes();
        att.getBuffer().put(data);
        att.getBuffer().flip();
        // 异步发送数据到服务端
        client.write(att.getBuffer(), att, new AioClientHandler());
        System.out.println("send to server: " + new String(data));
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
        new AioClient().connect();
    }
}
