package Node;

import Miner.Block;
import Miner.BlockChain;
import Node.MessageTypes.BlockMessage;
import Node.MessageTypes.LatestBlockNumber;
import Node.MessageTypes.TransactionMessage;
import Utilities.Util;
import Wallet.Transaction;
import Wallet.TransactionType;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Node implements Runnable{
    ConcurrentLinkedQueue<TransactionMessage> transactionsToOtherNodes;
    ConcurrentLinkedQueue<Transaction> transactionsToMiner;
    ConcurrentLinkedQueue<Transaction> incomingTransaction;
    ConcurrentLinkedQueue<Block> blocksToOtherNodes;
    ConcurrentLinkedQueue<Object> outBoundMessage;
    ConcurrentLinkedQueue<Object> incomingMessage;
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

        ConcurrentLinkedQueue<Socket> newConnectionsToListener = new ConcurrentLinkedQueue<Socket>();
        this.outBoundMessage = new ConcurrentLinkedQueue<>();
        incomingMessage = new ConcurrentLinkedQueue<>();

        this.server = new SocketHandler(newConnectionsToListener);
        this.listener = new Listener(newConnectionsToListener, outBoundMessage, incomingMessage);
        this.socketHandler = new Thread(server);
        this.handleOutBound = new Thread(listener);
        handleOutBound.start();
        socketHandler.start();


        while (Listener.amountOfConnectedSockets() <= 1) {
            // wait here until we are connected to at least one person
        }

        // rest of program can continue once we connect to at least one person

    }
    // how will we notify miner when a new block comes in and how will he access its data for the next block?
    @Override
    public void run() {
        List<Block> blocksNotAbleToBeVerifiedYet = new ArrayList<>();
        boolean startUp = false;
        int latestBlockNumber = Integer.MAX_VALUE;
        while (true) {
            if(!blocksToOtherNodes.isEmpty()) {
                outBoundMessage.add(blocksToOtherNodes.poll());
            }
            if(!incomingMessage.isEmpty()) {
                Object object = incomingMessage.poll();
                // when should i send another blocklistrequest if one of my connected nodes fails to send me some that i have previously requested?
                // we need the node to wait until we have latest block
                if (object instanceof BlockMessage) {
                    BlockMessage message = (BlockMessage) object;
                    if (message.getBlocks().get(0).blockNumber == Ledger.getInstance().getLatestBlock().blockNumber + 1) {
                        int peersIndexInList = SocketHandler.findPeerIndexByIp(message.getIp());

                        for (Block x : message.getBlocks()) {
                            if (verifyIncomingBlock(x)) {
                                Ledger.getInstance().addBlock(x, x.hash);
                                BlockChain.nodeAdd(x);

                                outBoundMessage.add(x);
                                SocketHandler.peers.get(peersIndexInList).raiseScoreByOne();
                            } else {
                                SocketHandler.peers.get(peersIndexInList).lowerScoreByOne();
                            }
                        }
                    } else {
                        blocksNotAbleToBeVerifiedYet.addAll(message.getBlocks());
                    }
                } else if (object instanceof LatestBlockNumber && !startUp) {// use startup boolean here so people cant send latestblocknumbers and block our entire program
                    latestBlockNumber = ((LatestBlockNumber) object).getLatestBlockNumber();
                    startUp = true;
                } else if (Ledger.getInstance().getLatestBlock().blockNumber >= latestBlockNumber) {
                    if (object instanceof TransactionMessage) {
                        TransactionMessage transactionMessage = (TransactionMessage) object;
                        incomingTransaction.add(transactionMessage.getTransaction());
                    }
                }

            }

            if (!blocksNotAbleToBeVerifiedYet.isEmpty()) {
                blocksNotAbleToBeVerifiedYet.sort(Comparator.comparingInt(Block::getBlockNumber));

                for (Block x : blocksNotAbleToBeVerifiedYet) {
                    if (x.getBlockNumber() != Ledger.getInstance().getLatestBlock().getBlockNumber() + 1) break;
                    // very stupid doing this i guess but i dont want to rewrite code for verifying block
                    incomingMessage.add(x);
                    blocksNotAbleToBeVerifiedYet.remove(x);
                }
            }
            if(!incomingTransaction.isEmpty()) {
                TransactionMessage transaction = new TransactionMessage(incomingTransaction.poll(), "ip not inputted yet");
                if (verifyIncomingTransaction(transaction.getTransaction())) {
                    transactionsToOtherNodes.add(transaction);
                    transactionsToMiner.add(transaction.getTransaction());
                }
            }

        }

    }

    public boolean verifyIncomingBlock(Block block){
        return (
            verifyBlockHash(block) &&
            verifyPreviousHash(block) &&
            verifyMerkleRoot(block) &&
            addOrUpdateTransactions(block)
        );
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
