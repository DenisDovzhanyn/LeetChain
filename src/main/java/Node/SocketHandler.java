package Node;

import Miner.Block;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class SocketHandler implements Runnable{
    ServerSocket server;
    ThreadPoolExecutor sockets;
    ConcurrentLinkedQueue<Block> blocksToOtherNodes;

    public SocketHandler(ConcurrentLinkedQueue<Block> blocksToOtherNodes) {
        this.blocksToOtherNodes = blocksToOtherNodes;
    }

    @Override
    public void run() {
        sockets = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

        while (sockets.getPoolSize() < 4) {
            try {
                server = new ServerSocket(6478);
                sockets.submit(new Thread(new Connection(server.accept(), blocksToOtherNodes)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public int amountOfConnectedSockets() {
        return sockets.getPoolSize();
    }
}
