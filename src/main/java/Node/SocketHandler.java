package Node;

import Miner.Block;
import Node.MessageTypes.*;
import Wallet.Transaction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

public class SocketHandler implements Runnable{
    ServerSocket server;
    ThreadPoolExecutor sockets;
    ConcurrentLinkedQueue<TransactionMessage> transactionsToOtherNodes;
    ConcurrentLinkedQueue<BlockMessage> incomingBlock;
    ConcurrentLinkedQueue<SocketSendingOut> socketsToListener;
    static List<Peer> peers;

    public SocketHandler(ConcurrentLinkedQueue<BlockMessage> incomingBlock, ConcurrentLinkedQueue<SocketSendingOut> socketsToListener, ConcurrentLinkedQueue<TransactionMessage> transactionsToOtherNodes) {
        this.incomingBlock = incomingBlock;
        this.socketsToListener = socketsToListener;
        this.transactionsToOtherNodes = transactionsToOtherNodes;

        peers = peerFileToList();
    }

    @Override
    public void run() {
        sockets = (ThreadPoolExecutor) Executors.newFixedThreadPool(60);
        try {

            for (Peer x : peers) {
                Socket socket = new Socket(x.ip, x.port);
                if(socket.isConnected()) {
                    x.raiseScoreByOne();
                    ConcurrentLinkedQueue<BlockListRequest> blockRequests = new ConcurrentLinkedQueue<>();
                    ConcurrentLinkedQueue<PeerListRequest> peerRequests = new ConcurrentLinkedQueue<>();
                    ConcurrentLinkedQueue<LatestBlockNumber> latestBlockNumbers = new ConcurrentLinkedQueue<>();
                    SocketSendingOut outbound = new SocketSendingOut(socket, blockRequests, peerRequests, latestBlockNumbers);
                    SocketReceiving inbound = new SocketReceiving(socket, transactionsToOtherNodes, incomingBlock, blockRequests, peerRequests, latestBlockNumbers);

                    socketsToListener.add(outbound);
                    sockets.submit(outbound);
                    sockets.submit(inbound);
                }
            }
            while (sockets.getPoolSize() < 60) {
                server = new ServerSocket(6478);
                Socket socket = server.accept();
                checkSocketForPreviousConnection(socket.getRemoteSocketAddress().toString());

                ConcurrentLinkedQueue<BlockListRequest> blockRequests = new ConcurrentLinkedQueue<>();
                ConcurrentLinkedQueue<PeerListRequest> peerRequests = new ConcurrentLinkedQueue<>();
                ConcurrentLinkedQueue<LatestBlockNumber> latestBlockNumbers = new ConcurrentLinkedQueue<>();

                SocketSendingOut outbound = new SocketSendingOut(socket, blockRequests, peerRequests, latestBlockNumbers);
                socketsToListener.add(outbound);
                // nothing is being done with transactionstoothernodes wth?
                SocketReceiving inbound = new SocketReceiving(socket, transactionsToOtherNodes, incomingBlock, blockRequests, peerRequests, latestBlockNumbers);

                sockets.submit(outbound);
                sockets.submit(inbound);
                writePeersToFile();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int amountOfConnectedSockets() {
        return sockets.getPoolSize() / 2;
    }

    //
    public List<Peer> peerFileToList() {
        File peerList = new File("Peers");
        List<Peer> peers = new ArrayList<>();

        try {
            peerList.createNewFile();
            Scanner scanner = new Scanner(peerList);

            while (scanner.hasNext()) {
                String[] splitLine = scanner.nextLine().split(":");
                //                   ip            port                           score
                Peer peer = new Peer(splitLine[0], Integer.parseInt(splitLine[1]), Integer.parseInt(splitLine[2]));
                peers.add(peer);
            }
            scanner.close();
        } catch (IOException e) {
            System.out.println("Peers file not found, please create one or confirm correct location");
        }

        return peers;
    }

    // compare ip and port to previously connected sockets, if it is a match we raise score by one. if not we set score 1 and add to list
    public void checkSocketForPreviousConnection(String address) {
        String[] peerVariables = address.split(":");

        Peer peer = new Peer(peerVariables[0], Integer.parseInt(peerVariables[1]), 1);
        for (Peer x : peers){
            if (x.port == peer.port && x.ip.equals(peer.ip)) {
                x.raiseScoreByOne();
            }
        }
        peers.add(peer);
    }

    public void writePeersToFile() {
        try {
            File file = new File("Peers");
            FileWriter fileWriter = new FileWriter(file);

            for (Peer x : peers) {
                fileWriter.write(x.getIp() + ":" + x.getPort() + ":" + x.getScore());
            }
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Error writing peers to file");
        }
    }

    public static List<Peer> readTopNPeers(int amount) {
        List<Peer> sortedList = peers.stream()
                .sorted(Comparator.comparingInt(Peer::getScore))
                .collect(Collectors.toList());

        List<Peer> topNPeers = new ArrayList<>();

        int peersGathered = 0;
        for (int i = sortedList.size() - 1; i >= 0; i--) {
            topNPeers.add(sortedList.get(i));
            peersGathered++;
            if(peersGathered >= amount) break;
        }

        return topNPeers;
    }

}
