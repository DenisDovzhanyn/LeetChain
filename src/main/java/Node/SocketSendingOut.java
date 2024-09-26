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
    ConcurrentLinkedQueue<Object> outBoundMessages;
    String ip;

    public SocketSendingOut(Socket outBound, ConcurrentLinkedQueue<Object> outBoundMessages) {
        this.socketOut = outBound;
        this.outBoundMessages = outBoundMessages;
    }

    @Override
    public void run() {
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
            ObjectOutputStream outBound = new ObjectOutputStream(socketOut.getOutputStream());

            // on start up will create a single request and send it out, we will need to somehow pool together all the
            // highest block numbers we get back then get either the most common number OR highest number
            while (true) {
                if (!outBoundMessages.isEmpty()) {
                    Object object = outBoundMessages.poll();

                    if ( object instanceof LatestBlockNumber){
                        LatestBlockNumber request = (LatestBlockNumber) object;

                        if (request.getIsRequest()) {
                            int latest = Ledger.getInstance().getLatestBlock().blockNumber;
                            request.setLatestBlockNumber(latest);
                            request.setIsRequest(false);
                        } else {
                            request.setIsRequest(true);
                        }
                        outBound.writeObject(request);
                        continue;
                    }

                    if (object instanceof BlockListRequest) {
                        BlockListRequest request = (BlockListRequest) object;
                        if (request.isRequest()) {
                            List<Block> blockList = Ledger.getInstance().blockListStartAndEnd(request.start, request.end);
                            BlockMessage message = new BlockMessage(blockList, ip);
                            outBound.writeObject(message);
                        } else {
                            request.setRequest(true);
                            request.setIp(ip);
                            outBound.writeObject(request);
                        }
                        continue;
                    }
                    if (object instanceof PeerListRequest) {
                        PeerListRequest amountOfPeers = (PeerListRequest) object;
                        List<Peer> requestedPeers = SocketHandler.readTopNPeers(amountOfPeers.amountOfPeers);

                        PeerMessage peerMessage = new PeerMessage(requestedPeers, ip);
                        outBound.writeObject(peerMessage);
                        continue;
                    }

                    if (object instanceof BlockMessage) {
                        BlockMessage blockMessage = (BlockMessage) object;
                        blockMessage.setIp(ip);
                        outBound.writeObject(blockMessage);
                        continue;
                    }
                    if (object instanceof TransactionMessage) {
                        TransactionMessage message = (TransactionMessage) object;
                        message.setIp(ip);
                        outBound.writeObject(message);
                    }
                }


            }
        } catch (IOException e) {
            System.out.println("Connection terminated closing thread");
        }
    }


}
