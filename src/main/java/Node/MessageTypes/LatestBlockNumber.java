package Node.MessageTypes;

public class LatestBlockNumber extends BaseMessage {
    int latestBlockNumber;
    Boolean isRequest;

    public LatestBlockNumber() {
        isRequest = true;
    }

    public int getLatestBlockNumber() {
        return latestBlockNumber;
    }

    public void setLatestBlockNumber(int latestBlockNumber) {
        this.latestBlockNumber = latestBlockNumber;
    }

    public Boolean getIsRequest() {
        return isRequest;
    }

    public void setIsRequest(Boolean request) {
        isRequest = request;
    }
}
