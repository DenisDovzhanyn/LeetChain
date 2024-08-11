package Node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class SocketHandler implements Runnable{
    ServerSocket server;
    List<Thread> sockets;
    @Override
    public void run() {
        while (true) {
            try {
                server = new ServerSocket(6478);
                sockets.add(new Thread(new Connections(server.accept())));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }



    }
}
