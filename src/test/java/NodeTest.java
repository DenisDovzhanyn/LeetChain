import Node.Ledger;
import Miner.Block;
import Node.Node;
import Wallet.Transaction;
import Wallet.TransactionOutput;
import Wallet.TransactionType;
import Wallet.Wallet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
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

}
