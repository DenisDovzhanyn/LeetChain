package Node;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class SocketHandler implements Runnable{
    ServerSocket server;
    ThreadPoolExecutor sockets;

    @Override
    public void run() {
        sockets = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
        while (true) {
            try {
                server = new ServerSocket(6478);
                sockets.submit(new Thread(new Connection(server.accept())));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }



    }
}
