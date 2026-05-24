import java.io.*;
import java.net.*;

public class ServerTcp {

    public ServerTcp() {
        try (
                ServerSocket serverSocket = new ServerSocket(7587); // define a server socket listening on port 7587
                Socket clientSocket = serverSocket.accept(); // wait for a client to connect
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));// to read data from the client
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true))/* to send to client */ {

            String line = in.readLine();// read the first line from the client "HELLO SERVER"
            int counter = 1;

            while (line != null) {

                System.out.println("receive: " + line);
                out.println("ACK" + +counter);// send an acknowledgment back to the client for each message received
                 // read the next line from the client


                if (line.equals("DONE"))
                    break;

                line = in.readLine();
                counter++;
            }

            System.out.println("total message received: " + counter);

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        new ServerTcp(); // to start the server
    }
}