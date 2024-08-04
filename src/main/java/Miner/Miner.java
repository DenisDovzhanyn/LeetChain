package Miner;

import Node.Ledger;
import Wallet.Transaction;
import Wallet.TransactionType;
import Wallet.TransactionOutput;
import Wallet.Wallet;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Miner implements Runnable {
    ConcurrentLinkedQueue<Transaction> transactionsToMiner;
    List<Transaction> transactionList;
    PublicKey minersKey;

    public Miner (ConcurrentLinkedQueue<Transaction> toMiner, PublicKey minersKey) {
        transactionsToMiner = toMiner;
        this.minersKey = minersKey;
    }

    @Override
    public void run() {
        BlockChain chain = new BlockChain();

        if (chain.getBlockChain().isEmpty()) {
            Block block = new Block("0", 1, 50000000);
            mineBlock(block);
            chain.add(block);
        }

        while(true) {
            Block block;
            setListOfTransactions();
            // creating reward for miner doing this for testing
            Transaction reward = new Transaction(TransactionType.COINBASE);
            reward.addUTXOs(Block.calculateReward(chain.getPrevious().blockNumber + 1) + scrapeFees(),minersKey,minersKey);
            reward.outputs.get(0).applySig(Wallet.getPrivateFromPublic(minersKey));
            transactionList.add(reward);

            block = new Block(chain.getPrevious().hash, chain.getPrevious().blockNumber + 1, chain.calculateDifficulty(), transactionList);

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
//        System.out.println("And you earned " + block.transactionlist.get(0).outputs.get(0).value + " LeetCoins!!!");
    }

    public double scrapeFees() {
        double totalFeesCollected = 0;

        for (Transaction x : transactionList) {
            double inputtedAmount = 0;
            double outPuttedAmount = 0;
            for (TransactionOutput y : x.inputs) {
                inputtedAmount += y.value;
            }
            for (TransactionOutput z : x.outputs) {
                outPuttedAmount += z.value;
            }
            totalFeesCollected += inputtedAmount - outPuttedAmount;
        }

        return totalFeesCollected;
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