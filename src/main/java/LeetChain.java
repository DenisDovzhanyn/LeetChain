import java.util.ArrayList;


public class LeetChain {

    public static void main(String[] args){
        BlockChain chain = new BlockChain();


        chain.add(new Block("0","HELP ME IM GOING TO DIE","for int i = 0"));
        //chain.get(0).mineBlock();

        //chain.add(new Block(chain.get(chain.size()-1).hash, "LOLOL", "hmmm idk"));
        //chain.get(1).mineBlock();

       // System.out.println(isChainValid());
    }

   /* public static boolean isChainValid(){
        Block previous;
        Block current;

        for(int i = 1; i < chain.size(); i++){

            current = chain.get(i);
            previous = chain.get(i -1);

            if(!previous.hash.equals(current.previousHash)){
                System.out.println("previous hash tampered: ");
                System.out.println(previous.hash);
                System.out.println(current.previousHash);
                return false;
            }

            if(!current.hash.equals(current.calHash())){
                System.out.println("current hash tampered");
                System.out.println(current.hash);
                System.out.println(current.calHash());
                return false;
            }

        }
        return true;
    }

    */
}
