package Node.MessageTypes;

import Miner.Block;

import java.util.List;

public class BlockMessage extends BaseMessage {
    List<Block> blocks;

    public BlockMessage(List<Block> blocks, String ip) {
        this.blocks = blocks;
        this.ip = ip;
    }
}
