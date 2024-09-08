package Node;

import Miner.Block;
import Node.MessageTypes.BlockListRequest;
import Node.MessageTypes.BlockMessage;
import Node.MessageTypes.LatestBlockNumber;
import Node.MessageTypes.TransactionMessage;

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
        while (true) {
            if (!newlyConnectedSockets.isEmpty()) {
                assignPersonalQueues();
            }
            if (!outBoundMessages.isEmpty()) {
                addToPersonalQueues();
            }

            List<Integer> blockNumbers = new ArrayList<>();
            while (!inBoundMessages.isEmpty()) {
                Object object = inBoundMessages.poll();

                if (object instanceof LatestBlockNumber) {
                    LatestBlockNumber latest = (LatestBlockNumber) object;
                    blockNumbers.add(latest.getLatestBlockNumber());
                } else {
                    inBoundMessagesToNode.add(object);
                }

                // this may not work but my reasoning is that we might not get all the requests back instantly
                // so we wait 100ms every iteration just incase someone sends their request back a little late
                try {
                    wait(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
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
