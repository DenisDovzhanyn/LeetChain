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

public class LedgerTest {
    Block block;
    ArrayList<Transaction> list;
    Wallet wallet;
    // set up
        @Before
    public void setUpBlock(){
         // opening database
         Ledger.getInstance();
         list = new ArrayList<>();
         wallet = new Wallet();
         wallet.run();
         list.add(wallet.generateTransaction(wallet.getPublicByIndex(0), wallet.getPublicByIndex(0),5, TransactionType.COINBASE));
         // creates new block for us to work with
         block = new Block("prev", 100000, 100000, list);
    }

    @Test
    public void doesLedgerAddAndGetAndDeleteBlock(){

        Ledger.getInstance().addBlock(block, "testKey");
        Block testing = Ledger.getInstance().getBlockByKey("testKey");

        Assert.assertEquals("block number does not match", block.blockNumber, testing.blockNumber);
        Assert.assertEquals("block hash does not match", block.hash, testing.hash);
        Assert.assertEquals("previous hash does not match", block.previousHash, testing.previousHash);

        // testing to make sure transaction details match
        Assert.assertEquals("type does not match", block.transactionlist.get(0).type, testing.transactionlist.get(0).type);
        Assert.assertEquals("value does not match", block.transactionlist.get(0).outputs.get(0).value, testing.transactionlist.get(0).outputs.get(0).value, 0);
        Assert.assertEquals("merkle root not building correctly/ doesnt match", block.merkleRoot, Util.getMerkleRoot(testing.transactionlist, (Transaction x) -> x.id));

        Ledger.getInstance().deleteBlockByKey("testKey");
        testing = Ledger.getInstance().getBlockByKey("testKey");
        Assert.assertNull("delete method does not delete block", testing);
    }










}
