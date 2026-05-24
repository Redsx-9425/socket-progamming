import java.net.*;
import java.util.ArrayList;

public class UDPServer {

    public UDPServer() {
        int count = 0;
        ArrayList<String> msgList = new ArrayList<>(); // to store received messages
        try (DatagramSocket socket = new DatagramSocket(7588)) {

            byte[] buffer = new byte[128]; // to store incoming data

            socket.setSoTimeout(3000); //to not enter infinite loop if less than 200 packets received
            // socket.receive() it blocks until a packet is received if the packet lost will wait forever
            //if no packet received in 5 seconds it will get out from the loop
            while (count < 210) {
                try {  // to handle the SocketTimeoutException
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length); // create a packet to receive data
                    socket.receive(packet);// receive a packet and store it in the buffer
                    String message = new String(packet.getData(), 0, packet.getLength()); //
                    msgList.add(message);
                    count++;
                } catch (SocketTimeoutException e) {
                    break;
                }

            }
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        System.out.println("  Total packets received: " + msgList.size()); //  total number of packets received = size of the  list
        System.out.println("  Number of missing packets: " + (200 - msgList.size())); // 200 packets sent , so missing = 200 - received
        // Check for out of order packets by comparing the packets numbers
        int out = 0;
        for (int i = 0; i < msgList.size() - 1; i++) {
            int currentNum = Integer.parseInt(msgList.get(i).split("-")[1]); // get the packet number
            int nextNum = Integer.parseInt(msgList.get(i + 1).split("-")[1]); // get the next packet number
            if (currentNum > nextNum) // if curr > next , then out of order
                out++;
        }

        System.out.println("  Number of out of order packets: " + out);

    }

    public static void main(String[] args) {
        new UDPServer(); // to start the server
    }
}
