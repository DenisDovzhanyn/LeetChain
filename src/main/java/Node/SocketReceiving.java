package Node;

import Miner.BlockChain;
import Node.MessageTypes.*;
import Wallet.Transaction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SocketReceiving implements Runnable {
    Socket receiving;
    ConcurrentLinkedQueue<TransactionMessage> incomingTransactions;
    ConcurrentLinkedQueue<BlockMessage> incomingBlocks;
    ConcurrentLinkedQueue<BlockListRequest> blockRequest;
    ConcurrentLinkedQueue<PeerListRequest> peerListRequest;
    ConcurrentLinkedQueue<LatestBlockNumber> latestBlockNumberRequest;
    String ip;

    public SocketReceiving(Socket receiving,ConcurrentLinkedQueue<TransactionMessage> incomingTransactions, ConcurrentLinkedQueue<BlockMessage> incomingBlocks,
                           ConcurrentLinkedQueue<BlockListRequest> blockRequest, ConcurrentLinkedQueue<PeerListRequest> peerRequests,
                           ConcurrentLinkedQueue<LatestBlockNumber> numberRequest) {
        this.receiving = receiving;
        this.incomingTransactions = incomingTransactions;
        this.incomingBlocks = incomingBlocks;
        this.blockRequest = blockRequest;
        this.peerListRequest = peerRequests;
        this.latestBlockNumberRequest = numberRequest;
    }

    @Override
    public void run() {
        try {
            ip = InetAddress.getLocalHost().getHostAddress();

            ObjectInputStream inputStream = new ObjectInputStream(receiving.getInputStream());

            while (true) {
                Object object = inputStream.readObject();

                // from testing in a different repository, when an object is received and then the connection closes
                // this will throw an error " connnection reset", upon getting this error we should close this thread ( including the sending out thread aswell )
                // how will we handle this exception and close this thread ??????
                // even so if we throw an exception to close this thread, how will we close the sending out thread?
                // Should I just let it die on its own when it tries to send out info?

                if (object instanceof BlockListRequest) {
                    BlockListRequest request = (BlockListRequest) object;
                    blockRequest.add(request);
                }
                if (object instanceof PeerListRequest) {
                    PeerListRequest request = (PeerListRequest) object;
                    peerListRequest.add(request);
                }
                if (object instanceof LatestBlockNumber) {
                    LatestBlockNumber latestNumber = (LatestBlockNumber) object;
                    if (latestNumber.getIsRequest()) {
                        latestBlockNumberRequest.add(latestNumber);
                    } else {
                        int start = Ledger.getInstance().getLatestBlock().blockNumber + 1;
                        BlockListRequest request = new BlockListRequest(start, latestNumber.getLatestBlockNumber(), false, ip);
                        // if its not a request, then we want to ask MULTIPLE connected people for a certain range of blocks, but How do I send it back up
                        // and split it amongst multiple sockets?
                    }
                }
                if (object instanceof BlockMessage) {
                    incomingBlocks.add((BlockMessage) object);
                }
                if (object instanceof TransactionMessage) {
                    incomingTransactions.add((TransactionMessage) object);
                }
                if (object instanceof PeerMessage) {
                    PeerMessage message = (PeerMessage) object;
                    SocketHandler.peers.addAll(message.getPeers());

                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("object not formatted correctly or connection terminated");
        }
    }
}
