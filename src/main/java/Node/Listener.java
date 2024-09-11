package Node;

import Node.MessageTypes.LatestBlockNumber;

import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

// instead of giving all sockets access to one queue we will give them each their own
public class Listener implements Runnable{
    List<ConcurrentLinkedQueue<Object>> personalQueues;
    ConcurrentLinkedQueue<Object> outBoundMessages;
    ConcurrentLinkedQueue<Object> inBoundMessages;
    ConcurrentLinkedQueue<Object> inBoundMessagesToNode;
    ConcurrentLinkedQueue<Socket> newlyConnectedSockets;
    static ThreadPoolExecutor sockets;

    public Listener(ConcurrentLinkedQueue<Socket> newlyConnectedSockets, ConcurrentLinkedQueue<Object> outBoundMessages, ConcurrentLinkedQueue<Object> inboundMessages) {
        this.personalQueues = new ArrayList<>();
        this.newlyConnectedSockets = newlyConnectedSockets;
        this.outBoundMessages = outBoundMessages;
        this.inBoundMessagesToNode = inboundMessages;
        this.inBoundMessages = new ConcurrentLinkedQueue<>();
    }


    @Override
    public void run() {
        sockets = (ThreadPoolExecutor) Executors.newFixedThreadPool(60);
        long startTime = System.currentTimeMillis();
        long thirtySeconds = 30000;
        boolean trueOnlyOnceSendRequest = true;
        boolean trueOnlyOnceSortLatestBlockNumber = true;
        List<Object> waitingToBeSentToQueue = new ArrayList<>();
        List<Integer> blockNumbers = new ArrayList<>();
        while (true) {
            if (!newlyConnectedSockets.isEmpty()) {
                assignPersonalQueues();
            }
            // wait for 30 seconds to get some connections
            if (startTime - System.currentTimeMillis() > thirtySeconds && trueOnlyOnceSendRequest) {
                trueOnlyOnceSendRequest = false;
                LatestBlockNumber startUpRequest = new LatestBlockNumber(false);
                addToPersonalQueues();
            }

            if (!outBoundMessages.isEmpty()) {
                addToPersonalQueues();
            }


            while (!inBoundMessages.isEmpty()) {
                Object object = inBoundMessages.poll();

                // once we waited 30 seconds and we reassigned startTime, we wait another 30 seconds but during this time
                // we filter out objects that arent latestblocknumbers
                if (startTime - System.currentTimeMillis() < thirtySeconds + thirtySeconds && trueOnlyOnceSortLatestBlockNumber) {

                    if (object instanceof LatestBlockNumber){
                        blockNumbers.add(((LatestBlockNumber) object).getLatestBlockNumber());
                    } else {
                        waitingToBeSentToQueue.add(object);
                    }
                } else if (startTime - System.currentTimeMillis() > thirtySeconds + thirtySeconds && trueOnlyOnceSortLatestBlockNumber) {
                    int blockHighestBlockNumber = Collections.max(blockNumbers);

                    // now we will need to split the ranges somewhere here so we can ask nodes for different blocks

                    trueOnlyOnceSortLatestBlockNumber = false;
                } else if (!trueOnlyOnceSendRequest) {
                    if (!waitingToBeSentToQueue.isEmpty()) inBoundMessagesToNode.addAll(waitingToBeSentToQueue);

                    inBoundMessagesToNode.add(object);
                }

            }

            if (!blockNumbers.isEmpty()) {
                // sort from largest to smallest
                blockNumbers.sort(Comparator.reverseOrder());
                // we can get the highest latest block number but maybe it will be safer if we compare the 2 highest numbers and make sure they match?
                int highest = blockNumbers.get(0);

                // then we want to split up the blocks we want so we ask multiple nodes for different ranges of blocks
            }
        }
    }


    public void assignPersonalQueues() {
        ConcurrentLinkedQueue<Object> personalQueue = new ConcurrentLinkedQueue<>();

        Socket socket = newlyConnectedSockets.poll();
        SocketSendingOut outbound = new SocketSendingOut(socket, personalQueue);
        SocketReceiving inbound = new SocketReceiving(socket, personalQueue, inBoundMessages);

        personalQueues.add(personalQueue);
        sockets.submit(inbound);
        sockets.submit(outbound);
    }

    public void addToPersonalQueues() {
        Object object = outBoundMessages.poll();
        personalQueues.forEach((ConcurrentLinkedQueue<Object> x) -> x.add(object));
    }

    public static int amountOfConnectedSockets() {
        return sockets.getActiveCount() / 2;
    }
}
