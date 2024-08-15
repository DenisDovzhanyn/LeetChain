package Node;

import Miner.Block;
import Wallet.Transaction;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class SocketHandler implements Runnable{
    ServerSocket server;
    ThreadPoolExecutor sockets;
    ConcurrentLinkedQueue<Block> blocksToOtherNodes;
    ConcurrentLinkedQueue<Transaction> transactionsToOtherNodes;
    ConcurrentLinkedQueue<Block> incomingBlock;

    public SocketHandler(ConcurrentLinkedQueue<Block> blocksToOtherNodes) {
        this.blocksToOtherNodes = blocksToOtherNodes;
    }

    @Override
    public void run() {
        sockets = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

        while (sockets.getPoolSize() < 9) {
            try {
                server = new ServerSocket(6478);
                Socket socket = server.accept();
                SocketSendingOut outbound = new SocketSendingOut(socket, blocksToOtherNodes, transactionsToOtherNodes);
                // this is weird fix this
                SocketReceiving inbound = new SocketReceiving(socket, transactionsToOtherNodes, incomingBlock);

                sockets.submit(outbound);
                sockets.submit(inbound);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public int amountOfConnectedSockets() {
        return sockets.getPoolSize();
    }
}
