public class Node implements Runnable{

    @Override
    public void run() {

    }



    public boolean verifyIncomingBlock(Block block){
        Block latestLocallyStoredBlock = Ledger.getInstance().getBlockByKey("latestBlockHash");
        if (!block.hash.equals(block.calHash())) return false;
        if (!block.previousHash.equals(latestLocallyStoredBlock.hash)) return false;
        // NOT YET IMPLEMENTED BUT THIS CHECKS TRANSACTIONS BY REBUILDING MERKLE TREE TO GET MERKLE ROOT
        //if (!block.merkleroot.equals(Util.getMerkleRoot(Util.getTransactionHashList(block.transactions)))) return false;
        return true;
    }
}
