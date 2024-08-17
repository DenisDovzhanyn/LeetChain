package Node;

import Miner.Block;
import Node.RequestTypes.BlockListRequest;
import Node.RequestTypes.PeerListRequest;
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

    public SocketHandler(ConcurrentLinkedQueue<Block> blocksToOtherNodes, ConcurrentLinkedQueue<Block> incomingBlock) {
        this.blocksToOtherNodes = blocksToOtherNodes;
        this.incomingBlock = incomingBlock;
    }

    @Override
    public void run() {
        sockets = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

        while (sockets.getPoolSize() <= 9) {
            try {
                server = new ServerSocket(6478);
                Socket socket = server.accept();
                // after we connect we want to raise their score on our peer list file ( we will want to connect to the people with the highest scores when starting program )
                // also I just realized all these connections will only be people trying to connect with us?
                // what about when WE want to connect with people ?
                // maybe in this class first we try to connect with people then after that we open up our server and let people connect to us?

                ConcurrentLinkedQueue<BlockListRequest> blockRequests = new ConcurrentLinkedQueue<>();
                ConcurrentLinkedQueue<PeerListRequest> peerRequests = new ConcurrentLinkedQueue<>();

                SocketSendingOut outbound = new SocketSendingOut(socket, blocksToOtherNodes, transactionsToOtherNodes, blockRequests, peerRequests);
                SocketReceiving inbound = new SocketReceiving(socket, transactionsToOtherNodes, incomingBlock, blockRequests, peerRequests);

                sockets.submit(outbound);
                sockets.submit(inbound);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public int amountOfConnectedSockets() {
        return sockets.getPoolSize() / 2;
    }
}
