import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class HttpServerForFileSharing {

    private static int totalRequests = 0;
    private static int successfulDownloads = 0;
    private static int failedRequests = 0;

    public static void main(String[] args) {
        int port = 6587;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("listening on port: " + port);
            getStatics();

            while (true) { // to handle multiple user
                try (Socket clientSocket = serverSocket.accept()) {
                    totalRequests++;
                    handleClientRequest(clientSocket);
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void handleClientRequest(Socket clientSocket) throws IOException {
        String clientIP = clientSocket.getInetAddress().getHostAddress(); // get client ip
        int clientPort = clientSocket.getPort(); // get client port

        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // to read from client
        BufferedOutputStream out = new BufferedOutputStream(clientSocket.getOutputStream()); // to write to client

        String requestLine = in.readLine();

        // If the header stream starts empty, evaluate as a bad frame format
        if (requestLine == null || requestLine.trim().isEmpty()) {
            sendErrorResponse(out, 400, "Bad Request", "Malformed HTTP request header frame received.");
            logEvent(clientIP, clientPort, "Unknown/Null Request", 400);
            return;
        }

        //split the request  by the space
        String[] parts =  requestLine.split(" ");
        if (parts.length < 2) {
            sendErrorResponse(out, 400, "Bad Request", "Incomplete request line.");
            logEvent(clientIP, clientPort, requestLine, 400);
            return;
        }

        String httpMethod = parts[0];
        String requestedResource = parts[1];


        if (requestedResource.equals("/")) {
            requestedResource = "/home_en.html";
        }


        String cleanPath = requestedResource;
        if(requestedResource.startsWith("/")){
            cleanPath = requestedResource.substring(1); // to remove (/) in the start of requested file if found
        }


        File targetFile = new File(cleanPath);

        if (!httpMethod.equals("GET")) {

            sendErrorResponse(out, 400, "Bad Request", "method not supported ");
            logEvent(clientIP, clientPort, requestedResource, 400);
        } else if (!targetFile.exists() || targetFile.isDirectory()) { //file not found

            sendErrorResponse(out, 404, "Not Found", "Requested file not found");
            logEvent(clientIP, clientPort, requestedResource, 404);
        } else { // accepted
            sendSuccessfulFileResponse(out, targetFile);
            logEvent(clientIP, clientPort, requestedResource, 200);
        }
    }

    private static void sendSuccessfulFileResponse(BufferedOutputStream out, File file) throws IOException {
        String contentType = getFileExtension(file.getName());
        byte[] fileBytes = new byte[(int) file.length()];

        // Read the target resource into cache buffer arrays
        try (FileInputStream fis = new FileInputStream(file)) { // save file bytes into array to send it
            int bytesReaded = 0;
            while (bytesReaded < fileBytes.length) {
                int read = fis.read(fileBytes, bytesReaded, fileBytes.length - bytesReaded);
                if (read == -1) break;
                bytesReaded += read;
            }
        }

        //print http response
        PrintWriter headerWriter = new PrintWriter(out, false);
        headerWriter.print("HTTP/1.1 200 OK \r\n");
        headerWriter.print("Server: ENCS3320 Custom File Sharing Server\r\n");
        headerWriter.print("Content-Type: " + contentType + "\r\n");
        headerWriter.print("Content-Length: " + fileBytes.length + "\r\n");
        headerWriter.print("Connection: close\r\n");
        headerWriter.print("\r\n"); //end of headers
        headerWriter.flush();

        // send file bytes
        out.write(fileBytes, 0, fileBytes.length);
        out.flush();

        successfulDownloads++;
    }

    private static void sendErrorResponse(BufferedOutputStream out, int statusCode, String statusText, String message) throws IOException {
        failedRequests++;

        String msg = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>" + statusCode + " " + statusText + "</title></head>" + "<body style='font-family:Sans-Serif; text-align:center; padding-top:50px;'>" + "<h1 style='color:#DC3545;'>Error Code: " + statusCode + "</h1>" + "<h2>" + message + "</h2>" + "<hr><p>ENCS3320 Custom File Sharing Protocol Server Engine Interface Engine</p>" + "</body></html>";

        byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);//because html use utf_8 encoding

        PrintWriter headerWriter = new PrintWriter(out, false);
        headerWriter.print("HTTP/1.1 " + statusCode + " " + statusText + "\r\n");
        headerWriter.print("Content-Type: text/html; charset=UTF-8\r\n");
        headerWriter.print("Content-Length: " + msgBytes.length + "\r\n");
        headerWriter.print("Connection: close\r\n");
        headerWriter.print("\r\n");
        headerWriter.flush();

        out.write(msgBytes, 0, msgBytes.length);
        out.flush();
    }

    // to ensure of file extension requested the allowed {html,txt,jpeg,png}
    // text encoding UTF-8
    private static String getFileExtension(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".txt")) return "text/plain; charset=UTF-8";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        return "application/octet-stream";
    }

    // who the client , what requested and the response
    private static void logEvent(String ip, int port, String resource, int status) {
        System.out.println("connection logs:");
        System.out.println("    Client: " + ip + " " + port);
        System.out.println("    Resource Requested: " + resource);
        System.out.println("    Response Status Code: " + status + " " + getStatusLabel(status));
        System.out.println();
        getStatics();
    }

    private static String getStatusLabel(int status) {
        if (status == 200) return "OK";
        if (status == 400) return "BAD REQUEST";
        if (status == 404) return "NOT FOUND";
        return "[UNKNOWN]";
    }

    private static void getStatics() {
        System.out.println("Server logs:");
        System.out.println("    Total successful file downloads : " + successfulDownloads);
        System.out.println("    Total failed requests : " + failedRequests);
        System.out.println("    Total requests : " + totalRequests);
        System.out.println();
    }
}
