import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Leader Election Server - runs the BULLY and the RING election algorithms.
 * The client sends: algorithm, number of processes, failed processes, and the process
 * that notices the failure. The server runs the election, prints every message it
 * exchanges on this window and sends the same trace back to the client.
 *
 * Run:  java ElectionServer      (then start:  java ElectionClient)
 */
public class ElectionServer {

    static final int PORT = 7000;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("==================================================");
            System.out.println("            LEADER ELECTION SERVER");
            System.out.println("==================================================");
            System.out.println("Listening on port " + PORT + "  (press Ctrl+C to stop)");
            System.out.println("Algorithms available: BULLY, RING\n");

            while (true) {
                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                    stamp("Client connected from " + socket.getInetAddress().getHostAddress());
                    String request;
                    while ((request = in.readLine()) != null) {
                        if (request.equalsIgnoreCase("EXIT")) break;
                        stamp("Request received: " + request);
                        List<String> trace = handle(request);
                        for (String line : trace) {
                            System.out.println("   " + line);
                            out.println(line);
                        }
                        out.println("END");
                        System.out.println();
                    }
                    stamp("Client disconnected.\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    static void stamp(String message) {
        System.out.println("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + message);
    }

    // request format:  ALGORITHM n failedList initiator      e.g.  BULLY 5 5,4 2   (use "-" for no failure)
    static List<String> handle(String request) {
        List<String> trace = new ArrayList<>();
        try {
            String[] p = request.trim().split("\\s+");
            String algorithm = p[0].toUpperCase();
            int n = Integer.parseInt(p[1]);
            boolean[] active = new boolean[n + 1];
            Arrays.fill(active, 1, n + 1, true);
            if (!p[2].equals("-")) {
                for (String f : p[2].split(",")) {
                    int failed = Integer.parseInt(f.trim());
                    if (failed < 1 || failed > n) throw new IllegalArgumentException("failed process " + failed + " is outside 1.." + n);
                    active[failed] = false;
                }
            }
            int initiator = Integer.parseInt(p[3]);
            if (initiator < 1 || initiator > n) throw new IllegalArgumentException("initiator must be between 1 and " + n);
            if (!active[initiator]) throw new IllegalArgumentException("initiator Process " + initiator + " has failed");

            StringBuilder state = new StringBuilder();
            for (int i = 1; i <= n; i++) state.append("P").append(i).append(active[i] ? "=UP " : "=DOWN ");
            trace.add("Processes: " + state.toString().trim());
            trace.add("");

            int[] messages = {0};
            int coordinator = algorithm.equals("BULLY")
                    ? Election.bully(n, active, initiator, trace, messages)
                    : Election.ring(n, active, initiator, trace, messages);

            trace.add("");
            trace.add("RESULT: Process " + coordinator + " is the new coordinator  (" + algorithm
                    + ", " + messages[0] + " messages exchanged)");
        } catch (Exception e) {
            trace.add("ERROR: " + e.getMessage());
        }
        return trace;
    }
}
