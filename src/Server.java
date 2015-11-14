import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Server{

    public static DatagramSocket serverSocket;
    public static List<String> clients = new ArrayList<String>();
    private static int nrClienti = 0;

    public static byte[] buffer;

    public static void main(String[] args) throws  IOException{
        serverSocket = new DatagramSocket(null);
        serverSocket.setReuseAddress(true);
        serverSocket.setBroadcast(true);
        serverSocket.bind(new InetSocketAddress(1234));

        System.out.println("Serverul a fost pornit");

        buffer = new byte[256];

        while(true){

                DatagramPacket packet =  new DatagramPacket(buffer, buffer.length );
                serverSocket.receive(packet);
                String clientNickname = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Clientul cu nickname-ul " + clientNickname + " incearca sa se conecteze" );
                if(validNickname(clientNickname)){
                    clients.add(clientNickname);


                    DatagramSocket threadSocket = new DatagramSocket();
                    Thread t = new Thread(new Connection(threadSocket, packet, clientNickname));
                    t.start();
                }
                else{
                    byte[] errBuffer = new byte[256];
                    errBuffer= "Invalid nickname ".getBytes();
                    DatagramPacket errorPacket = new DatagramPacket(errBuffer,errBuffer.length,packet.getAddress(),packet.getPort());
                    serverSocket.send(errorPacket);
                }


        }

    }

    private static boolean validNickname(String nickname){
        return !clients.contains(nickname);
    }
}
