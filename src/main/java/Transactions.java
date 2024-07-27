import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class Transactions {
    public String transactionId;  // hash
    public PublicKey sender;
    public PublicKey receiver;
    public float amount;
    public byte[] signature;

    public static int transactionNumber = 0;

    public ArrayList<UTXO> inputs = new ArrayList<UTXO>();
    public ArrayList<UTXO> outputs = new ArrayList<UTXO>();

    public Transactions(PublicKey sender, PublicKey receiver, float amount) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
    }

    private String calculateID() {
        transactionNumber++;
        String hash = Util.hash(Util.keyToString(sender) + Util.keyToString(receiver) + Float.toString(amount) + transactionNumber);

        return hash;
    }

    public void applySig(PrivateKey key) {
        signature = Util.applySignature(key, transactionId);
    }

    public boolean verifySignature(PublicKey sender, String transactionId, byte[] signature) {
        return Util.verifySignature(sender, transactionId, signature);
    }

    public ArrayList<UTXO> gatherUTXOs(PublicKey key){
        return this.inputs = Ledger.getInstance().getUTXOList(key);

    }



}
