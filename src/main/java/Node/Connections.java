package Node;

import java.net.Socket;

public class Connections implements Runnable {
    Socket connection;

    public Connections(Socket socket) {
        this.connection = socket;
    }

    @Override
    public void run() {

    }
}
