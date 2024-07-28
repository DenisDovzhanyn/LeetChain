import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

public class Block implements Serializable {
    public String hash;
    public String previousHash;
    private long timeStamp;
    private String question;
    private String answer;
    private int nonce;
    private int difficulty = 1;
    public int blockNumber = 1;


    public transient BigInteger currentHashValue;


    public Block(String previousHash, String answer, String question, int blockNumber, int difficulty){
        this.question = question;
        this.answer = answer;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calHash();
        this.blockNumber = blockNumber;
        this.difficulty = difficulty;


    }

    public Block(){

    }


    public String calHash(){
        String calculatedHash = Util.hash(previousHash + Long.toString(timeStamp) + question + answer
                + Integer.toString(difficulty) + Integer.toString(nonce));

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

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getDifficulty() {
        return difficulty;
    }
}