import Miner.Miner;
import Node.Node;
import Wallet.Wallet;
import Wallet.Transaction;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Main {

    public static void main(String[] args) {
        ConcurrentLinkedQueue<Transaction> linkedQueueToMiner = new ConcurrentLinkedQueue<Transaction>();

        Thread miner = new Thread(new Miner(linkedQueueToMiner));
        Thread wallet = new Thread(new Wallet(linkedQueueToMiner));
        Thread node = new Thread(new Node(linkedQueueToMiner));

        node.start();
        wallet.start();
        miner.start();


    }
}
