import java.util.ArrayList;

public class BlockChain {
    private ArrayList<Block> blockChain = new ArrayList<Block>();

    public BlockChain(){

    }
    public void add(Block block){
        if(!checkIfBlockExist(block) && isChainValid()) Ledger.getInstance().addBlock(block,Integer.toString(block.blockNumber));


    }

    public boolean checkIfBlockExist(Block block){
        if(Ledger.getInstance().getBlock(Integer.toString(block.blockNumber)) != null) return true;
        return false;
    }
    public boolean isChainValid(){
        Block previous;
        Block current;

        for(int i = 1; i < blockChain.size(); i++){

            current = blockChain.get(i);
            previous = blockChain.get(i -1);

            if(!previous.hash.equals(current.previousHash)){
                System.out.println("previous hash tampered: ");
                System.out.println(previous.hash);
                System.out.println(current.previousHash);
                return false;
            }

            if(!current.hash.equals(current.calHash())){
                System.out.println("current hash tampered");
                System.out.println(current.hash);
                System.out.println(current.calHash());
                return false;
            }

        }
        return true;
    }
}
