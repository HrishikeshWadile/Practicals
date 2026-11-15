import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * Echo Client - type a message, the server echoes it back.
 * Watch the server window to see what the server receives.
 *
 * Run:  java EchoClient [host] [port]
 */
public class EchoClient {

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5000;

        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("==================================================");
            System.out.println("                  ECHO CLIENT");
            System.out.println("==================================================");
            System.out.println("Connected to Echo Server at " + host + ":" + port);
            System.out.println("Server: " + in.readLine());
            System.out.println();

            while (true) {
                System.out.print("Client: ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String message = scanner.nextLine();
                out.println(message);

                String reply = in.readLine();
                if (reply == null) {
                    System.out.println("Server closed the connection.");
                    break;
                }
                System.out.println("Server: " + reply);
                System.out.println();

                if (message.equalsIgnoreCase("exit")) {
                    break;
                }
            }
            System.out.println("Disconnected from Echo Server.");

        } catch (ConnectException e) {
            System.out.println("Could not connect - start EchoServer first.");
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }
}
