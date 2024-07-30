import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LedgerTest {
    Block block;
    Ledger ledger;
    ArrayList<Transaction> list;
    Wallet wallet;
    // set up
        @Before
    public void setUpBlock(){
         // opening database
         ledger = new Ledger();
         list = new ArrayList<>();
         wallet = new Wallet();
         wallet.run();

         list.add(new Transaction(wallet.getPublicByIndex(0), wallet.getPublicByIndex(1), 100000));
         list.get(0).applySig(wallet.getPrivateFromPublic(wallet.getPublicByIndex(0)));

         list.add(new Transaction(wallet.getPublicByIndex(1), wallet.getPublicByIndex(0), 24242));
         list.get(0).applySig(wallet.getPrivateFromPublic(wallet.getPublicByIndex(1)));

         // creates new block for us to work with
         block = new Block("prev", 100000, 100000, list);
    }

    @Test
    public void doesLedgerAddAndGetAndDeleteBlock(){

        ledger.addBlock(block, "testKey");
        Block testing = ledger.getBlockByKey("testKey");

        Assert.assertEquals("block number does not match", block.blockNumber, testing.blockNumber);
        Assert.assertEquals("block hash does not match", block.hash, testing.hash);
        Assert.assertEquals("previous hash does not match", block.previousHash, testing.previousHash);

        // testing to make sure transaction details match
        Assert.assertEquals("amount does not match", block.transactionlist.get(0).amount, testing.transactionlist.get(0).amount, 0);
        Assert.assertEquals("public key does not match", block.transactionlist.get(0).sender, testing.transactionlist.get(0).sender);
        Assert.assertEquals("merkle root not building correctly/ doesnt match", block.merkleRoot, Util.getMerkleRoot(testing.transactionlist));

        ledger.deleteBlockByKey("testKey");
        testing = ledger.getBlockByKey("testKey");
        Assert.assertNull("delete method does not delete block", testing);
    }










}
