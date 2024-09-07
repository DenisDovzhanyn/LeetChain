package Node;

import Miner.Block;
import Node.MessageTypes.BlockMessage;
import Node.MessageTypes.TransactionMessage;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

// instead of giving all sockets access to one queue we will give them each their own
public class Listener implements Runnable{
    List<ConcurrentLinkedQueue<Object>> personalQueues;
    ConcurrentLinkedQueue<Object> outBoundMessages;
    ConcurrentLinkedQueue<Object> inBoundMessages;
    ConcurrentLinkedQueue<Socket> newlyConnectedSockets;
    static ThreadPoolExecutor sockets;

    public Listener(ConcurrentLinkedQueue<Socket> newlyConnectedSockets, ConcurrentLinkedQueue<Object> outBoundMessages, ConcurrentLinkedQueue<Object> inboundMessages) {
        this.personalQueues = new ArrayList<>();
        this.newlyConnectedSockets = newlyConnectedSockets;
        this.outBoundMessages = outBoundMessages;
        this.inBoundMessages = inboundMessages;
    }


    @Override
    public void run() {
        sockets = (ThreadPoolExecutor) Executors.newFixedThreadPool(60);

        while (true) {
            if (!newlyConnectedSockets.isEmpty()) {
                assignPersonalQueues();
            }

            if(!outBoundMessages.isEmpty()) {
                addToPersonalQueues();
            }
        }
    }


    public void assignPersonalQueues() {
        ConcurrentLinkedQueue<Object> personalQueue = new ConcurrentLinkedQueue<>();

        Socket socket = newlyConnectedSockets.poll();
        SocketSendingOut outbound = new SocketSendingOut(socket, personalQueue);
        SocketReceiving inbound = new SocketReceiving(socket, personalQueue, inBoundMessages);

        personalQueues.add(personalQueue);
    }

    public void addToPersonalQueues() {
        Object object = outBoundMessages.poll();
        personalQueues.forEach((ConcurrentLinkedQueue<Object> x) -> x.add(object));
    }

    public static int amountOfConnectedSockets() {
        return sockets.getActiveCount() / 2;
    }
}
