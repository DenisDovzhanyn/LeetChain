package Wallet;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import Utilities.Util;
import Node.Ledger;

public class Transaction implements Serializable {
    public String id;  // hash
    public TransactionType type;


    public List<TransactionOutput> inputs = new ArrayList<TransactionOutput>();
    public List<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

    public Transaction(TransactionType type) {
        this.type = type;
    }

    public String calculateID() {
        List<TransactionOutput> concatList = Stream.concat(inputs.stream(), outputs.stream()).collect(Collectors.toList());

         return Util.hash(Util.getMerkleRoot(concatList, (TransactionOutput x) -> x.Id) + type);
    }



    public List<TransactionOutput> gatherUTXOs(PublicKey key) {
        return Ledger.getInstance().getUTXOListByPublicKey(key);
    }

    // we only add UTXOs (transactionOutputs) until we reach the amount the person is trying to send,
    // we then add this to our input list which we will use to remove the inputs from the DB AFTER the transaction has gone through (block mined/signature verified)
    public boolean addUTXOs(double amount, double fee, PublicKey sender, PublicKey receiver) {
        double temp = 0;


        if (!type.equals(TransactionType.COINBASE)) {

            List<TransactionOutput> UTXO = gatherUTXOs(sender);

            for (TransactionOutput input : UTXO) {
                if (temp >= amount + fee) break;
                inputs.add(input);
                temp += input.getValue();
            }

            if (temp < amount + fee) return false;
        } else {
            inputs.add(new TransactionOutput(sender, receiver, amount));
        }
        createOutputs(amount, temp, fee, sender, receiver);

        return true;
    }

    public void createOutputs(double amount, double temp, double fee, PublicKey sender, PublicKey receiver) {
        outputs.add(new TransactionOutput(sender, receiver, amount));

        if (temp > amount + fee) {
            double change = temp - (amount + fee);
            outputs.add(new TransactionOutput(sender, sender, change));
        }
    }

    //checks if the inputs used are actually usable UTXOs
    public boolean verifyInputs() {
        List<TransactionOutput> sendersUTXOSFromDB = Ledger.getInstance().getUTXOListByPublicKey(outputs.get(0).getSender());
        Set<TransactionOutput> inputSet = new HashSet<TransactionOutput>(inputs);

        for (TransactionOutput x : sendersUTXOSFromDB) {
            if (inputSet.contains(x)) inputSet.remove(x);
        }
        if (!inputSet.isEmpty()) return false;

        return true;
    }

}
