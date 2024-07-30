import org.bouncycastle.util.encoders.Base32;

import java.security.*;
import java.util.ArrayList;
import java.util.List;

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

    public static String getMerkleRoot(List<Transaction> transactions) {
        List<String> endResult = new ArrayList<>();

        for(Transaction x : transactions){
            endResult.add(x.transactionId);
        }

        return getMerkleRootFromHashes(endResult);
    }

    // tried to make this overloaded but java is stupid and thinks List<Transaction> is the same as List<String> and will not let it compile
    private static String getMerkleRootFromHashes(List<String> transactionHashes) {
        if(transactionHashes.size() == 1) return transactionHashes.get(0);

        List<String> parentList = new ArrayList<>();

        for (int i = 0; i < transactionHashes.size(); i+= 2) {
            String hash = hash(transactionHashes.get(i) + transactionHashes.get(i+1));
            parentList.add(hash);
        }

        if (transactionHashes.size() % 2 == 1) {
            String duplicateLast = transactionHashes.get(transactionHashes.size()-1);
            String hashed = hash(duplicateLast + duplicateLast);
            parentList.add(hashed);
        }

        return getMerkleRootFromHashes(parentList);

    }



}
