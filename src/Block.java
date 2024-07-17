import java.security.MessageDigest;
import java.util.Date;

public class Block {
    public String hash;
    public String previousHash;
    private long timeStamp;
    private String question;
    private String answer;


    public Block(String previousHash, String answer, String question){
        this.question = question;
        this.answer = answer;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calHash();
    }

    public String calHash(){
        String calculatedHash = Hasher.hash(previousHash + Long.toString(timeStamp) + question + answer);

        return calculatedHash;
    }


}