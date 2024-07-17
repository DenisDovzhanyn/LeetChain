import java.util.ArrayList;

public class LeetChain {

    public static ArrayList<Block> chain = new ArrayList<Block>();

    public static void main(String[] args){
        chain.add(new Block("0","HELP ME IM GOING TO DIE","for int i = 0"));
        chain.add(new Block(chain.get(chain.size()-1).hash, "LOLOL", "hmmm idk"));
        System.out.println("Hash for block 1: " + chain.get(0).hash);
        System.out.println("Hash for block 2: " + chain.get(1).hash);
        if(!chain.get(0).hash.equals(chain.get(1).previousHash) || !chain.get(1).hash.equals(chain.get(1).calHash())){
            System.out.println("Oh hell no! NO tampering");
        } else {
            System.out.println("Thanks for not tampering!");
        }
    }
}
