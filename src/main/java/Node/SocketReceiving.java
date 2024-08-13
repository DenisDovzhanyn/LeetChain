package Node;

import Miner.Block;
import Wallet.Transaction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SocketReceiving implements Runnable {
    Socket receiving;
    ConcurrentLinkedQueue<Transaction> incomingTransactions;
    ConcurrentLinkedQueue<Block> incomingBlocks;

    public SocketReceiving(Socket receiving,ConcurrentLinkedQueue<Transaction> incomingTransactions, ConcurrentLinkedQueue<Block> incomingBlocks) {
        this.receiving = receiving;
        this.incomingTransactions = incomingTransactions;
        this.incomingBlocks = incomingBlocks;
    }

    @Override
    public void run() {
        try {
            while (true) {
                ObjectInputStream inputStream = new ObjectInputStream(receiving.getInputStream());
                Object object = inputStream.readObject();

                if (object instanceof Block) {
                    incomingBlocks.add((Block) object);
                }
                else if (object instanceof Transaction) {
                    incomingTransactions.add((Transaction) object);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
