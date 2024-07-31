package Miner;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import Node.Ledger;

public class BlockChain {

    // Only stores the 20 most recent blocks
    private static CircularFifoQueue<Block> blockChain = new CircularFifoQueue<Block>(20);

    public BlockChain(){
        Ledger.getInstance();
        blockChain = Ledger.getInstance().generateList();
    }

    public void add(Block block){
        if(!checkIfBlockExist(block) && isChainValid() || !checkIfBlockExist(block) && blockChain.size() == 1){
            Ledger.getInstance().addBlock(block, block.hash);
            blockChain.add(block);

        }
    }

    //checking if the block is already present on rocksDB
    public boolean checkIfBlockExist(Block block){
        if(Ledger.getInstance().getBlockByKey(Integer.toString(block.blockNumber)) == null) return false;
        return true;
    }

    // checks current and previous blocks hash to make sure they match up, prevents tampering
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


    public CircularFifoQueue<Block> getBlockChain() {
        return blockChain;
    }

    public Block getPrevious(){
        if (blockChain.isEmpty()) {
            return null;  // Return null if blockChain is empty
        }
        return blockChain.get(blockChain.size()-1);
    }

    public Block getByIndexOfList(int index){
        return blockChain.get(index);
    }


    //difficulty is calculated every 19 blocks, difficulty can grow or shrink exponentially depending on how fast blocks were mined
    public int calculateDifficulty(){
        if(getPrevious().blockNumber % 19 == 0) {
            long expectedTime = 19 * (10 * 60 * 1000);
            long actualTime = getPrevious().getTimeStamp() - getByIndexOfList(0).getTimeStamp();
            int newDifficulty = Math.round((expectedTime / actualTime) * getPrevious().getDifficulty());
            return newDifficulty;


        }
            return getPrevious().getDifficulty();

    }
}
