import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base32;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.List;

public class Wallet {
    private List<PublicKey> publicKeys = new ArrayList<PublicKey>();



    public Wallet(){
        generateKeyPair();
    }

    public void generateKeyPair() {
        try {
            PublicKey publicKey;
            PrivateKey privateKey;

            Security.addProvider(new BouncyCastleProvider());
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("EC", "BC");
            keyPairGen.initialize(new ECGenParameterSpec("P-256"));
            keyPairGen.generateKeyPair();
            KeyPair keyPair = keyPairGen.generateKeyPair();

            publicKey = keyPair.getPublic();
            privateKey = keyPair.getPrivate();
            publicKeys.add(keyPair.getPublic());

            writeKeyToFile(publicKey, privateKey);
        } catch(GeneralSecurityException e){
            throw new RuntimeException("error generating key pair", e);
        }
    }
        // we use Base32 instead of Base64 because 64 uses / which can cause problems when creating files
    public void writeKeyToFile(PublicKey publicKey, PrivateKey privateKey){
        try {
            byte[] publicEncoded = publicKey.getEncoded();
            byte[] privateEncoded = privateKey.getEncoded();
            String path = "Keys/" + Base32.toBase32String(publicEncoded);

            File keyPair = new File(path);
            keyPair.createNewFile();

            PrintWriter writer = new PrintWriter(keyPair);
            writer.println(Base32.toBase32String(privateEncoded));
            writer.close();

        } catch(IOException e) {
            throw new RuntimeException("Error writing key to file", e);
        }
    }
}
