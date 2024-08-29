package Node;

import Node.MessageTypes.BlockMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

// instead of giving all sockets access to one queue we will give them each their own
public class Listener implements Runnable{
    List<ConcurrentLinkedQueue<BlockMessage>> socketPersonalQueue;
    ConcurrentLinkedQueue<SocketSendingOut> newlyConnectedSockets;
    ConcurrentLinkedQueue<BlockMessage> blocksToBeSentOut;

    public Listener(ConcurrentLinkedQueue<SocketSendingOut> newlyConnectedSockets, ConcurrentLinkedQueue<BlockMessage> blocksToBeSentOut) {
        this.socketPersonalQueue = new ArrayList<>();
        this.newlyConnectedSockets = newlyConnectedSockets;
        this.blocksToBeSentOut = blocksToBeSentOut;
    }


    @Override
    public void run() {
        while (true) {
            if (!blocksToBeSentOut.isEmpty()) {
                addBlockToAllSocketQueues();
            }
        }
    }

    public void addBlockToAllSocketQueues() {
        BlockMessage blockMessage = blocksToBeSentOut.poll();

        for (ConcurrentLinkedQueue<BlockMessage> x : socketPersonalQueue) {
            x.add(blockMessage);
        }
    }
}
