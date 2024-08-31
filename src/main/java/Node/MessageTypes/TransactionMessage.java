package Node.MessageTypes;

import Wallet.Transaction;

public class TransactionMessage extends BaseMessage{
    Transaction transaction;

    public TransactionMessage(Transaction transaction, String ip) {
        this.transaction = transaction;
        this.ip = ip;
    }
}
