package Node;

import Miner.Block;
import Miner.BlockChain;
import Miner.Miner;
import Node.MessageTypes.BlockMessage;
import Node.MessageTypes.TransactionMessage;
import Utilities.Util;
import Wallet.Transaction;
import Wallet.TransactionType;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Node implements Runnable{
    ConcurrentLinkedQueue<TransactionMessage> transactionsToOtherNodes;
    ConcurrentLinkedQueue<Transaction> transactionsToMiner;
    ConcurrentLinkedQueue<Transaction> incomingTransaction;
    ConcurrentLinkedQueue<Block> blocksToOtherNodes;
    ConcurrentLinkedQueue<BlockMessage> incomingBlocks;
    SocketHandler server;
    Listener listener;
    Thread handleOutBound;
    Thread socketHandler;


    public Node (ConcurrentLinkedQueue<Transaction> transactionsMiner, ConcurrentLinkedQueue<Block> blocksToOtherNodes, ConcurrentLinkedQueue<Transaction> incomingTransaction) {
        transactionsToOtherNodes = new ConcurrentLinkedQueue<>();
        this.transactionsToMiner = transactionsMiner;
        this.incomingTransaction = incomingTransaction;
        this.blocksToOtherNodes = blocksToOtherNodes;
        Ledger.getInstance();
        ConcurrentLinkedQueue<SocketSendingOut> newConnectionsToListener = new ConcurrentLinkedQueue<SocketSendingOut>();

        this.server = new SocketHandler(incomingBlocks, newConnectionsToListener, transactionsToOtherNodes);
        this.listener = new Listener(newConnectionsToListener, blocksToOtherNodes, transactionsToOtherNodes);
        this.socketHandler = new Thread(server);
        this.handleOutBound = new Thread(listener);
        handleOutBound.start();
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
               BlockMessage message= incomingBlocks.poll();
               int peersIndexInList = SocketHandler.findPeerIndexByIp(message.getIp());

                for (Block x : message.getBlocks()) {
                    if (verifyIncomingBlock(x)) {
                        Ledger.getInstance().addBlock(x, x.hash);
                        BlockChain.nodeAdd(x);
                        // we want to send this out to other people we are connected to but how do we stop it from sending it back to the
                        // person who sent us the block originally?
                        blocksToOtherNodes.add(x);
                        SocketHandler.peers.get(peersIndexInList).raiseScoreByOne();
                    } else {
                        SocketHandler.peers.get(peersIndexInList).lowerScoreByOne();
                    }
                }
            }
            if(!incomingTransaction.isEmpty()) {
                TransactionMessage transaction = new TransactionMessage(transactionsToMiner.poll(), "ip not inputted yet");
                transactionsToOtherNodes.add(transaction);
                transactionsToMiner.add(transaction.getTransaction());
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
