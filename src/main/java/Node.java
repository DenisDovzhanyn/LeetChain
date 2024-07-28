public class Node implements Runnable{

    @Override
    public void run() {

    }



    public boolean verifyIncomingBlock(Block block){
        Block latestLocallyStoredBlock = Ledger.getInstance().getBlockByKey("latestBlockHash");
        if (!block.hash.equals(block.calHash())) return false;
        if (!block.previousHash.equals(latestLocallyStoredBlock.hash)) return false;

        return true;
    }
}
