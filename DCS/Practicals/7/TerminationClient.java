import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Termination Detection client - type events of the distributed computation.
 *
 *   SEND a b    process a (active) sends a WORK message to process b
 *   RECEIVE b   process b receives the oldest WORK message waiting for it
 *   FINISH p    process p finishes its work and becomes passive
 *   STATUS      show all processes, messages in transit and the engagement tree
 *   INIT n      start a new computation with n processes (2-9)
 *   DEMO        run a ready-made example
 *   EXIT        quit
 *
 * Run:  java TerminationClient [host]
 */
public class TerminationClient {

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";

        try (Socket socket = new Socket(host, 9000);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner sc = new Scanner(System.in)) {

            System.out.println("==================================================");
            System.out.println("     TERMINATION DETECTION CLIENT (Dijkstra-Scholten)");
            System.out.println("==================================================");
            System.out.println("Connected to server at " + host + ":9000.  Starts with 3 processes, P1 = root (ACTIVE).");
            System.out.println("Commands: SEND a b | RECEIVE b | FINISH p | STATUS | INIT n | DEMO | EXIT");

            while (true) {
                System.out.print("\n> ");
                if (!sc.hasNextLine()) break;
                String command = sc.nextLine().trim();
                if (command.isEmpty()) continue;
                out.println(command);
                if (command.equalsIgnoreCase("EXIT")) break;

                String line;
                while ((line = in.readLine()) != null && !line.equals("END")) {
                    System.out.println(line);
                }
            }
            System.out.println("Client closed.");

        } catch (ConnectException e) {
            System.out.println("Could not connect - start TerminationServer first.");
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }
}
