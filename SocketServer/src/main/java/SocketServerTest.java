import aio.AioServer;
import bio.BioSingleThreadServer;
import nio.NioServer;

import java.io.IOException;

public class SocketServerTest {

    public static void main(String[] args) throws IOException {
//          BioSingleThreadServer target = new BioSingleThreadServer(6789);
//          bio.BioMulThreadServer target = new bio.BioMulThreadServer(6789);
        NioServer target = new NioServer(6789);
//        AioServer target = new AioServer(6789);
        new Thread(target, "ServerThread").start();
    }
}
