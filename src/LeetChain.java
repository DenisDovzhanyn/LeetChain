import java.util.ArrayList;

public class LeetChain {

    public static ArrayList<Block> chain = new ArrayList<Block>();

    public static void main(String[] args){
        chain.add(new Block("0","HELP ME IM GOING TO DIE","for int i = 0"));
        chain.get(0).mineBlock(5);

        chain.add(new Block(chain.get(chain.size()-1).hash, "LOLOL", "hmmm idk"));
        chain.get(1).mineBlock(6);

        System.out.println(isChainValid());
    }

    public static boolean isChainValid(){
        Block previous;
        Block current;

        for(int i = 1; i < chain.size(); i++){

            current = chain.get(i);
            previous = chain.get(i -1);

            if(!previous.hash.equals(current.previousHash)){
                System.out.println("previous hash tampered");
                return false;
            }

            if(!current.hash.equals(current.calHash())){
                System.out.println("current hash tampered");
            }



        }
        return true;
    }
}
