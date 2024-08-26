package Node;

import Miner.Block;
import Node.MessageTypes.BlockListRequest;
import Node.MessageTypes.BlockMessage;
import Node.MessageTypes.PeerListRequest;
import Node.MessageTypes.PeerMessage;
import Wallet.Transaction;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
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
    String ip;

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
            ip = InetAddress.getLocalHost().getHostAddress();
            ObjectOutputStream outBound = new ObjectOutputStream(socketOut.getOutputStream());

            while (true) {
                if (!blockRequests.isEmpty()) {
                    BlockListRequest request = blockRequests.poll();
                    List<Block> blockList= Ledger.getInstance().blockListStartAndEnd(request.start, request.end);
                    BlockMessage message = new BlockMessage(blockList, ip);
                    outBound.writeObject(message);
                }
                if (!peerRequests.isEmpty()) {
                   int amountOfPeers = peerRequests.poll().amountOfPeers;
                   List<Peer> requestedPeers = SocketHandler.readTopNPeers(amountOfPeers);

                   PeerMessage peerMessage = new PeerMessage(requestedPeers, ip);
                   outBound.writeObject(peerMessage);
                }
                // we peek here instead of polling() so we dont remove the object,
                // because this concurrent list is shared with ALL open sockets
                // is there a way to remove it after the last socket has sent it out?
                // how ?
                if (!blocksToOtherNodes.isEmpty()) {
                    List<Block> blockList = new ArrayList<>();
                    blockList.add(blocksToOtherNodes.peek());

                    BlockMessage blockMessage = new BlockMessage(blockList, ip);
                    outBound.writeObject(blockMessage);
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
