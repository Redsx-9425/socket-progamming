import java.net.*;


public class UDPClient {

    public UDPClient() {
        try (DatagramSocket socket = new DatagramSocket())/*  */ {
            for (int i = 1; i <= 200; i++) {
                String message = "Packet-" + i;
                byte[] buffer = message.getBytes(); // convert the string message to bytes because UDP works with bytes
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getLocalHost(), 7588); // the data , length of data , destination IP , destination port
                socket.send(packet);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new UDPClient(); // to start the client
    }
}
