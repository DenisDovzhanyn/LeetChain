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
    ConcurrentLinkedQueue<Socket> socketsToListener;
    static List<Peer> peers;

    public SocketHandler(ConcurrentLinkedQueue<Socket> socketsToListener) {
        this.socketsToListener = socketsToListener;
        peers = peerFileToList();
    }

    @Override
    public void run() {
        try {
            for (Peer x : peers) {
                Socket socket = new Socket(x.ip, x.port);
                if(socket.isConnected()) {
                    x.raiseScoreByOne();
                    socketsToListener.add(socket);
                }
            }

            while (Listener.sockets.getPoolSize() < 60) {
                // i dont think i should be making a new server socket every iteration whoops
                server = new ServerSocket(6478);
                Socket socket = server.accept();
                checkSocketForPreviousConnection(socket.getRemoteSocketAddress().toString());

                socketsToListener.add(socket);
                writePeersToFile();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    public static int findPeerIndexByIp(String ip) {
        int index = 0;
        for (int i = 0; i < peers.size(); i++) {
            if (peers.get(i).ip.equals(ip)) {
                index = i;
                break;
            }
        }
        return index;
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
