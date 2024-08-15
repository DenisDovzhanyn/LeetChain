package Node;

import Miner.Block;
import Node.RequestTypes.BlockListRequest;
import Node.RequestTypes.PeerListRequest;
import Wallet.Transaction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SocketSendingOut implements Runnable{
    Socket socketOut;
    ConcurrentLinkedQueue<Block> blocksToOtherNodes;
    ConcurrentLinkedQueue<Transaction> transactionsToOtherNodes;
    ConcurrentLinkedQueue<BlockListRequest> blockRequests;
    ConcurrentLinkedQueue<PeerListRequest> peerRequests;

    public SocketSendingOut(Socket outBound, ConcurrentLinkedQueue<Block> blocksToOtherNodes, ConcurrentLinkedQueue<Transaction> transactionsToOtherNodes,
                            ConcurrentLinkedQueue<BlockListRequest> blockRequests, ConcurrentLinkedQueue<PeerListRequest> peerRequests) {
        this.socketOut = outBound;
        this.blocksToOtherNodes = blocksToOtherNodes;
        this.transactionsToOtherNodes = transactionsToOtherNodes;
        this.blockRequests = blockRequests;
        this.peerRequests = peerRequests;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream outBound = new ObjectOutputStream(socketOut.getOutputStream());

            while (true) {
                if (!blockRequests.isEmpty()) {
                    BlockListRequest request = blockRequests.poll();
                    List<Block> requestedBlocklist = Ledger.getInstance().blockListStartAndEnd(request.start, request.end);

                    for (Block block : requestedBlocklist) {
                        outBound.writeObject(block);
                    }
                }
                if (!blocksToOtherNodes.isEmpty()) {
                    outBound.writeObject(blocksToOtherNodes.peek());
                }
                if(!transactionsToOtherNodes.isEmpty()) {
                    outBound.writeObject(transactionsToOtherNodes.peek());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
