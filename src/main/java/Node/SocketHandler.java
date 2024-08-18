package Node;

import Miner.Block;
import Node.RequestTypes.BlockListRequest;
import Node.RequestTypes.PeerListRequest;
import Wallet.Transaction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class SocketHandler implements Runnable{
    ServerSocket server;
    ThreadPoolExecutor sockets;
    ConcurrentLinkedQueue<Block> blocksToOtherNodes;
    ConcurrentLinkedQueue<Transaction> transactionsToOtherNodes;
    ConcurrentLinkedQueue<Block> incomingBlock;
    List<Peer> peers;

    public SocketHandler(ConcurrentLinkedQueue<Block> blocksToOtherNodes, ConcurrentLinkedQueue<Block> incomingBlock) {
        this.blocksToOtherNodes = blocksToOtherNodes;
        this.incomingBlock = incomingBlock;
        peers = new ArrayList<>();
    }

    @Override
    public void run() {
        sockets = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);


        // we need to try to connect here first with a regular socket before opening up our server for connections from other people
        while (sockets.getPoolSize() <= 9) {
            try {
                server = new ServerSocket(6478);
                Socket socket = server.accept();
                checkSocketForPreviousConnection(socket.getRemoteSocketAddress().toString());

                ConcurrentLinkedQueue<BlockListRequest> blockRequests = new ConcurrentLinkedQueue<>();
                ConcurrentLinkedQueue<PeerListRequest> peerRequests = new ConcurrentLinkedQueue<>();

                SocketSendingOut outbound = new SocketSendingOut(socket, blocksToOtherNodes, transactionsToOtherNodes, blockRequests, peerRequests);
                SocketReceiving inbound = new SocketReceiving(socket, transactionsToOtherNodes, incomingBlock, blockRequests, peerRequests);

                sockets.submit(outbound);
                sockets.submit(inbound);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public int amountOfConnectedSockets() {
        return sockets.getPoolSize() / 2;
    }

    //
    public List<Peer> peerFileToList() {
        File peerlist = new File("Peers");
        List<Peer> peers = new ArrayList<>();

        try {
            peerlist.createNewFile();
            Scanner scanner = new Scanner(peerlist);

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
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);

            for (Peer x : peers) {
                fileWriter.write(x.getIp() + ":" + x.getPort() + ":" + x.getScore());
            }
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Error writing peers to file");
        }
    }


}
