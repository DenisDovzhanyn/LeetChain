import java.security.PublicKey;

public class Transactions {
    public String transactionId;  // hash
    public PublicKey sender;
    public PublicKey receiver;
    public float amount;
    public byte[] signature;

    private static int transactionNumber = 0;

    public Transactions(PublicKey sender, PublicKey receiver, float amount){
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.transactionId = calculateID();
    }




    private String calculateID() {
        transactionNumber++;
        String hash = Util.hash(Util.keyToString(sender) + Util.keyToString(receiver) + Float.toString(amount) + Integer.toString(transactionNumber));
        return hash;


    }

}
