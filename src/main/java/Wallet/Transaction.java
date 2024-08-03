package Wallet;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
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

    private String calculateID() {
        List<TransactionOutput> concatList = Stream.concat(inputs.stream(), outputs.stream()).collect(Collectors.toList());

         return Util.hash(Util.getMerkleRoot(concatList, (TransactionOutput x) -> x.Id) + type);
    }



    public List<TransactionOutput> gatherUTXOs(PublicKey key) {
        return Ledger.getInstance().getUTXOList(key);
    }

    // we only add UTXOs (transactionOutputs) until we reach the amount the person is trying to send,
    // we then add this to our input list which we will use to remove the inputs from the DB AFTER the transaction has gone through (block mined/signature verified)
    public boolean addUTXOs(float amount, PublicKey sender, PublicKey receiver) {
        float temp = 0;
        if (!type.equals(TransactionType.COINBASE)) {
            List<TransactionOutput> UTXO = gatherUTXOs(sender);

            for (TransactionOutput input : UTXO) {
                if (temp >= amount) break;
                inputs.add(input);
                temp += input.getValue();
            }
            if (temp < amount) return false;
        } else {
            inputs.add(new TransactionOutput(sender,receiver,amount));
        }
        createOutputs(amount, temp, sender, receiver);

        return true;
    }

    public void createOutputs(float amount, float temp, PublicKey sender, PublicKey receiver) {
        outputs.add(new TransactionOutput(sender, receiver, amount));

        if(temp > amount) {
            float change = temp - amount;
            outputs.add(new TransactionOutput(sender, sender, change));
        }
    }

    public void determineType() {
       /* if(!sender.equals(receiver)) type = TransactionType.PEER_TO_PEER;
        if(sender.equals(receiver)) type = TransactionType.COINBASE;

        */

    }

}
