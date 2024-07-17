import java.util.ArrayList;

public class BlockChain {
    private ArrayList<Block> blockChain = new ArrayList<>();

    public BlockChain(){

    }
    public void add(Block block){
        Ledger.getInstance().addBlock(block,"key1");
        Ledger.getInstance().getBlock("key1");

    }

    public boolean checkIfBlockExist(){
        Ledger.getInstance();
        return false;
    }
}
