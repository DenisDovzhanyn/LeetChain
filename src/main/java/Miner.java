public class Miner implements Runnable {

    @Override
    public void run() {
        BlockChain chain = new BlockChain();

        if (chain.getBlockChain().isEmpty()) {
            Block block = new Block("0", "genesis", "genesis", 1, 500000);
            mineBlock(block);
            chain.add(block);
        }

        while(true) {
            Block block = new Block(chain.getPrevious().hash, "question", "answer", chain.getPrevious().blockNumber + 1, chain.calculateDifficulty());
            mineBlock(block);
            chain.add(block);
        }
    }


    public void mineBlock(Block block) {
        System.out.println("mining block at index: " + block.blockNumber + ", at difficulty: " + block.getDifficulty() + "... ");
        while (!block.isHashFound(block.currentHashValue)) {
            block.mineBlock();
           // System.out.print(block.hash + "\r");

        }
        System.out.println("Nice you've mined a block: " + block.hash);
    }
}
