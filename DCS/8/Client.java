import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * Name Server client - menu driven. Choose an operation, the request goes to the
 * name server and the answer is shown here. The server window shows every request.
 *
 * Run:  java Client [host] [port]
 */
public class Client {

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5000;

        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner sc = new Scanner(System.in)) {

            System.out.println("==================================================");
            System.out.println("                   NAME CLIENT");
            System.out.println("==================================================");
            System.out.println("Connected to Name Server at " + host + ":" + port);

            boolean running = true;
            while (running) {
                System.out.println("\n  1. Lookup  (name -> address)");
                System.out.println("  2. Register a new name");
                System.out.println("  3. Unregister a name");
                System.out.println("  4. List all names");
                System.out.println("  5. Exit");
                System.out.print("Choice: ");
                if (!sc.hasNextLine()) break;
                String choice = sc.nextLine().trim();

                String request;
                switch (choice) {
                    case "1":
                        System.out.print("Enter domain name: ");
                        request = "LOOKUP " + sc.nextLine().trim();
                        break;
                    case "2":
                        System.out.print("Enter name: ");
                        String name = sc.nextLine().trim();
                        System.out.print("Enter address: ");
                        request = "REGISTER " + name + " " + sc.nextLine().trim();
                        break;
                    case "3":
                        System.out.print("Enter name to remove: ");
                        request = "UNREGISTER " + sc.nextLine().trim();
                        break;
                    case "4":
                        request = "LIST";
                        break;
                    case "5":
                        request = "EXIT";
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                        continue;
                }

                out.println(request);
                String reply = in.readLine();
                System.out.println("Name Server replied: " + reply);
            }
            System.out.println("Disconnected from Name Server.");

        } catch (ConnectException e) {
            System.out.println("Could not connect - start NameServer first.");
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }
}
