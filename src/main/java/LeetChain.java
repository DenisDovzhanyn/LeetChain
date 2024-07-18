import java.util.ArrayList;


public class LeetChain {

    public static void main(String[] args){
        BlockChain chain = new BlockChain();
        Block genesis = new Block("genesis","hello","question",0);
        genesis.mineBlock();
        chain.add(genesis);
        System.out.println("GENESIS EXISTS: " + chain.checkIfBlockExist(genesis));
        System.out.println("previous:" + chain.getPrevious());
        System.out.println(chain.getPrevious().blockNumber);
        Block block2 = new Block(chain.getPrevious().hash,"gello","peanuts",chain.getPrevious().blockNumber+1);
        block2.mineBlock();
        chain.add(block2);
        System.out.println(chain.isChainValid());
        System.out.println(chain.getBlockChain().get(0) + " " +chain.getBlockChain().get(1));


    }

}
