import Miner.Miner;
import Node.Node;
import Utilities.Util;
import Wallet.Wallet;
import Wallet.Transaction;
import org.junit.Test;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Main {

    public static void main(String[] args) {
        ConcurrentLinkedQueue<Transaction> linkedQueueToMiner = new ConcurrentLinkedQueue<Transaction>();

        Thread node = new Thread(new Node(linkedQueueToMiner));
        Thread miner = new Thread(new Miner(linkedQueueToMiner, Util.stringToKey("GBMTAEYGA4VIMSGOHUBACBQIFKDERTR5AMAQOA2CAACOM42GDZYYS2PZET3VHFJPQPUQ7HQZ4BHPKZIGQOXIBVA27BC64JHQDNC3ZLOAODSBK5LOAUAMD7IHYS3NBJCEMHRSCVVS3E7M5NHWOY======")));
        Thread wallet = new Thread(new Wallet(linkedQueueToMiner));
        Thread test = new Thread(new test());

        node.start();
        wallet.start();
        miner.start();
        test.start();
    }
}
