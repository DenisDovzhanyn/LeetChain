import java.util.ArrayList;


public class LeetChain {

    public static void main(String[] args){
        BlockChain chain = new BlockChain();
        Block block;
        if(chain.getBlockChain().isEmpty()) {
            block = new Block("genesis","hello","question",0);
            block.mineBlock();
            chain.add(block);
            System.out.println("no genesis block!!!!!");
        }

        System.out.println(chain.getPrevious().blockNumber);
       while(true){
           Block newBlock = new Block(chain.getPrevious().hash, "question", "answer", chain.getPrevious().blockNumber + 1);
           newBlock.mineBlock();
           chain.add(newBlock);

       }
      

    }

}
