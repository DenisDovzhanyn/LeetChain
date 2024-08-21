package Node;

import Miner.Block;
import Node.RequestTypes.BlockListRequest;
import Node.RequestTypes.PeerListRequest;
import Wallet.Transaction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
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

                    // why for loop here going through list when I can just send the whole list?
                    for (Block block : requestedBlocklist) {
                        outBound.writeObject(block);
                    }
                }
                if (!peerRequests.isEmpty()) {
                   int amountOfPeers = peerRequests.poll().amountOfPeers;
                   List<Peer> requestedPeers = SocketHandler.readTopNPeers(amountOfPeers);

                   for (Peer peer : requestedPeers) {
                       outBound.writeObject(peer);
                   }
                }
                // we peek here instead of polling() so we dont remove the object,
                // because this concurrent list is shared with ALL open sockets
                // is there a way to remove it after the last socket has sent it out?
                // how ?
                if (!blocksToOtherNodes.isEmpty()) {
                    outBound.writeObject(blocksToOtherNodes.peek());
                    // sketchy but it might work? we wait for 1 second and then remove it? this gives enough times for all threads to peak at block
                    wait(1000);
                    blocksToOtherNodes.poll();
                }
                if(!transactionsToOtherNodes.isEmpty()) {
                    outBound.writeObject(transactionsToOtherNodes.peek());
                    wait(1000);
                    transactionsToOtherNodes.poll();
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Connection terminated");
        }
    }


}
