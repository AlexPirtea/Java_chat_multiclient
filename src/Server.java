import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;


public class Server{

    public static DatagramSocket serverSocket;
    public static List<String> clients = new ArrayList<>();
    public static byte[] buffer;
    public static List<Clients> clientsList = new ArrayList<>();
    public static void main(String[] args) throws  IOException{
        serverSocket = new DatagramSocket(null);
        serverSocket.setReuseAddress(true);
        serverSocket.setBroadcast(true);
        serverSocket.bind(new InetSocketAddress(1234));

        System.out.println("Server is on");

        buffer = new byte[256];

        while(true){
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(packet);
                String clientNickname = new String(packet.getData(), 0, packet.getLength());
                System.out.println(clientNickname + " trying to connect");
                if (validNickname(clientNickname)) {
                    clients.add(clientNickname);
                    clientsList.add(new Clients(clientNickname, packet.getAddress(), packet.getPort()));
                    DatagramSocket threadSocket = new DatagramSocket();
                    Thread t = new Thread(new Connection(threadSocket, packet, clientNickname));
                    t.start();
                } else {
                    byte[] errBuffer;
                    errBuffer = "Invalid nickname ".getBytes();
                    DatagramPacket errorPacket = new DatagramPacket(errBuffer, errBuffer.length, packet.getAddress(), packet.getPort());
                    serverSocket.send(errorPacket);
                }
            } catch (Exception e) {
                System.out.println("System error: " + e.getMessage());
            }
        }
    }

    private static boolean validNickname(String nickname){
        try {
            return !clientsList.contains(nickname);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public static Clients getClient(String nickname) {
        for (int i = 0; i < clientsList.size(); i++) {
            if (nickname.equals(clientsList.get(i).nickname)) {
                return clientsList.get(i);
            }
        }
        return new Clients();
    }


}
