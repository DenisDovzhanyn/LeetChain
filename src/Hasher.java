import java.security.MessageDigest;

public class Hasher {

    // takes in input and converts it into a unique hash
    public static String hash(String input){

        try {
            MessageDigest encrypter = MessageDigest.getInstance("SHA-256");
            byte[] hash = encrypter.digest(input.getBytes("UTF-8"));
            StringBuffer hexHash = new StringBuffer();

            for(int i = 0; i < hash.length; i++){
                String hex = Integer.toHexString(0xff & hash[i]);

                if(hex.length() == 1) hexHash.append('0');
                hexHash.append(hex);
            }

            return hexHash.toString();

        } catch(Exception f){
            throw new RuntimeException(f);
        }
    }
}
