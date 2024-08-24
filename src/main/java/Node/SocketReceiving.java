package Node;

import Miner.Block;
import Node.MessageTypes.BlockListRequest;
import Node.MessageTypes.PeerListRequest;
import Wallet.Transaction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SocketReceiving implements Runnable {
    Socket receiving;
    ConcurrentLinkedQueue<Transaction> incomingTransactions;
    ConcurrentLinkedQueue<Block> incomingBlocks;
    ConcurrentLinkedQueue<BlockListRequest> blockRequest;
    ConcurrentLinkedQueue<PeerListRequest> peerListRequest;

    public SocketReceiving(Socket receiving,ConcurrentLinkedQueue<Transaction> incomingTransactions, ConcurrentLinkedQueue<Block> incomingBlocks,
                           ConcurrentLinkedQueue<BlockListRequest> blockRequest, ConcurrentLinkedQueue<PeerListRequest> peerRequests) {
        this.receiving = receiving;
        this.incomingTransactions = incomingTransactions;
        this.incomingBlocks = incomingBlocks;
        this.blockRequest = blockRequest;
        this.peerListRequest = peerRequests;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(receiving.getInputStream());
            while (true) {
                Object object = inputStream.readObject();

                // from testing in a different repository, when an object is received and then the connection closes
                // this will throw an error " connnection reset", upon getting this error we should close this thread ( including the sending out thread aswell )
                // how will we handle this exception and close this thread ??????
                // even so if we throw an exception to close this thread, how will we close the sending out thread?
                // Should I just let it die on its own when it tries to send out info?

                if (object instanceof BlockListRequest) {
                    BlockListRequest request = (BlockListRequest) object;
                    blockRequest.add(request);
                }
                if (object instanceof PeerListRequest) {
                    PeerListRequest request = (PeerListRequest) object;
                    peerListRequest.add(request);
                }
                if (object instanceof Block) {
                    incomingBlocks.add((Block) object);
                }
                if (object instanceof Transaction) {
                    incomingTransactions.add((Transaction) object);
                }
                if (object instanceof Peer) {
                    SocketHandler.peers.add((Peer) object);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("object not formatted correctly or connection terminated");
        }
    }
}
