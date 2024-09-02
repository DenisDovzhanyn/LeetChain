package Node;

public class Peer{
    String ip;
    int port;
    int score;

    public Peer (String ip, int port, int score) {
        this.ip = ip;
        this.port = port;
        this.score = score;
    }

    public void raiseScoreByOne() {
        score++;
    }

    public void lowerScoreByOne() {
        score--;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

}
