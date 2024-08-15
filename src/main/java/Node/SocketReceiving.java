package Node;

import Miner.Block;
import Node.RequestTypes.BlockListRequest;
import Node.RequestTypes.PeerListRequest;
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

    public SocketReceiving(Socket receiving,ConcurrentLinkedQueue<Transaction> incomingTransactions, ConcurrentLinkedQueue<Block> incomingBlocks) {
        this.receiving = receiving;
        this.incomingTransactions = incomingTransactions;
        this.incomingBlocks = incomingBlocks;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(receiving.getInputStream());
            while (true) {
                Object object = inputStream.readObject();

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
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("error receiving/formatting data");
        }
    }
}
