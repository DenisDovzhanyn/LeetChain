package Node;

import Miner.Block;
import Wallet.Transaction;
import Wallet.TransactionOutput;
import Utilities.Util;
import Wallet.TransactionType;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import Miner.Miner;

import java.io.*;
import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Ledger {
    private static Ledger instance;
    private RocksDB blocksDb;
    private RocksDB keysAndTransactionDb;
    //loads db and assigns it to db variable
    public Ledger() {
        RocksDB.loadLibrary();
        Options options = new Options().setCreateIfMissing(true);
        try {
            this.blocksDb = RocksDB.open(options, "BlockDataBase");
            this.keysAndTransactionDb = RocksDB.open(options, "keyAndTransactionDatabase");
        } catch (RocksDBException e) {
            throw new RuntimeException("Error opening RocksDB", e);
        }
    }

    // allows us to have the db always open
    public static Ledger getInstance() {
        if (instance == null) instance = new Ledger();

        return instance;

    }


    // serializes block and adds it to rocksDB, key will be block hash
    // latestblockhash always updated to point at the latest mined block
    public void addBlock(Block block, String key) {
        try {
            ByteArrayOutputStream bytArray = new ByteArrayOutputStream();
            ObjectOutputStream obs = new ObjectOutputStream(bytArray);
            obs.writeObject(block);

            byte[] blockBytes = bytArray.toByteArray();
            byte[] keyBytes = key.getBytes();
            byte[] latestBlockHashKey = "latestBlockHash".getBytes();
            byte[] latestBlockHash = block.hash.getBytes();

            blocksDb.put(keyBytes, blockBytes);
            blocksDb.put(latestBlockHashKey,latestBlockHash);
        } catch (IOException | RocksDBException e){
            throw new RuntimeException("error adding to db", e);
        }
    }

    // returns and deserializes block, returning null if no block was found/doesnt exist
    public Block getBlockByKey(String key) {
        Block gottenBlock = null;
        try{
            byte[] blockBytes = blocksDb.get(key.getBytes());

            if(blockBytes != null) gottenBlock = deserializeBlock(blockBytes);

        } catch (RocksDBException f) {
            throw new RuntimeException("error getting block",f);
        }

        return gottenBlock;
    }

    //uses circularfifoqueue to generate a list when program starts that holds the 20 latest blocks
    public CircularFifoQueue<Block> generateList(){
        CircularFifoQueue<Block> list = new CircularFifoQueue<Block>(20);
        List<Block> beforeFifoQueue = new ArrayList<Block>();
        String previousHash = "";
        String latestBlockHashKey = "latestBlockHash";

        try {
            for (int i = 0; i < 20; i++) {
                byte[] value = null;

                if (i == 0) {
                    byte[] latestHash = blocksDb.get(latestBlockHashKey.getBytes());
                    if(latestHash != null) value = blocksDb.get(latestHash);
                } else {
                    value = blocksDb.get(previousHash.getBytes());
                }

                if (value != null) {
                    Block block = deserializeBlock(value);
                    previousHash = block.previousHash;
                    beforeFifoQueue.add(0, block);
                }
            }
            list.addAll(beforeFifoQueue);
        } catch (RocksDBException e){
            throw new RuntimeException("error generating list", e);
        }

        return list;
    }

    public Block deserializeBlock(byte[] value){
        try(ByteArrayInputStream bytArray = new ByteArrayInputStream(value);
            ObjectInputStream ois = new ObjectInputStream(bytArray)){
            Block block = (Block) ois.readObject();

            return block;
        } catch(IOException | ClassNotFoundException e ){
            throw new RuntimeException("error deserializing", e);
        }
    }

    // ADD A PUT/REMOVE/GET METHOD FOR UTXOS!!!!!!!
    public List<TransactionOutput> getUTXOListByPublicKey(PublicKey key) {
        List<TransactionOutput> output = new ArrayList<>();

        try {

            byte[] listByte = keysAndTransactionDb.get(Util.keyToString(key).getBytes());
            if(listByte != null) {

                ByteArrayInputStream byteArray = new ByteArrayInputStream(listByte);
                ObjectInputStream ois = new ObjectInputStream(byteArray);

                output = (List<TransactionOutput>) ois.readObject();

                return output;
            }
        } catch (RocksDBException | IOException | ClassNotFoundException e) {
            throw new RuntimeException("error getting utxo list", e);
        }
        return output;
    }

    // checks inputs/outputs before adding to db
    public boolean addOrUpdateUTXOList (List<Transaction> transactionList, int blockNumber) {
        final int MAX_COINBASE_TRANSACTIONS = 1;
        int totalCoinBaseTransactions = 0;
        double scrapedFees = Miner.scrapeFees(transactionList);

            for(Transaction x : transactionList) {
                List<TransactionOutput> inputsAndOutputs = Stream.concat(x.inputs.stream(), x.outputs.stream()).collect(Collectors.toList());

                double inputsAmount = getAmount(x.inputs);
                double outputsAmount = getAmount(x.outputs);

                if (x.type.equals(TransactionType.COINBASE)) {
                    totalCoinBaseTransactions++;
                    if (totalCoinBaseTransactions > MAX_COINBASE_TRANSACTIONS) {
                        System.out.println("Too many coinbase transactions for one block, rejecting block");
                        return false;
                    }
                    if (Block.calculateReward(blockNumber) + scrapedFees < outputsAmount) {
                        System.out.println("Miner messed with block reward or fees, rejecting block");
                        return false;
                    }
                } else if (inputsAmount < outputsAmount || !x.verifyInputs()) {
                    System.out.println("Not enough money for transaction: " + x.id + ", rejecting block");
                    return false;
                }
                if(!Transaction.verifySignatures(inputsAndOutputs)) return false;

                List<TransactionOutput> sendersFilteredUTXOs = filterUsedOutputs(x.inputs);
                Map<PublicKey, List<TransactionOutput>> publicKeyToUXTOList = mapUpdatedListsToMap(sendersFilteredUTXOs, x.outputs);

                for (Map.Entry<PublicKey, List<TransactionOutput>> y : publicKeyToUXTOList.entrySet()) {
                    writeUTXOListToDB(y.getKey(), y.getValue());
                }
            }
        System.out.println("UTXOs successfully added");
        return true;
    }

    // after checks pass list is filtered
    public List<TransactionOutput> filterUsedOutputs(List<TransactionOutput> transactionInputs) {
        List<TransactionOutput> sendersUnFilteredUTXOList = getUTXOListByPublicKey(transactionInputs.get(0).getReceiver());

        Set<TransactionOutput> usedUTXOs = new HashSet<>();
        for(TransactionOutput x : transactionInputs) {
            usedUTXOs.add(x);
        }

        List<TransactionOutput> sendersFilteredUTXOList = sendersUnFilteredUTXOList.stream()
                .filter(y -> !usedUTXOs.contains(y))
                .collect(Collectors.toList());

        return sendersFilteredUTXOList;
    }

    // after list is filtered new lists are added to a map
    public Map<PublicKey, List<TransactionOutput>> mapUpdatedListsToMap(List<TransactionOutput> sendersFilteredList, List<TransactionOutput> transactionOutputs) {
        PublicKey fromFundsKey = transactionOutputs.get(0).getSender();
        PublicKey toFundsKey = transactionOutputs.get(0).getReceiver();

        Map<PublicKey, List<TransactionOutput>> keyUXTOValuePairs = new HashMap<>();
        List<TransactionOutput> receiversUTXOList = getUTXOListByPublicKey(toFundsKey);

        for(TransactionOutput x : transactionOutputs) {
            if(x.getReceiver().equals(toFundsKey)) receiversUTXOList.add(x);
            else sendersFilteredList.add(x);
        }
        if(!sendersFilteredList.isEmpty())  keyUXTOValuePairs.put(fromFundsKey, sendersFilteredList);
        if(!receiversUTXOList.isEmpty()) keyUXTOValuePairs.put(toFundsKey, receiversUTXOList);

        return keyUXTOValuePairs;
    }

    private void writeUTXOListToDB(PublicKey key, List<TransactionOutput> UTXO) {
        try {
            ByteArrayOutputStream byt = new ByteArrayOutputStream();
            ObjectOutputStream obs = new ObjectOutputStream(byt);
            obs.writeObject(UTXO);
            byte[] bytes = byt.toByteArray();
            byte[] keyBytes = Util.keyToString(key).getBytes();
            keysAndTransactionDb.put(keyBytes, bytes);
        } catch (IOException | RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    public double getAmount(List<TransactionOutput> transaction) {
        return transaction
                .stream()
                .mapToDouble(x -> x.value)
                .sum();
    }

    public void deleteBlockByKey(String key) {
        try {
            blocksDb.delete(key.getBytes());
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }



}
