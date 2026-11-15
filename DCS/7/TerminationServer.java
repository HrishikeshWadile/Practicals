import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Termination Detection (Dijkstra-Scholten algorithm) - server side.
 * The server owns the processes P1..Pn. P1 is the root (initiator) and starts ACTIVE.
 * The client sends events (SEND, RECEIVE, FINISH ...). The server applies the algorithm,
 * prints every WORK / ACK message here and tells the root when termination is detected.
 *
 * Run:  java TerminationServer      (then start:  java TerminationClient)
 */
public class TerminationServer {

    static final int PORT = 9000;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("==================================================");
            System.out.println("     TERMINATION DETECTION SERVER (Dijkstra-Scholten)");
            System.out.println("==================================================");
            System.out.println("Listening on port " + PORT + "  (press Ctrl+C to stop)\n");

            while (true) {
                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                    stamp("Client connected from " + socket.getInetAddress().getHostAddress());
                    Engine engine = new Engine(3);
                    String command;
                    while ((command = in.readLine()) != null) {
                        if (command.equalsIgnoreCase("EXIT")) break;
                        System.out.println("[" + time() + "] Client command: " + command);
                        for (String line : engine.execute(command)) {
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

    static String time() { return new SimpleDateFormat("HH:mm:ss").format(new Date()); }

    static void stamp(String message) { System.out.println("[" + time() + "] " + message); }
}
