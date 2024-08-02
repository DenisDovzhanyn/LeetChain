package Node;

import Miner.Block;
import Wallet.TransactionOutput;
import Utilities.Util;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.*;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public List<TransactionOutput> getUTXOList(PublicKey key) {
        List<TransactionOutput> output;

        try {
            byte[] listByte = keysAndTransactionDb.get(Util.keyToString(key).getBytes());

            ByteArrayInputStream byteArray = new ByteArrayInputStream(listByte);
            ObjectInputStream ois = new ObjectInputStream(byteArray);

            output = (List<TransactionOutput>) ois.readObject();

            return output;
        } catch (RocksDBException | IOException | ClassNotFoundException e) {
            throw new RuntimeException("error getting utxo list", e);
        }
    }

    public void addOrUpdateUTXOList(PublicKey publicKey, List<TransactionOutput> usedUTXOs) {
        List<TransactionOutput> previousUTXOs = getUTXOList(publicKey);
        // helps with filtering and reduces time complexity
        Map<TransactionOutput, Integer> usedUTXOMap = new HashMap<>();
        for (TransactionOutput x : usedUTXOs) {
            usedUTXOMap.put(x, 1);
        }
        List<TransactionOutput> newUTXOs = previousUTXOs
                .stream()
                .filter(x -> usedUTXOMap.get(x) == null)
                .collect(Collectors.toList());

        byte[] utxoList = null;
        byte[] key = Util.keyToString(publicKey).getBytes();

        try {
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            ObjectOutputStream obs = new ObjectOutputStream(byteArray);

            obs.writeObject(usedUTXOs);
            utxoList = byteArray.toByteArray();

            keysAndTransactionDb.put(key, utxoList);

        } catch (RocksDBException | IOException e) {
            throw new RuntimeException("error adding UTXO list to db",e);
        }
    }

    public void deleteBlockByKey(String key) {
        try {
            keysAndTransactionDb.delete(key.getBytes());
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }



}
