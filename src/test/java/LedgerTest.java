import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LedgerTest {
    Block block;
    Ledger ledger;
    // set up
        @Before
    public void setUpBlock(){
         // opening database
         ledger = new Ledger();
         // creates new block for us to work with
         block = new Block("a", "hello", "testing", 1000000, 5000);
    }

    @Test
    public void doesLedgerAddAndGetAndDeleteBlock(){

        ledger.addBlock(block, "testKey");
        Block testing = ledger.getBlockByKey("testKey");

        Assert.assertEquals("block number does not match", block.blockNumber, testing.blockNumber);
        Assert.assertEquals("block hash does not match", block.hash, testing.hash);
        Assert.assertEquals("previous hash does not match", block.previousHash, testing.previousHash);

        ledger.deleteBlockByKey("testKey");
        testing = ledger.getBlockByKey("testKey");
        Assert.assertNull("delete method does not delete block", testing);
    }







}
