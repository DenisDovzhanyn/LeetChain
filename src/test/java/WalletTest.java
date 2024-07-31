import Wallet.Wallet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.ConcurrentLinkedQueue;
import Wallet.Transaction;
public class WalletTest {
    PublicKey publicTestKey;
    PrivateKey privateTestKey;
    Wallet wallet;

    @Before
    public void setPublicTestKey() {
        ConcurrentLinkedQueue<Transaction> requiredForWallet = new ConcurrentLinkedQueue<Transaction>();

        wallet = new Wallet(requiredForWallet);
        wallet.run();
        publicTestKey = wallet.generateKeyPair();

    }

    @Test
    public void getPrivateFromPublic() {
        privateTestKey = wallet.getPrivateFromPublic(publicTestKey);

        Assert.assertNotNull("private key returning null", privateTestKey);
        Assert.assertTrue("key not deleting properly", wallet.removeKey(publicTestKey));
    }
}
