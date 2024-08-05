import Miner.Block;
import Node.Ledger;
import Utilities.Util;
import Wallet.Wallet;
import Wallet.TransactionType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import Wallet.Transaction;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LedgerTest {
    Block block;

    // set up
        @Before
    public void setUpBlock(){
         // opening database
         Ledger.getInstance();
         // creates new block for us to work with
         block = new Block("prev", 100000, 100000);
    }

    @Test
    public void doesLedgerAddAndGetAndDeleteBlock(){

        Ledger.getInstance().addBlock(block, "testKey");
        Block testing = Ledger.getInstance().getBlockByKey("testKey");

        Assert.assertEquals("block number does not match", block.blockNumber, testing.blockNumber);
        Assert.assertEquals("block hash does not match", block.hash, testing.hash);
        Assert.assertEquals("previous hash does not match", block.previousHash, testing.previousHash);

        Ledger.getInstance().deleteBlockByKey("testKey");
        testing = Ledger.getInstance().getBlockByKey("testKey");
        Assert.assertNull("delete method does not delete block", testing);
    }
}
