import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * Echo Server - concurrent TCP server.
 * Every client is served by its own thread, so several clients can chat at the same time.
 * Everything a client types is shown here on the server side.
 *
 * Run:  java EchoServer          (then start one or more:  java EchoClient)
 */
public class EchoServer {

    static final int PORT = 5000;
    static final AtomicInteger clientCounter = new AtomicInteger(0);
    static final AtomicInteger activeClients = new AtomicInteger(0);

    static String time() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    static void log(String message) {
        System.out.println("[" + time() + "] " + message);
    }

    public static void main(String[] args) {
        ExecutorService pool = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("==================================================");
            System.out.println("                  ECHO SERVER");
            System.out.println("==================================================");
            System.out.println("Listening on port " + PORT + "  (press Ctrl+C to stop)");
            System.out.println("Waiting for clients...\n");

            while (true) {
                Socket socket = serverSocket.accept();
                int id = clientCounter.incrementAndGet();
                pool.execute(() -> handleClient(socket, id));
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }

    static void handleClient(Socket socket, int id) {
        String who = "Client #" + id + " (" + socket.getInetAddress().getHostAddress()
                + ":" + socket.getPort() + ")";
        int messages = 0;
        log(who + " connected.   Active clients: " + activeClients.incrementAndGet());

        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println("Welcome! You are client #" + id + ". Type 'exit' to quit.");

            String line;
            while ((line = in.readLine()) != null) {
                messages++;
                log("Client #" + id + " [" + Thread.currentThread().getName() + "] sent : " + line);
                out.println("Echo: " + line);
                log("Server -> Client #" + id + "              echo: " + line);
                if (line.equalsIgnoreCase("exit")) {
                    break;
                }
            }
        } catch (IOException e) {
            log("Client #" + id + " connection error: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) { }
            log(who + " disconnected after " + messages + " message(s).   Active clients: "
                    + activeClients.decrementAndGet());
        }
    }
}
