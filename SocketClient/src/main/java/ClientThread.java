import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class ClientThread {
    private String hostname;
    private int port;

    public ClientThread(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }


    public void connect() {
        try (Socket socket = new Socket(hostname, port)) {
            System.out.println("connect to server");
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (true) {
                writer.println("massage" + System.currentTimeMillis());
                System.out.println("send to server");
                String line = reader.readLine();
                System.out.println("received: " + line);
                sleep(2000);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        new ClientThread("localhost", 6789).connect();
    }
}
