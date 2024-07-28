import java.security.PublicKey;

public class TransactionOutput {
    private String hashId;
    private PublicKey receiver;
    private float value;

    public TransactionOutput(PublicKey receiver, float value) {
        this.receiver = receiver;
        this.value = value;
        this.hashId = Util.hash(Util.keyToString(receiver) + Float.toString(value));
    }


    public PublicKey getReceiver() {
        return receiver;
    }

    public void setReceiver(PublicKey receiver) {
        this.receiver = receiver;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
