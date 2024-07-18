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
       /* System.out.println("GENESIS EXISTS: " + chain.checkIfBlockExist(genesis));
        System.out.println("previous:" + chain.getPrevious());
        System.out.println(chain.getPrevious().blockNumber);
        Block block2 = new Block(chain.getPrevious().hash,"gello","peanuts",chain.getPrevious().blockNumber+1);
        block2.mineBlock();
        chain.add(block2);
        System.out.println(chain.isChainValid());
        System.out.println(chain.getBlockChain().get(0) + " " +chain.getBlockChain().get(1));
        */

    }

}
