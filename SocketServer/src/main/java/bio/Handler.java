package bio;

import java.io.*;
import java.net.Socket;

public class Handler implements Runnable {
    Socket socket;

    public Handler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            String text;
            while ((text = reader.readLine()) != null) {
                System.out.println(Thread.currentThread().getName()+" received: "+text);
                String reverseText = new StringBuilder(text).reverse().toString();
                writer.println("Server: " + reverseText);
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
