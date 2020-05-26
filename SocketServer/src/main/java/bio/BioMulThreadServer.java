package bio;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BioMulThreadServer implements Runnable {

    private int port;

    public BioMulThreadServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);
            int cpuNum = Runtime.getRuntime().availableProcessors();
            ThreadPoolExecutor socketPool = new ThreadPoolExecutor(cpuNum, cpuNum * 2, 1000,
                    TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000));

            while (true) {
                Socket socket = serverSocket.accept();
                socketPool.submit(new Handler(socket));
            }
        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

