import java.util.ArrayList;


public class LeetChain {

    public static void main(String[] args){
        BlockChain chain = new BlockChain();
        Block block;

        Wallet wallet = new Wallet();

        if(chain.getBlockChain().isEmpty()) {
            block = new Block("genesis","hello","question",1, 300000);
            block.mineBlock();
            chain.add(block);

        }

       while(true){
           Block newBlock = new Block(chain.getPrevious().hash, "question", "answer", chain.getPrevious().blockNumber + 1, chain.calculateDifficulty());
           newBlock.mineBlock();
           chain.add(newBlock);

       }


    }

}
