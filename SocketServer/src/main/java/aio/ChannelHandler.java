package aio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ChannelHandler implements CompletionHandler<Integer, Attachment> {

    @Override
    public void completed(Integer result, Attachment attachment) {
        if (attachment.isReadMode()) {
            // 读取来自客户端的数据
            ByteBuffer buffer = attachment.getBuffer();
            buffer.flip();
            byte[] bytes = new byte[buffer.limit()];
            buffer.get(bytes);
            String msg = new String(bytes).trim();
            try {
                System.out.println("read from client " + attachment.getClient().getRemoteAddress() + ": " + msg);

                // 响应客户端请求，返回数据
                write(attachment, buffer, msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            attachment.setReadMode(true);
            attachment.getBuffer().clear();
            attachment.getClient().read(attachment.getBuffer(), attachment, this);
        }
    }

    @Override
    public void failed(Throwable t, Attachment att) {
        System.out.println("disconnected");
    }


    private void write(Attachment attachment, ByteBuffer buffer, String msg) throws IOException {
        String response = "response for " + msg;
        byte[] responseByte = response.getBytes(StandardCharsets.UTF_8);
        buffer.clear();
        buffer.put(responseByte);
        attachment.setReadMode(false);
        buffer.flip();
        attachment.getClient().write(buffer, attachment, this);
        System.out.println("response to client" + attachment.getClient().getRemoteAddress());
    }
}