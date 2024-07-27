import org.bouncycastle.util.encoders.Base32;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.*;
import java.util.ArrayList;

public class Util {

    // takes in input and converts it into a unique hash
    public static String hash(String input) {

        try {
            MessageDigest encrypter = MessageDigest.getInstance("SHA-256");
            byte[] hash = encrypter.digest(input.getBytes("UTF-8"));
            StringBuffer hexHash = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);

                if(hex.length() == 1) hexHash.append('0');
                hexHash.append(hex);
            }

            return hexHash.toString();
        } catch(Exception f){
            throw new RuntimeException(f);
        }
    }


    public static String keyToString(Key key) {
        byte[] publicEncoded = key.getEncoded();

        return Base32.toBase32String(publicEncoded);
    }

    public static byte[] applySignature(PrivateKey privateKey, String input) {
        try {
            Signature signature = Signature.getInstance("ECDSA", "BC");
            signature.initSign(privateKey);

            byte[] inputBytes = input.getBytes();
            signature.update(inputBytes);

            return signature.sign();

        } catch (Exception e) {
            throw new RuntimeException("error applying signature", e);
        }
    }

    public static boolean verifySignature(PublicKey publicKey, String data, byte[] signature){
        try {
            Signature verify = Signature.getInstance("ECDSA", "BC");
            verify.initVerify(publicKey);
            verify.update(data.getBytes());

            return verify.verify(signature);
        } catch (Exception e) {
            throw new RuntimeException("error verifying", e);
        }
    }

}
