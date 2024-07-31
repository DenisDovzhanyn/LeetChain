package Miner;

import Wallet.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Miner implements Runnable {
    ConcurrentLinkedQueue<Transaction> transactionsToMiner;
    List<Transaction> transactionList;
    public Miner (ConcurrentLinkedQueue<Transaction> toMiner) {
        transactionsToMiner = toMiner;
    }
    @Override
    public void run() {
        BlockChain chain = new BlockChain();

        if (chain.getBlockChain().isEmpty()) {
            Block block = new Block("0", 1, 500000);
            mineBlock(block);
            chain.add(block);
        }

        while(true) {
            Block block;
            setListOfTransactions();
            if(!transactionList.isEmpty()) {
                block = new Block(chain.getPrevious().hash, chain.getPrevious().blockNumber + 1, chain.calculateDifficulty(), transactionList);
            } else {
                block = new Block(chain.getPrevious().hash, chain.getPrevious().blockNumber + 1, chain.calculateDifficulty(), transactionList);
            }

            mineBlock(block);
            chain.add(block);
        }


    }


    public void mineBlock(Block block) {
        System.out.println("mining block at index: " + block.blockNumber + ", at difficulty: " + block.getDifficulty() + "... ");
        while (!block.isHashFound(block.currentHashValue)) {
            block.mineBlock();
           // System.out.print(block.hash + "\r");

        }
        System.out.println("Nice you've mined a block: " + block.hash);
    }

    public void setListOfTransactions() {
        transactionList = new ArrayList<>();
        if (!transactionsToMiner.isEmpty()) {
            for (int i = 0; i < transactionsToMiner.size(); i++) {
                if (i > 20) break;

                transactionList.add(transactionsToMiner.poll());
            }
        }
    }

}