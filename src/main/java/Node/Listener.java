package Node;

import Miner.Block;
import Node.MessageTypes.BlockListRequest;
import Node.MessageTypes.BlockMessage;
import Node.MessageTypes.LatestBlockNumber;

import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

// instead of giving all sockets access to one queue we will give them each their own
public class Listener implements Runnable{
    List<ConcurrentLinkedQueue<Object>> receivingSocketsToOutboundSockets;
    ConcurrentLinkedQueue<Object> outBoundMessages;
    ConcurrentLinkedQueue<Object> incomingMessages;
    ConcurrentLinkedQueue<Object> inBoundMessagesToNode;
    ConcurrentLinkedQueue<Socket> newlyConnectedSockets;
    static ThreadPoolExecutor sockets;

    public Listener(ConcurrentLinkedQueue<Socket> newlyConnectedSockets, ConcurrentLinkedQueue<Object> outBoundMessages, ConcurrentLinkedQueue<Object> inboundMessages) {
        this.receivingSocketsToOutboundSockets = new ArrayList<>();
        this.newlyConnectedSockets = newlyConnectedSockets;
        this.outBoundMessages = outBoundMessages;
        this.inBoundMessagesToNode = inboundMessages;
        this.incomingMessages = new ConcurrentLinkedQueue<>();
    }


    @Override
    public void run() {
        sockets = (ThreadPoolExecutor) Executors.newFixedThreadPool(60);
        long startTime = System.currentTimeMillis();
        long tenSeconds = 10000;
        boolean trueOnlyOnceSendRequest = true;
        boolean trueOnlyOnceSortLatestBlockNumber = true;
        List<Object> waitingToBeSentToQueue = new ArrayList<>();
        List<Integer> blockNumbers = new ArrayList<>();

        while (true) {
            if (!newlyConnectedSockets.isEmpty()) {
                assignPersonalQueues();
            }
            // wait for 30 seconds to get some connections
            if (startTime - System.currentTimeMillis() > tenSeconds && trueOnlyOnceSendRequest) {
                trueOnlyOnceSendRequest = false;
                LatestBlockNumber startUpRequest = new LatestBlockNumber(false);
                addToPersonalQueues();
            }

            if (!outBoundMessages.isEmpty()) {
                addToPersonalQueues();
            }


            while (!incomingMessages.isEmpty()) {
                Object object = incomingMessages.poll();

                // once we waited 30 seconds and we reassigned startTime, we wait another 30 seconds but during this time
                // we filter out objects that arent latestblocknumbers
                if (startTime - System.currentTimeMillis() < tenSeconds + tenSeconds && trueOnlyOnceSortLatestBlockNumber) {

                    if (object instanceof LatestBlockNumber){
                        blockNumbers.add(((LatestBlockNumber) object).getLatestBlockNumber());
                    } else {
                        waitingToBeSentToQueue.add(object);
                    }
                } else if (startTime - System.currentTimeMillis() > tenSeconds + tenSeconds && trueOnlyOnceSortLatestBlockNumber) {
                    int blockHighestBlockNumber = Collections.max(blockNumbers);
                    int lowestBlockToAskFor = Ledger.getInstance().getLatestBlock().blockNumber + 1;
                    int amountOfBlocksNeeded = blockHighestBlockNumber - lowestBlockToAskFor;
                    int amountOfBlocksEachNodeWillAskFor = amountOfBlocksNeeded / amountOfConnectedSockets();

                    // now we will need to split the ranges somewhere here so we can ask nodes for different blocks
                    int startingBlock = lowestBlockToAskFor;
                    int endBlock = amountOfBlocksEachNodeWillAskFor;
                    for (ConcurrentLinkedQueue<Object> x : receivingSocketsToOutboundSockets) {
                        if (endBlock > blockHighestBlockNumber) break;
                        List<Block> blocks = Ledger.getInstance().blockListStartAndEnd(startingBlock, endBlock);
                        BlockMessage message = new BlockMessage(blocks, "ip not set yet");
                        x.add(message);

                        startingBlock = endBlock + 1;
                        endBlock += amountOfBlocksEachNodeWillAskFor;
                    }

                    trueOnlyOnceSortLatestBlockNumber = false;
                } else if (!trueOnlyOnceSendRequest) {
                    if (!waitingToBeSentToQueue.isEmpty()) inBoundMessagesToNode.addAll(waitingToBeSentToQueue);

                    inBoundMessagesToNode.add(object);
                }

            }

        }
    }


    public void assignPersonalQueues() {
        ConcurrentLinkedQueue<Object> personalQueue = new ConcurrentLinkedQueue<>();

        Socket socket = newlyConnectedSockets.poll();
        SocketSendingOut outbound = new SocketSendingOut(socket, personalQueue);
        SocketReceiving inbound = new SocketReceiving(socket, personalQueue, incomingMessages);

        receivingSocketsToOutboundSockets.add(personalQueue);
        sockets.submit(inbound);
        sockets.submit(outbound);
    }

    public void addToPersonalQueues() {
        Object object = outBoundMessages.poll();
        receivingSocketsToOutboundSockets.forEach((ConcurrentLinkedQueue<Object> x) -> x.add(object));
    }

    public static int amountOfConnectedSockets() {
        return sockets.getActiveCount() / 2;
    }
}
