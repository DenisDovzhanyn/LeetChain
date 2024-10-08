package Node.MessageTypes;

import Node.Peer;

import java.util.List;

public class PeerMessage extends BaseMessage {
    List<Peer> peers;

    public PeerMessage(List<Peer> peers, String ip) {
        this.peers = peers;
        this.ip = ip;
    }

    public List<Peer> getPeers() {
        return peers;
    }

    public void setPeers(List<Peer> peers) {
        this.peers = peers;
    }
}
