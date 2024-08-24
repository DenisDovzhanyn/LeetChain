package Node.MessageTypes;



public class BlockListRequest extends BaseMessage {
    public int start;
    public int end;

    public BlockListRequest(int start, int end, String ip) {
        this.start = start;
        this.end = end;
        this.ip = ip;
    }

}
