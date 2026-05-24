import java.io.*;
import java.net.*;

public class ClientTcp {

    public ClientTcp() {
        try (
                Socket client = new Socket("localhost", 7587); // connect to the server on localhost and port 7587
                PrintWriter out = new PrintWriter(client.getOutputStream(), true); // to send data to the server
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream())/* to receive data from the server */)) {

            out.println("HELLO SERVER");// send first message to the server

            for (int i = 1; i <= 10; i++) // Send 10 messages to the server
                out.println("Message " + i);

            out.println("DONE"); // send final message to the server

            String response = in.readLine(); // read the next line from the server

            int counter = 0; // to count the number of ACK from the server
            while (response != null) {
                System.out.println("receive: " + response);
                if (response.equals("ACk12"))
                    break;
                response = in.readLine();
                counter++;
            }
            System.out.println("Total ACK received: " + counter);

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new ClientTcp(); // to start the client
    }
}