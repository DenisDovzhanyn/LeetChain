package Miner;
import Wallet.Transaction;
import Utilities.Util;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Block implements Serializable {
    public String hash;
    public String previousHash;
    private long timeStamp;
    public String merkleRoot;
    public List<Transaction> transactionlist = new ArrayList<>();
    private int nonce;
    private int difficulty = 1;
    public int blockNumber = 1;


    public transient BigInteger currentHashValue;


    public Block(String previousHash, int blockNumber, int difficulty, List<Transaction> transactionlist){
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calHash();
        this.blockNumber = blockNumber;
        this.difficulty = difficulty;
        this.transactionlist = transactionlist;
        this.merkleRoot = Util.getMerkleRoot(transactionlist, (Transaction x) -> x.id);
    }

    public Block(String previousHash, int blockNumber, int difficulty){
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calHash();
        this.blockNumber = blockNumber;
        this.difficulty = difficulty;
    }

    public Block(){

    }


    public String calHash(){
        String calculatedHash = Util.hash(previousHash + Long.toString(timeStamp)
                + Integer.toString(difficulty) + merkleRoot + Integer.toString(nonce));

        return calculatedHash;
    }

    public void mineBlock(){
            nonce++;
            hash = calHash();
            currentHashValue = new BigInteger(hash,16);
    }

    public boolean isHashFound(BigInteger actual){

        if(actual == null) return false;
        if(actual.compareTo(calcTarget()) == -1) return true;

        return false;
    }

    public BigInteger calcTarget(){
        return (new BigInteger("16"))
                .pow(64)
                .divide(BigInteger.valueOf(difficulty))
                .subtract(BigInteger.valueOf(1));
    }

    public static double calculateReward(int blockNumber) {
        final int MAX_BLOCK = 1000000;
        final double INITIAL_REWARD = 100;
        final double K = 10; // decay constant
        final double X = 1.5f; //polynomial influence

        if(blockNumber > MAX_BLOCK) return 0;

        double reward = INITIAL_REWARD * Math.exp(-K * Math.pow((double)blockNumber / MAX_BLOCK, X));
        return reward;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getDifficulty() {
        return difficulty;
    }
}