import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Scanner;

/**
 * Created by pirtea on 12.11.2015.
 */
public class Client extends Thread {

    public static DatagramSocket clientSocket;
    public static DatagramPacket inputPacket;
    public static DatagramPacket outputPacket;
    public static String nickname;
    public static byte[] bufferOutput = new byte[256];
    public static byte[] bufferInput = new byte[256];
    public  Client(){
        try {
            System.out.println("Nickname:");
            Scanner scan = new Scanner(System.in);
            nickname = scan.next();;
            clientSocket = new DatagramSocket();
            inputPacket = new DatagramPacket(bufferInput,bufferInput.length);
        }
        catch(IOException ioe){
            System.out.println(ioe.getMessage()+"\nCan't start the client");
        }
    }

    public void run(){

        new Thread(new Runnable() {
            @Override
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

//            //  Receive confirmation form connection thread
//            byte[] bufferInput = new byte[256];
//            inputPacket = new DatagramPacket(bufferInput, bufferInput.length);
//            clientSocket.receive(inputPacket);
//            String raspuns = new String(inputPacket.getData(), 0, inputPacket.getLength());
//            System.out.println(raspuns);


            while(true){
                bufferOutput = new byte[256];
                System.out.println("Message: ");
                String messageToSend;// = "test=";
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                messageToSend = br.readLine();
                bufferOutput = messageToSend.getBytes();
                outputPacket = new DatagramPacket(bufferOutput, bufferOutput.length, inputPacket.getAddress(), inputPacket.getPort());
                clientSocket.send(outputPacket);


            }
        }
        catch(IOException ioe){System.out.println(ioe.getMessage());}
    }


    public static void main(String[] args) {
        Client c = new Client();
        c.start();

    }

    private static void MessageHandler(){
        try {
             MulticastSocket socket = new MulticastSocket(4446);
             InetAddress address = InetAddress.getByName("230.0.0.1");
             byte[] buffer;
             DatagramPacket inPacket;
             socket.joinGroup(address);

            while (true) {
                buffer = new byte[256];
                inPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(inPacket);
                String answ = new String(inPacket.getData(), 0, inPacket.getLength());
                System.out.println(answ);
            }
        }catch(IOException ioe){}
    }

    private static void SystemMessageHandler(){
        try{
            while(true) {
                Thread.sleep(500);
                bufferInput = new byte[256];
                System.out.println("SMH: Waiting message from system");
                clientSocket.receive(inputPacket);
                String answer = new String(inputPacket.getData(), 0, inputPacket.getLength());
                System.out.println(answer);
            }
        }catch(IOException ioe){}
        catch(InterruptedException ie){System.out.println(ie.getMessage());}
    }
}
