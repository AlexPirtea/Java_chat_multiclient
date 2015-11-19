import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Scanner;

/**b
 * Created by pirtea on 12.11.2015.
 */
public class Client extends Thread {

    public static DatagramSocket clientSocket;
    public static DatagramPacket inputPacket;
    public static DatagramPacket outputPacket;
    public static String nickname;
    public static InetAddress serverAddress;
    public static int serverPort;
    public static boolean clientIsOn = true;
    public static byte[] bufferOutput = new byte[256];
    public static byte[] bufferInput = new byte[256];
    public  Client(){
        try {
            System.out.println("Nickname:");
            Scanner scan = new Scanner(System.in);
            nickname = scan.next();
            clientSocket = new DatagramSocket();
            inputPacket = new DatagramPacket(bufferInput,bufferInput.length);
        }
        catch(IOException ioe){
            System.out.println(ioe.getMessage()+"\nCan't start the client");
        }
    }

    public static void main(String[] args) {
        Client c = new Client();
        c.start();
    }

    private static void MessageHandler() {
        try {
            MulticastSocket socket = new MulticastSocket(4446);
            InetAddress address = InetAddress.getByName("230.0.0.1");
            byte[] buffer;
            DatagramPacket inPacket;
            socket.joinGroup(address);

            while (clientIsOn) {
                buffer = new byte[256];
                inPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(inPacket);
                String answer = new String(inPacket.getData(), 0, inPacket.getLength());
                if (!answer.equals("")) {
                    System.out.println(answer);
                }
            }
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }

    private static void SystemMessageHandler() {
        try {
            clientSocket.receive(inputPacket);
            //  Store connection address and port for later use
            serverAddress = inputPacket.getAddress();
            serverPort = inputPacket.getPort();
            while (clientIsOn) {
                clientSocket.receive(inputPacket);
                String answer = new String(inputPacket.getData(), 0, inputPacket.getLength());
                if (!answer.equals("")) {
                    System.out.println(answer);
                }
            }
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }

    public void run() {

        new Thread(new Runnable() {
            public void run() {
                SystemMessageHandler();
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                MessageHandler();
            }
        }).start();

        try{
            //  Sending nickname
            bufferOutput = new byte[256];
            bufferOutput = nickname.getBytes();
            outputPacket = new DatagramPacket(bufferOutput, bufferOutput.length, Inet4Address.getLocalHost(), 1234);
            clientSocket.send(outputPacket);


            while (clientIsOn) {
                bufferOutput = new byte[256];
                System.out.println("Message: ");
                String messageToSend;
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                messageToSend = br.readLine();
                bufferOutput = messageToSend.getBytes();
                outputPacket = new DatagramPacket(bufferOutput, bufferOutput.length, serverAddress, serverPort);
                clientSocket.send(outputPacket);
                if (messageToSend.equals("quit")) {
                    clientIsOn = false;
                    System.out.println("Oh my God, you killed the connection! You bastard!");
                }
            }
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }
}
