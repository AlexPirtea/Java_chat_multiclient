import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by pirtea on 12.11.2015.
 */
public class Connection implements Runnable {

    private static InetAddress group;
    private DatagramSocket clientSocket;
    private DatagramPacket inputPacket, outputPacket;
    private MulticastSocket multicastSocket = new MulticastSocket();
    private String nickname;
    private boolean connectionIsOn = true;
    private byte[] outputBuffer, inputBuffer ;

    public Connection(DatagramSocket clientSocket, DatagramPacket packet, String nickname) throws IOException {
        this.clientSocket = clientSocket;
        this.inputPacket = packet;
        this.nickname = nickname;
    }

    public void run() {
        try {

            byte[] outputBuffer;
            outputBuffer = "Connection accepted".getBytes();
            outputPacket = new DatagramPacket(outputBuffer,outputBuffer.length,inputPacket.getAddress(),inputPacket.getPort());
            clientSocket.send(outputPacket);
            while (connectionIsOn) {
                    clientSocket.receive(inputPacket);
                    String receivedMessage = new String(inputPacket.getData(), 0, inputPacket.getLength());
                //System.out.println("Message received on connection thread:" + receivedMessage);
                String[] parts = receivedMessage.split(" ");
                switch (parts[0]) {
                        case "list":
                            for(int i=0 ; i<Server.clients.size() ; i++){
                                outputBuffer = Server.clients.get(i).getBytes();
                                outputPacket = new DatagramPacket(outputBuffer,outputBuffer.length,inputPacket.getAddress(),inputPacket.getPort());
                                clientSocket.send(outputPacket);
                            }
                            break;

                        case "quit":
                            outputBuffer = "".getBytes();
                            outputPacket = new DatagramPacket(outputBuffer, outputBuffer.length, inputPacket.getAddress(), inputPacket.getPort());
                            clientSocket.send(outputPacket);

                            group = InetAddress.getByName("230.0.0.1");
                            outputPacket = new DatagramPacket(outputBuffer, outputBuffer.length, group, 4446);
                            multicastSocket.send(outputPacket);

                            //  Disconnect client and close connection
                            clientSocket.disconnect();
                            clientSocket.close();
                            multicastSocket.disconnect();
                            multicastSocket.close();
                            System.out.println(nickname + " left the server.");

                            //  Remove from client list
                            Server.clients.remove(nickname);
                            Server.clientsList.remove(Server.getClient(nickname));

                            //  Let the thread finish
                            connectionIsOn = false;
                            break;

                    case "msg":
                        String toClient = parts[1];
                        String message = nickname + " --prv_msg--> " + recomposeMessage(parts);

                        // match client name and send message
                        Clients auxClient = Server.getClient(toClient);

                        if (auxClient.address != null && auxClient.port > 0 && (auxClient.nickname.length() > 0)) {
                            outputBuffer = message.getBytes();
                            outputPacket = new DatagramPacket(outputBuffer, outputBuffer.length, auxClient.address, auxClient.port);
                            clientSocket.send(outputPacket);
                        } else {
                            outputBuffer = "No such user".getBytes();
                            outputPacket = new DatagramPacket(outputBuffer, outputBuffer.length, inputPacket.getAddress(), inputPacket.getPort());
                            clientSocket.send(outputPacket);
                        }
                        break;

                        default:
                            receivedMessage = nickname + ": "+ receivedMessage;
                            outputBuffer = receivedMessage.getBytes();
                            // outputPacket = new DatagramPacket(outputBuffer, outputBuffer.length,inputPacket.getAddress(),inputPacket.getPort());
                            group = InetAddress.getByName("230.0.0.1");
                            outputPacket = new DatagramPacket(outputBuffer, outputBuffer.length,group,4446);
                            multicastSocket.send(outputPacket);
                            System.out.println(receivedMessage);
                            break;
                    }
            }
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }

    private String recomposeMessage(String[] parts) {
        String recomposedMessage = "";
        for (int i = 2; i < parts.length; i++) {
            if (i == 2) {
                recomposedMessage = recomposedMessage + parts[i];
                continue;
            }
            recomposedMessage = " " + recomposedMessage + " " + parts[i];
        }
        System.out.println("private msg:" + recomposedMessage);
        return recomposedMessage;
    }
}