import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class Transaction {
    public String transactionId;  // hash
    public PublicKey sender;
    public PublicKey receiver;
    public float amount;
    public byte[] signature;

    public static int transactionNumber = 0;

    public ArrayList<TransactionOutput> inputs = new ArrayList<TransactionOutput>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

    public Transaction(PublicKey sender, PublicKey receiver, float amount) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        transactionId = calculateID();
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

    public ArrayList<TransactionOutput> gatherUTXOs(PublicKey key) {
        return Ledger.getInstance().getUTXOList(key);
    }

    // we only add UTXOs (transactionOutputs) until we reach the amount the person is trying to send,
    // we then add this to our input list which we will use to remove the inputs from the DB AFTER the transaction has gone through (block mined/signature verified)
    public boolean doYouHaveEnoughCoin(PublicKey key) {
        ArrayList<TransactionOutput> UTXO = gatherUTXOs(key);
        float temp = 0;

        for (TransactionOutput input : UTXO) {
            if(temp >= amount) break;
            inputs.add(input);
            temp += input.getValue();
        }

        return (temp >= amount);
    }

}
