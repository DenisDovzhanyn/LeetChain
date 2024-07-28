import java.security.PrivateKey;
import java.security.PublicKey;



public class LeetChain {

    public static void main(String[] args){
        BlockChain chain = new BlockChain();
        Block block;

        Wallet a = new Wallet();
        Wallet b = new Wallet();
        Transaction transaction = new Transaction(a.getPublicByIndex(0), b.getPublicByIndex(1), 5);
/*
        if(chain.getBlockChain().isEmpty()) {
            block = new Block("genesis","hello","question",1, 3000000);
            block.mineBlock();
            chain.add(block);

        }

       while(true){
           Block newBlock = new Block(chain.getPrevious().hash, "question", "answer", chain.getPrevious().blockNumber + 1, chain.calculateDifficulty());
           newBlock.mineBlock();
           chain.add(newBlock);
       }
*/
    }
}
