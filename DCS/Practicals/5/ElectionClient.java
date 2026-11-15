import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Leader Election Client - describe the situation, the server runs the election.
 *
 * Run:  java ElectionClient [host]
 */
public class ElectionClient {

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";

        try (Socket socket = new Socket(host, 7000);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner sc = new Scanner(System.in)) {

            System.out.println("==================================================");
            System.out.println("            LEADER ELECTION CLIENT");
            System.out.println("==================================================");
            System.out.println("Connected to Election Server at " + host + ":7000");

            while (true) {
                System.out.println("\n  1. Bully algorithm");
                System.out.println("  2. Ring algorithm");
                System.out.println("  3. Exit");
                System.out.print("Choice: ");
                if (!sc.hasNextLine()) break;
                String choice = sc.nextLine().trim();
                if (choice.equals("3")) {
                    out.println("EXIT");
                    break;
                }
                if (!choice.equals("1") && !choice.equals("2")) {
                    System.out.println("Invalid choice.");
                    continue;
                }

                System.out.print("Enter number of processes: ");
                String n = sc.nextLine().trim();
                System.out.print("Enter failed process(es), comma separated (e.g. 5 or 4,5; - for none): ");
                String failed = sc.nextLine().trim();
                if (failed.isEmpty()) failed = "-";
                System.out.print("Enter process that detects the coordinator failure: ");
                String initiator = sc.nextLine().trim();

                out.println((choice.equals("1") ? "BULLY " : "RING ") + n + " " + failed.replace(" ", "") + " " + initiator);

                System.out.println("\n----------------- trace from server -----------------");
                String line;
                while ((line = in.readLine()) != null && !line.equals("END")) {
                    System.out.println(line);
                }
                System.out.println("-----------------------------------------------------");
            }
            System.out.println("Client closed.");

        } catch (ConnectException e) {
            System.out.println("Could not connect - start ElectionServer first.");
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }
}
