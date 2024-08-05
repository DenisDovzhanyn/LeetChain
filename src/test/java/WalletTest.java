import Utilities.Util;
import Wallet.Wallet;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.ConcurrentLinkedQueue;
import Wallet.Transaction;
import Wallet.TransactionType;
public class WalletTest {
    PublicKey publicTestKey;
    PrivateKey privateTestKey;
    Wallet wallet;
    ConcurrentLinkedQueue<Transaction> requiredForWallet;

    @Before
    public void setPublicTestKey() {
        requiredForWallet = new ConcurrentLinkedQueue<Transaction>();

        wallet = new Wallet(requiredForWallet);
        wallet.run();
        publicTestKey = wallet.generateKeyPair();

    }

    @Test
    public void getPrivateFromPublic() {
        privateTestKey = wallet.getPrivateFromPublic(publicTestKey);

        Assert.assertNotNull("private key returning null", privateTestKey);

    }

    @Test
    public void signatureVerifying() {
        wallet.generateTransaction(wallet.getPublicByIndex(0), wallet.getPublicByIndex(0), 5,0, TransactionType.COINBASE);
        String transId = requiredForWallet.peek().outputs.get(0).Id;
        byte[] signature = requiredForWallet.peek().outputs.get(0).signature;
        Assert.assertTrue("signature not verifying", Util.verifySignature(wallet.getPublicByIndex(0),transId, signature));
    }

    @After
    public void deleteKey() {
        Assert.assertTrue("key not deleting properly", wallet.removeKey(publicTestKey));
    }


}
