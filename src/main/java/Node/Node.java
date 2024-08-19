package Node;

import Miner.Block;
import Miner.BlockChain;
import Miner.Miner;
import Utilities.Util;
import Wallet.Transaction;
import Wallet.TransactionType;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Node implements Runnable{
    ConcurrentLinkedQueue<Transaction> transactionsToOtherNodes;
    ConcurrentLinkedQueue<Transaction> transactionsToMiner;
    ConcurrentLinkedQueue<Block> blocksToOtherNodes;
    ConcurrentLinkedQueue<Block> incomingBlocks;

    SocketHandler server;
    Thread socketHandler;


    public Node (ConcurrentLinkedQueue<Transaction> transactionsToNodes, ConcurrentLinkedQueue<Block> blocksToOtherNodes) {
        transactionsToOtherNodes = transactionsToNodes;
        this.blocksToOtherNodes = blocksToOtherNodes;
        Ledger.getInstance();
        server = new SocketHandler(blocksToOtherNodes, incomingBlocks);
        socketHandler = new Thread(server);
        socketHandler.start();

        while (server.amountOfConnectedSockets() <= 1) {
            // wait here until we are connected to at least one person
        }

        // rest of program can continue once we connect to at least one person

    }
    // how will we notify miner when a new block comes in and how will he access its data for the next block?
    @Override
    public void run() {
        while (true) {
            if(!incomingBlocks.isEmpty()) {
                // notify miner before or after a new block is verified? how long will verification take ? will people try to take advantage of this
                // and send faulty blocks to set miners back and interrupt their mining?
                Block incomingBlock = blocksToOtherNodes.poll();

                if (verifyIncomingBlock(incomingBlock)) {
                    Ledger.getInstance().addBlock(incomingBlock, incomingBlock.hash);
                    BlockChain.nodeAdd(incomingBlock);

                }
            }

            if(!transactionsToMiner.isEmpty()) {

            }

        }

    }

    public boolean verifyIncomingBlock(Block block){
        if (!verifyBlockHash(block)) return false;
        if (!verifyPreviousHash(block)) return false;
        if (!verifyMerkleRoot(block)) return false;
        if (!addOrUpdateTransactions(block)) return false;

        return true;
    }

    public boolean verifyIncomingTransaction(Transaction transaction) {
        double inputs = Ledger.getInstance().getAmount(transaction.inputs);
        double outputs = Ledger.getInstance().getAmount(transaction.outputs);

        if (inputs < outputs || !transaction.verifyInputs() || transaction.type == TransactionType.COINBASE) return false;

        return true;
    }

    public boolean verifyBlockHash(Block block) {
        if (!block.hash.equals(block.calHash())) {
            System.out.println("Hash not matching with calculated hash");
            return false;
        }
        return true;
    }

    public boolean verifyPreviousHash(Block block) {
        Block latestLocallyStoredBlock = Ledger.getInstance().getLatestBlock();
        if (!block.previousHash.equals(latestLocallyStoredBlock.hash)) {
            System.out.println("Previous hash not matching with last locally stored block");
            return false;
        }

        return true;
    }

    public boolean verifyMerkleRoot(Block block) {
        String merkleRootFromIncomingBlock = Util.getMerkleRoot(block.transactionlist, (Transaction x) -> x.id);
        if (!block.merkleRoot.equals(merkleRootFromIncomingBlock)) {
            System.out.println("Merkleroot not matching with calculated merkleroot");
            return false;
        }

        return true;
    }

    public boolean verifyDifficulty(Block block) {
        BlockChain temporaryBlockChain = new BlockChain();
        temporaryBlockChain.add(block);
        if (block.getDifficulty() != temporaryBlockChain.calculateDifficulty()) {
            temporaryBlockChain.getBlockChain().remove(block);
            System.out.println("Calculated difficulty not matching incoming blocks difficulty");
            return false;
        }

        return true;
    }

    public boolean addOrUpdateTransactions(Block block) {
        if (!Ledger.getInstance().addOrUpdateUTXOList(block.transactionlist, block.blockNumber)) {
            return false;
        }

        return true;
    }





}
