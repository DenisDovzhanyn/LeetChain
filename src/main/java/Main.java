public class Main {

    public static void main(String[] args) {
        Thread miner = new Thread(new Miner());
        Thread wallet = new Thread(new Wallet());

        miner.start();
        wallet.start();
    }
}
