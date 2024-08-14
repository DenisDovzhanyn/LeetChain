package Node.RequestTypes;

public class PeerListRequest {
    int amountOfPeers;

    // request the top n peers from someone
    public PeerListRequest(int amountOfPeers) {
        this.amountOfPeers = amountOfPeers;
    }
}
