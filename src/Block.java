import java.security.MessageDigest;
import java.util.Date;

public class Block {
    public String hash;
    public String previousHash;
    private long timeStamp;
    private String question;
    private String answer;
    private int nonce;


    public Block(String previousHash, String answer, String question){
        this.question = question;
        this.answer = answer;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calHash();
    }

    public String calHash(){
        String calculatedHash = Hasher.hash(previousHash + Long.toString(timeStamp) + question + answer + Integer.toString(nonce));

        return calculatedHash;
    }

    public void mineBlock(int difficulty){
        String targetHash = new String(new char[difficulty]).replace('\0', '0');
        while(!hash.substring(0,difficulty).equals(targetHash)){
            nonce++;
            hash = calHash();
        }

        System.out.println("Nice you've mined a block");
    }


}