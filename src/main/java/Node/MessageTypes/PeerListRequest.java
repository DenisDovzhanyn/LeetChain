package Node.MessageTypes;

public class PeerListRequest extends BaseMessage{
    public int amountOfPeers;

    // request the top n peers from someone
    public PeerListRequest(int amountOfPeers, String ip) {
        this.amountOfPeers = amountOfPeers;
        this.ip = ip;
    }

}
