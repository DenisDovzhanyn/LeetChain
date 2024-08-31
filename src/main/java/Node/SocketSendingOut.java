package Node;

import Miner.Block;
import Node.MessageTypes.*;
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
    ConcurrentLinkedQueue<BlockMessage> blocksToOtherNodes;
    ConcurrentLinkedQueue<TransactionMessage> transactionsToOtherNodes;
    ConcurrentLinkedQueue<BlockListRequest> blockRequests;
    ConcurrentLinkedQueue<PeerListRequest> peerRequests;
    ConcurrentLinkedQueue<LatestBlockNumber> latestNumber;
    String ip;

    public SocketSendingOut(Socket outBound, ConcurrentLinkedQueue<BlockListRequest> blockRequests, ConcurrentLinkedQueue<PeerListRequest> peerRequests,
                            ConcurrentLinkedQueue<LatestBlockNumber> latestNumber) {
        this.socketOut = outBound;
        this.blockRequests = blockRequests;
        this.peerRequests = peerRequests;
        this.latestNumber = latestNumber;
    }

    public void setBlocksToOtherNodes(ConcurrentLinkedQueue<BlockMessage> blocksToOtherNodes) {
        this.blocksToOtherNodes = blocksToOtherNodes;
    }

    public void setTransactionsToOtherNodes(ConcurrentLinkedQueue<TransactionMessage> transactionsToOtherNodes) {
        this.transactionsToOtherNodes = transactionsToOtherNodes;
    }

    @Override
    public void run() {
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
            ObjectOutputStream outBound = new ObjectOutputStream(socketOut.getOutputStream());

            while (true) {
                if (!latestNumber.isEmpty()) {
                    LatestBlockNumber request = latestNumber.poll();
                    int latest = Ledger.getInstance().getLatestBlock().blockNumber;
                    request.setLatestBlockNumber(latest);
                    request.setIsRequest(false);
                    outBound.writeObject(request);
                }
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

                if (!blocksToOtherNodes.isEmpty()) {
                    BlockMessage blockMessage = blocksToOtherNodes.poll();
                    blockMessage.setIp(ip);
                    outBound.writeObject(blockMessage);
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
