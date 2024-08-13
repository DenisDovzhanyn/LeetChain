package Node;

import Miner.Block;
import Wallet.Transaction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SocketSendingOut implements Runnable{
    Socket socketOut;
    ConcurrentLinkedQueue<Block> blocksToOtherNodes;
    ConcurrentLinkedQueue<Transaction> transactionsToOtherNodes;

    public SocketSendingOut(Socket outBound, ConcurrentLinkedQueue<Block> blocksToOtherNodes, ConcurrentLinkedQueue<Transaction> transactionsToOtherNodes) {
        this.socketOut = outBound;
        this.blocksToOtherNodes = blocksToOtherNodes;
        this.transactionsToOtherNodes = transactionsToOtherNodes;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream outBound = new ObjectOutputStream(socketOut.getOutputStream());

            while (true) {
                if (!blocksToOtherNodes.isEmpty()) {
                    outBound.writeObject(blocksToOtherNodes.poll());
                }
                if(!transactionsToOtherNodes.isEmpty()) {
                    outBound.writeObject(transactionsToOtherNodes.poll());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
