import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base32;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Wallet {
    private List<PublicKey> publicKeys = new ArrayList<PublicKey>();



    public Wallet(){
        Security.addProvider(new BouncyCastleProvider());
        this.publicKeys = createListFromFiles();

    }

    public void generateKeyPair() {
        try {
            PublicKey publicKey;
            PrivateKey privateKey;


            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("EC", "BC");
            keyPairGen.initialize(new ECGenParameterSpec("P-256"));
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
    public void writeKeyToFile(PublicKey publicKey, PrivateKey privateKey) {
        try {
            String path = "Keys/" + Util.keyToString(publicKey);

            File keyPair = new File(path);
            keyPair.createNewFile();

            PrintWriter writer = new PrintWriter(keyPair);
            writer.println(Util.keyToString(privateKey));
            writer.close();

        } catch(IOException e) {
            throw new RuntimeException("Error writing key to file", e);
        }
    }

        // if array comes back empty == no keys, so we should generate keypair otherwise we go through the file names and convert them to public key objects
    public ArrayList<PublicKey> createListFromFiles() {
        ArrayList<PublicKey> listOfKeys = new ArrayList<PublicKey>();
        File keyFolder = new File("Keys");
        keyFolder.mkdir();
        File[] keys = new File("Keys/").listFiles();

        if (keys.length == 0) {
            generateKeyPair();
        } else {
            try {
                for (File file : keys) {
                    byte[] encoded = Base32.decode(file.getName());

                    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
                    KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
                    PublicKey toKey = keyFactory.generatePublic(keySpec);

                    listOfKeys.add(toKey);
                }
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
                throw new RuntimeException(e);
            }
        }
        return listOfKeys;
    }
        /* take in publicKey, convert that to string, compare string with filenames in Keys folder
            if publicKey matches a file name, go into that file, then convert the data in file (private key in string form)
            and convert that into a PrivateKey which can be used for signing transactions
         */
    public PrivateKey getPrivateFromPublic(PublicKey pk){
        String searchFor = Util.keyToString(pk);
        File[] keyDir = new File("Keys/").listFiles();

        for (File file : keyDir) {
            if (searchFor.equals(file.getName())) {
                try {
                    Scanner reader = new Scanner(file);
                    String encodedString = reader.nextLine();
                    byte[] privateKeyBytes = Base32.decode(encodedString);
                    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                    KeyFactory keyFac = KeyFactory.getInstance("EC", "BC");

                    PrivateKey decodedKey = keyFac.generatePrivate(keySpec);

                    return decodedKey;
                } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
                    throw new RuntimeException("error reading private key", e);
                }
            }
        }

        System.out.println("no key returned");
        return null;
    }




    public PublicKey getPublicByIndex(int index){
        return publicKeys.get(index);
    }

}
