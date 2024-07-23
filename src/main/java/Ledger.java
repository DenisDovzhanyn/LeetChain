import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Ledger {
    private static Ledger instance;
    private RocksDB db;

    //loads db and assigns it to db variable
    public Ledger(){
        RocksDB.loadLibrary();
        Options options = new Options().setCreateIfMissing(true);
        try {
            this.db = RocksDB.open(options, "DataBase");
        } catch (RocksDBException e) {
            throw new RuntimeException("Error opening RocksDB", e);
        }
    }

    // allows us to have the db always open
    public static Ledger getInstance(){
        if(instance == null) instance = new Ledger();

        return instance;

    }

    // REMINDER WE NEED TO CHANGE THE KEY TO BE THE BLOCKS HASH AND ADD A NEW ENTRY IN DB WITH KEY LATESTHASH THAT HOLDS
    //  THE VALUE OF THE LATEST HASH, SO WE CAN USE THAT HASH VALUE TO FIND THE ACTUAL ENTRY FOR THE LATEST BLOCK
    // serializes block and adds it to rocksDB, key will be block number
    public void addBlock(Block block, String key){
        try{
            ByteArrayOutputStream bytArray = new ByteArrayOutputStream();
            ObjectOutputStream obs = new ObjectOutputStream(bytArray);
            obs.writeObject(block);

            byte[] blockBytes = bytArray.toByteArray();
            byte[] keyBytes = key.getBytes();
            byte[] latestBlockHashKey = "latestBlockHash".getBytes();
            byte[] latestBlockHash = block.hash.getBytes();

            db.put(keyBytes, blockBytes);
            db.put(latestBlockHashKey,latestBlockHash);

        }catch (IOException | RocksDBException e){
            throw new RuntimeException("error adding to db", e);
        }
    }

    // returns and deserializes block, returning null if no block was found/doesnt exist
    public Block getBlockByKey(String key){
        Block gottenBlock = null;
        try{
            byte[] blockBytes = db.get(key.getBytes());

            if(blockBytes != null){
                 gottenBlock = deserialize(blockBytes);
            } else{
                System.out.println("ITS NULL ITS NULL ITS NULL ITS NULL ITS NULL ITS NULL");
            }
        } catch (RocksDBException f){
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
                    byte[] value;

                    if (i == 0){

                        byte[] latestHash = db.get(latestBlockHashKey.getBytes());
                        value = db.get(latestHash);

                    } else {
                        value = db.get(previousHash.getBytes());
                    }

                    if(value != null) {
                        Block block = deserialize(value);
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

    public Block deserialize(byte[] value){
        try(ByteArrayInputStream bytArray = new ByteArrayInputStream(value);
            ObjectInputStream ois = new ObjectInputStream(bytArray)){
            Block block = (Block) ois.readObject();
            return block;
        } catch(IOException | ClassNotFoundException e ){
            throw new RuntimeException("error deserializing", e);
        }
    }


}
