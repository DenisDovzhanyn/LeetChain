import Miner.Miner;
import Node.Node;
import Utilities.Util;
import Wallet.Wallet;
import Wallet.Transaction;
import Miner.Block;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Main {

    public static void main(String[] args) {
        ConcurrentLinkedQueue<Transaction> transactionsToMiners = new ConcurrentLinkedQueue<Transaction>();
        ConcurrentLinkedQueue<Block> blocksToNode = new ConcurrentLinkedQueue<Block>();

        Thread node = new Thread(new Node(transactionsToMiners, blocksToNode));
        Thread miner = new Thread(new Miner(transactionsToMiners, blocksToNode, Util.stringToKey("GBMTAEYGA4VIMSGOHUBACBQIFKDERTR5AMAQOA2CAACALXT4KB7T57BCLNHKMKP4IAYXWW4OEB6MM57QIQATKTSDDIUBMBBARJ2RERXNTAS6KJ7JPORGTXLVY5Q7PVOMW4KK3E4KZ4UGHAU6MY======")));
        Thread wallet = new Thread(new Wallet(transactionsToMiners));
        Thread test = new Thread(new test());

        node.start();
        wallet.start();
        miner.start();
        test.start();
    }
}
