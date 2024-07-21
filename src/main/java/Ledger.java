import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.io.*;

public class Ledger {
    private static Ledger instance;
    private RocksDB db;

    //loads db and assigns it to db variable
    public Ledger(){
        RocksDB.loadLibrary();
        Options options = new Options().setCreateIfMissing(true);
        try {
            this.db = RocksDB.open(options, "LeetChain");
        } catch (RocksDBException e) {
            throw new RuntimeException("Error opening RocksDB", e);
        }
    }

    // allows us to have the db always open
    public static Ledger getInstance(){
        if(instance == null) instance = new Ledger();

        return instance;

    }
    // serializes block and adds it to rocksDB, key will be block number
    public void addBlock(Block block, String key){
        try{
            ByteArrayOutputStream bytArray = new ByteArrayOutputStream();
            ObjectOutputStream obs = new ObjectOutputStream(bytArray);
            obs.writeObject(block);
            byte[] blockBytes = bytArray.toByteArray();
            byte[] keyBytes = key.getBytes();
            db.put(keyBytes, blockBytes);

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
        try(RocksIterator iterator = db.newIterator()){
            for(iterator.seekToFirst(); iterator.isValid(); iterator.next()){
                byte[] value = iterator.value();
                if(value != null) {
                    Block block = deserialize(value);
                    list.add(block);
                }
            }
            return list;
        } catch (Exception e){
            throw new RuntimeException("error building list", e);
        }

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
