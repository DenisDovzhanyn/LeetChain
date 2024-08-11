package Node;

import java.net.Socket;

public class Connection implements Runnable {
    Socket connection;

    public Connection(Socket socket) {
        this.connection = socket;
    }

    @Override
    public void run() {

    }
}
