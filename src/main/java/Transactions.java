import java.security.PublicKey;

public class Transactions {
    public String transactionId;  // hash
    public PublicKey sender;
    public PublicKey receiver;
    public float amount;
    public byte[] signature;

    private static int transactionNumber = 0;





    private String calculateID() {
        transactionNumber++;

        return Hasher.hash()

    }

}
