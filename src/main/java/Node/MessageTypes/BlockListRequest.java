package Node.MessageTypes;



public class BlockListRequest extends BaseMessage {
    public int start;
    public int end;
    boolean isRequest;

    public BlockListRequest(int start, int end, boolean isRequest, String ip) {
        this.start = start;
        this.end = end;
        this.isRequest = true;
        this.ip = ip;
    }

    public boolean isRequest() {
        return isRequest;
    }

    public void setRequest(boolean request) {
        isRequest = request;
    }
}
