package Node.RequestTypes;

public class PeerListRequest {
    public int amountOfPeers;

    // request the top n peers from someone
    public PeerListRequest(int amountOfPeers) {
        this.amountOfPeers = amountOfPeers;
    }

}
