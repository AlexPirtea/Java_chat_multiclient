import java.net.InetAddress;

/**
 * Created by pirtea on 19.11.2015.
 */
public class Clients {
    public String nickname;
    public InetAddress address;
    public int port;

    Clients(String nickname, InetAddress address, int port) {
        this.nickname = nickname;
        this.address = address;
        this.port = port;
    }
    
    Clients() {
    }

}
