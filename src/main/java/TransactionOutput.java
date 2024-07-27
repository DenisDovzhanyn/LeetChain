import java.security.PublicKey;

public class TransactionOutput {
    public String hashId;
    public PublicKey receiver;
    public float value;

    public TransactionOutput(PublicKey receiver, float value) {
        this.receiver = receiver;
        this.value = value;
        this.hashId = Util.hash(Util.keyToString(receiver) + Float.toString(value));
    }

    public boolean isItToMe(PublicKey key){
        return key.equals(receiver);
    }


}
