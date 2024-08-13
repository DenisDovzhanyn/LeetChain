package Node;

import Miner.Block;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Connection implements Runnable {
    Socket connection;
    ConcurrentLinkedQueue<Block> blocksToOtherNodes;
    ObjectOutputStream outputStream;
    ObjectInputStream inputStream;

    // looks like we will actually need two threads per SOCKET, one for listening and one for sending data out
    // maybe it is easier to have two different types of sockets instead? one socket for listening and one for sending data out?
    // with a max of 5 connections ( 5 threads) each of those will spawn 2 threads ( 15 threads) we will have a total of
    // 20 ish threads in the program (including miner wallet etc) will this severely slow it down?
    // or maybe instead of having two different sockets, instead i remove this class directly and have two classes for input/output
    // and put a single socket between both since waiting for input and sending outputs are two different things for a socket?
    public Connection(Socket socket, ConcurrentLinkedQueue<Block> toOtherNodes) {
        this.blocksToOtherNodes = toOtherNodes;
        this.connection = socket;
    }

    @Override
    public void run() {


    }
}
