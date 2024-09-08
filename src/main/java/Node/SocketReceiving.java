package Node;

import Node.MessageTypes.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Map.entry;

public class SocketReceiving implements Runnable {
    Socket receiving;
    ConcurrentLinkedQueue<Object> outBoundMessages;
    ConcurrentLinkedQueue<Object> incomingMessages;
    String ip;

    public SocketReceiving(Socket receiving, ConcurrentLinkedQueue<Object> outBoundMessages, ConcurrentLinkedQueue<Object> incomingMessages) {
        this.receiving = receiving;
        this.outBoundMessages = outBoundMessages;
        this.incomingMessages = incomingMessages;
    }



    @Override
    public void run() {
        Consumer<Object> routeGenericMessage = (Object x) -> outBoundMessages.add(x);
        Consumer<Object> routeGenericMessageOutboundInbound = (Object x) -> {
            outBoundMessages.add(x);
            incomingMessages.add(x);
        };
        Consumer<Object> routeLatestBlockNumberMessage = (Object x) -> {
            LatestBlockNumber latestNumber = (LatestBlockNumber) x;
            if (latestNumber.getIsRequest()) {
                outBoundMessages.add(latestNumber);
            } else {
                int start = Ledger.getInstance().getLatestBlock().blockNumber + 1;
                BlockListRequest request = new BlockListRequest(start, latestNumber.getLatestBlockNumber(), false, ip);
                // if its not a request, then we want to ask MULTIPLE connected people for a certain range of blocks, but How do I send it back up
                // and split it amongst multiple sockets?
            }
        };

        try {
            ip = InetAddress.getLocalHost().getHostAddress();

            ObjectInputStream inputStream = new ObjectInputStream(receiving.getInputStream());
            Map<Class, Consumer<Object>> messageRouter = Map.ofEntries(
                    entry(BlockListRequest.class, routeGenericMessage),
                    entry(PeerListRequest.class, routeGenericMessage),
                    entry(BlockMessage.class, routeGenericMessageOutboundInbound),
                    entry(TransactionMessage.class, routeGenericMessageOutboundInbound),
                    entry(PeerMessage.class, (Object x) -> SocketHandler.peers.addAll(((PeerMessage) x).getPeers())),
                    entry(LatestBlockNumber.class, routeLatestBlockNumberMessage)
            );
            while (true) {
                Object message = inputStream.readObject();
                if (message == null) continue;

                Consumer<Object> route = messageRouter.get(message.getClass());
                route.accept(message);

                // from testing in a different repository, when an object is received and then the connection closes
                // this will throw an error " connnection reset", upon getting this error we should close this thread ( including the sending out thread aswell )
                // how will we handle this exception and close this thread ??????
                // even so if we throw an exception to close this thread, how will we close the sending out thread?
                // Should I just let it die on its own when it tries to send out info?
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("object not formatted correctly or connection terminated");
        }
    }
}
