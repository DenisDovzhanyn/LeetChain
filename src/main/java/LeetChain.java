import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;


public class LeetChain {

    public static void main(String[] args){
        BlockChain chain = new BlockChain();
        Block block;

        Wallet wallet = new Wallet();
        PublicKey key = wallet.getPublicByIndex(0);
        PrivateKey test = wallet.getPrivateFromPublic(key);
        System.out.println(test.toString());
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
