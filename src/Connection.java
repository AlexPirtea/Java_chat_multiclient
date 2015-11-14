import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Objects;

/**
 * Created by pirtea on 12.11.2015.
 */
public class Connection implements Runnable {

    private DatagramSocket clientSocket;
    private DatagramPacket inputPacket, outputPacket;
    private MulticastSocket multicastSocket = new MulticastSocket();
    private String nickname;
    private byte[] outputBuffer, inputBuffer ;
    private int bufferLength;

    public Connection(DatagramSocket clientSocket, DatagramPacket packet, String nickname) throws IOException {
        this.clientSocket = clientSocket;
        this.inputPacket = packet;
        this.nickname = nickname;
    }

    public void run() {
        try {
            byte[] outputBuffer = new byte[256];
            outputBuffer = "Conexiune acceptata".getBytes();
            outputPacket = new DatagramPacket(outputBuffer,outputBuffer.length,inputPacket.getAddress(),inputPacket.getPort());
            clientSocket.send(outputPacket);
        }catch(IOException ioe ){}

            while (true) {
                try {
                    inputBuffer = new byte[256];
                    inputPacket = new DatagramPacket(inputBuffer, inputBuffer.length);
                    System.out.println("astept mesaj pe conexiune");
                    clientSocket.receive(inputPacket);
                    String mesajPrimit = new String(inputPacket.getData(), 0, inputPacket.getLength());
                    System.out.println("am primit mesajul pe conexiune:" + mesajPrimit);
                    switch(mesajPrimit) {

                        case "list":
                            for(int i=0 ; i<Server.clients.size() ; i++){
                                outputBuffer = Server.clients.get(i).getBytes();
                                outputPacket = new DatagramPacket(outputBuffer,outputBuffer.length,inputPacket.getAddress(),inputPacket.getPort());
                                clientSocket.send(outputPacket);
                            }
                            break;

                        case "quit":
                            clientSocket.close();
                            System.out.println(nickname + " a parasit serverul.");
                            break;

                        default:
                            outputBuffer = new byte[256];
                            mesajPrimit = nickname + ": "+ mesajPrimit;
                            outputBuffer = mesajPrimit.getBytes();
                            // outputPacket = new DatagramPacket(outputBuffer, outputBuffer.length,inputPacket.getAddress(),inputPacket.getPort());

                            InetAddress group = InetAddress.getByName("230.0.0.1");
                            outputPacket = new DatagramPacket(outputBuffer, outputBuffer.length,group,4446);
                            multicastSocket.send(outputPacket);
                            System.out.println(nickname + ": " + mesajPrimit);
                            break;
                    }


                } catch (IOException ioe) {
                    System.out.println(ioe.getMessage());
                }

            }

    }
}