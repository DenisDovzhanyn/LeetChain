import Node.Ledger;
import Miner.Block;
import Node.Node;
import Wallet.Transaction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NodeTest {
    ConcurrentLinkedQueue<Transaction> temp;
    ConcurrentLinkedQueue<Block> notUsed;
    Block falseBlock;
    Block correctBlock;
    Node node;

    @Before
    public void setUp() {
        temp = new ConcurrentLinkedQueue<>();
        notUsed = new ConcurrentLinkedQueue<>();
        node = new Node(temp, notUsed);
        falseBlock = Ledger.getInstance().getLatestBlock();
        correctBlock = Ledger.getInstance().getLatestBlock();
    }
    // we are testing to make sure that our methods return true/false when given true/false conditions
    @Test
    public void previousHashTest() {
        falseBlock.previousHash = "asdasdads";
        Assert.assertFalse("should return false, previous hash shouldn't match",node.verifyPreviousHash(falseBlock));
        falseBlock.previousHash = correctBlock.hash;
        Assert.assertTrue("should return true, previous hash should match", node.verifyPreviousHash(falseBlock));
    }

    @Test
    public void calculatedHashNotMatchingTest() {
        falseBlock.blockNumber = 2131441241;
        falseBlock.previousHash= "asdads";
        Assert.assertFalse("Should return false when calculating hash", node.verifyBlockHash(falseBlock));
        falseBlock.blockNumber = correctBlock.blockNumber;
    }

    @Test
    public void merkleRootNotMatchingTest() {
        falseBlock.transactionlist.get(0).id = "asdasd";
        Assert.assertFalse("Should return false when calculating false merkleRoot", node.verifyMerkleRoot(falseBlock));
        falseBlock.transactionlist.get(0).id = correctBlock.transactionlist.get(0).id;
        Assert.assertTrue("Should return true when calculating correct merkleRoot", node.verifyMerkleRoot(falseBlock));
    }

    @Test
    public void blockDifficultyTest() {
        falseBlock.setDifficulty(0);
        Assert.assertFalse("Should return false when given wrong difficulty (0)", node.verifyDifficulty(falseBlock));
        // testing if it is true is difficult due to the fact that the difficulty depends completely on the time between blocks
    }

    @Test
    public void beatTargetHashTest() {
        //value of the hash is 71,386,622,362,447,003,578,456,328,373,263,341,923,402,970,167,109,806,054,530,861,483,143,478,028,852
        falseBlock.currentHashValue = new BigInteger("9dd3657b6f964138d557ee18bb2095a19ff5b0a3a777287ead663273bbc6aa34", 16);
        Assert.assertFalse("Should return false when given an extremely large/impossible hash value", falseBlock.isHashFound(falseBlock.currentHashValue));
        falseBlock.currentHashValue = new BigInteger(correctBlock.hash, 16);
        Assert.assertTrue("Should return true when given a hash that beats target hash", falseBlock.isHashFound(falseBlock.currentHashValue));

    }



}
