package Node;

import Miner.Block;
import Node.MessageTypes.BlockMessage;
import Node.MessageTypes.TransactionMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

// instead of giving all sockets access to one queue we will give them each their own
public class Listener implements Runnable{
    List<ConcurrentLinkedQueue<BlockMessage>> socketPersonalBlockQueues;
    List<ConcurrentLinkedQueue<TransactionMessage>> socketPersonalTransactionQueues;
    ConcurrentLinkedQueue<SocketSendingOut> newlyConnectedSockets;
    ConcurrentLinkedQueue<Block> blocksToBeSentOut;
    ConcurrentLinkedQueue<TransactionMessage> transactionsToBeSentOut;

    public Listener(ConcurrentLinkedQueue<SocketSendingOut> newlyConnectedSockets, ConcurrentLinkedQueue<Block> blocksToBeSentOut, ConcurrentLinkedQueue<TransactionMessage> transactionsToBeSentOut) {
        this.socketPersonalBlockQueues = new ArrayList<>();
        this.socketPersonalTransactionQueues = new ArrayList<>();
        this.newlyConnectedSockets = newlyConnectedSockets;
        this.blocksToBeSentOut = blocksToBeSentOut;
        this.transactionsToBeSentOut = transactionsToBeSentOut;
    }


    @Override
    public void run() {
        while (true) {
            if (!newlyConnectedSockets.isEmpty()) {
                assignPersonalQueues();
            }
            if (!blocksToBeSentOut.isEmpty()) {
                addBlockToAllSocketQueues();
            }
            if (!transactionsToBeSentOut.isEmpty()) {
                addTransactionToAllSocketQueues();
            }
        }
    }

    public void addBlockToAllSocketQueues() {
        List<Block> blockList = new ArrayList<>();
        blockList.add(blocksToBeSentOut.poll());
        BlockMessage message = new BlockMessage(blockList,"not yet entered");
        for (ConcurrentLinkedQueue<BlockMessage> x : socketPersonalBlockQueues) {
            x.add(message);
        }
    }

    public void addTransactionToAllSocketQueues() {
        TransactionMessage transactionMessage = transactionsToBeSentOut.poll();

        for (ConcurrentLinkedQueue<TransactionMessage> x : socketPersonalTransactionQueues) {
            x.add(transactionMessage);
        }
    }

    public void assignPersonalQueues() {
        ConcurrentLinkedQueue<BlockMessage> personalBlockQueue = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<TransactionMessage> personalTransactionQueue = new ConcurrentLinkedQueue<>();

        SocketSendingOut socket = newlyConnectedSockets.poll();

        socket.setBlocksToOtherNodes(personalBlockQueue);
        socket.setTransactionsToOtherNodes(personalTransactionQueue);

        socketPersonalBlockQueues.add(personalBlockQueue);
        socketPersonalTransactionQueues.add(personalTransactionQueue);
    }
}
