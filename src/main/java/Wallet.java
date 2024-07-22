import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base32;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.*;
import java.security.spec.ECGenParameterSpec;

public class Wallet {
    private PublicKey publicKey;
    private PrivateKey privateKey;


    public Wallet(){
        generateKeyPair();
    }

    public void generateKeyPair() {
        try {
            Security.addProvider(new BouncyCastleProvider());
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("EC", "BC");
            keyPairGen.initialize(new ECGenParameterSpec("P-256"));
            keyPairGen.generateKeyPair();
            KeyPair keyPair = keyPairGen.generateKeyPair();

            this.publicKey = keyPair.getPublic();
            this.privateKey = keyPair.getPrivate();
            writeKeyToFile(publicKey, privateKey);
        } catch(GeneralSecurityException e){
            throw new RuntimeException("error generating key pair", e);
        }
    }
        // we use Base32 instead of Base64 because 64 uses / which can cause problems when creating files
    public void writeKeyToFile(PublicKey publicKey, PrivateKey privateKey){
        try{
            byte[] bytes = publicKey.getEncoded();
            String path = "Keys/" + Base32.toBase32String(bytes);

            File keyPair = new File(path);
            keyPair.createNewFile();

            PrintWriter writer = new PrintWriter(keyPair);
            writer.println(privateKey.toString());
            writer.close();
        } catch(IOException e){
            throw new RuntimeException("Error writing key to file", e);
        }
    }
}
