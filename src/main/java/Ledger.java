import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.io.*;

public class Ledger {
    private static Ledger instance;
    private RocksDB db;

    public Ledger(){
        RocksDB.loadLibrary();
        Options options = new Options().setCreateIfMissing(true);
        try {
            this.db = RocksDB.open(options, "LeetChain");
        } catch (RocksDBException e) {
            throw new RuntimeException("Error opening RocksDB", e);
        }
    }


    public static Ledger getInstance(){
        if(instance == null) instance = new Ledger();

        return instance;

    }

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

    public Block getBlock(String key){
        Block gottenBlock = null;
        try{
            byte[] blockBytes = db.get(key.getBytes());

            if(blockBytes != null){
                ByteArrayInputStream byteInput = new ByteArrayInputStream(blockBytes);
                ObjectInputStream ois = new ObjectInputStream(byteInput);
                 gottenBlock = (Block) ois.readObject();
            } else{
                System.out.println("ITS NULL ITS NULL ITS NULL ITS NULL ITS NULL ITS NULL");
            }
        } catch (IOException | RocksDBException | ClassNotFoundException f){
            throw new RuntimeException("error getting block",f);
        }
        return gottenBlock;
    }

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
