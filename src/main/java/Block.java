import java.io.Serializable;
import java.util.Date;

public class Block implements Serializable {
    public String hash;
    public String previousHash;
    private long timeStamp;
    private String question;
    private String answer;
    private int nonce;
    private int difficulty = 3;
    public int blockNumber = 0;




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
        String calculatedHash = Hasher.hash(previousHash + Long.toString(timeStamp) + question + answer
                + Integer.toString(difficulty) + Integer.toString(nonce));

        return calculatedHash;
    }

    public void mineBlock(){
        String targetHash = new String(new char[difficulty]).replace('\0', '0');
        while(!hash.substring(0,difficulty).equals(targetHash)){
            nonce++;
            hash = calHash();
            System.out.println(hash);
        }

        System.out.println("Nice you've mined a block: " + hash);

    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getDifficulty() {
        return difficulty;
    }
}