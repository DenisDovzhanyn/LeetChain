import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

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
        Block gottenBlock = new Block();
        try{
            byte[] blockBytes = db.get(key.getBytes());

            if(blockBytes != null){
                ByteArrayInputStream byteInput = new ByteArrayInputStream(blockBytes);
                ObjectInputStream ois = new ObjectInputStream(byteInput);
                 gottenBlock = (Block) ois.readObject();
            }
        } catch (IOException | RocksDBException | ClassNotFoundException f){
            throw new RuntimeException("error getting block OR block doesnt exist",f);
        }
        System.out.println(gottenBlock.hash + " " + gottenBlock.previousHash);
        return gottenBlock;
    }

    public void generateList(){

    }
}
