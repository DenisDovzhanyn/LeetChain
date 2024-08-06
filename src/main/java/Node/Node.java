package Node;

import Miner.Block;
import Utilities.Util;
import Wallet.Transaction;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Node implements Runnable{
    ConcurrentLinkedQueue<Transaction> toMinerAndOtherNodes;
    ConcurrentLinkedQueue<Block> blocksToOtherNodes;
    public Node (ConcurrentLinkedQueue<Transaction> transactionsToMiners, ConcurrentLinkedQueue<Block> blocksToOtherNodes) {
        toMinerAndOtherNodes = transactionsToMiners;
        this.blocksToOtherNodes = blocksToOtherNodes;
        Ledger.getInstance();
    }

    @Override
    public void run() {
        while (true) {
            if(!blocksToOtherNodes.isEmpty()) {
                // we send a block out if list is not empty
            }

        }

    }

    public boolean verifyIncomingBlock(Block block){
        Block latestLocallyStoredBlock = Ledger.getInstance().getBlockByKey("latestBlockHash");
        if (!block.hash.equals(block.calHash())){
            System.out.println("Hash not matching with calculated hash");
            return false;
        }
        if (!block.previousHash.equals(latestLocallyStoredBlock.hash)){
            System.out.println("Previous hash not matching with last locally stored block");
            return false;
        }

        String merkleRootFromIncomingBlock = Util.getMerkleRoot(block.transactionlist, (Transaction x) -> x.id);
        if (!block.merkleRoot.equals(merkleRootFromIncomingBlock)){
            System.out.println("Merkleroot not matching with calculated merkleroot");
            return false;
        }
        return true;
    }




}
