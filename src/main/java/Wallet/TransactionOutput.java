package Wallet;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import Utilities.Util;

public class TransactionOutput implements Serializable {
    public String Id;
    private PublicKey sender;
    private PublicKey receiver;
    public double value;
    public byte[] signature;

    public TransactionOutput(PublicKey sender, PublicKey receiver, double value) {
        this.receiver = receiver;
        this.sender = sender;
        this.value = value;
        this.Id = Util.hash(Util.keyToString(sender) + Util.keyToString(receiver) + Double.toString(value));
    }

    public void applySig(PrivateKey key) {
        signature = Util.applySignature(key, Id);
    }



    public boolean verifySignature(PublicKey sender, String transactionId, byte[] signature) {
        return Util.verifySignature(sender, transactionId, signature);
    }

    public PublicKey getReceiver() {
        return receiver;
    }

    public void setReceiver(PublicKey receiver) {
        this.receiver = receiver;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public PublicKey getSender() {
        return sender;
    }
}
