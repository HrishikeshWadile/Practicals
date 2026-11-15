import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * Name Server - multi-threaded TCP name server.
 * Operations:  LOOKUP name | REGISTER name address | UNREGISTER name | LIST
 * Every request received from a client is printed on this (server) window.
 *
 * Run:  java NameServer          (then start:  java Client)
 */
public class NameServer {

    static final int PORT = 5000;
    static final Map<String, String> nameTable = new ConcurrentHashMap<>();
    static int clientCounter = 0;

    static void log(String message) {
        System.out.println("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + message);
    }

    public static void main(String[] args) {
        nameTable.put("google", "142.250.195.14");
        nameTable.put("youtube", "142.250.72.14");
        nameTable.put("facebook", "157.240.241.35");
        nameTable.put("amazon", "205.251.242.103");
        nameTable.put("microsoft", "20.112.52.29");

        ExecutorService pool = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("==================================================");
            System.out.println("                   NAME SERVER");
            System.out.println("==================================================");
            System.out.println("Listening on port " + PORT + "  (press Ctrl+C to stop)");
            System.out.println("Names registered at start: " + nameTable.size());
            printTable();
            System.out.println("Waiting for clients...\n");

            while (true) {
                Socket socket = serverSocket.accept();
                int id = ++clientCounter;
                pool.execute(() -> serve(socket, id));
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }

    static void printTable() {
        System.out.println("  NAME            ADDRESS");
        System.out.println("  --------------- ---------------");
        new TreeMap<>(nameTable).forEach((n, a) -> System.out.printf("  %-15s %s%n", n, a));
        System.out.println();
    }

    static void serve(Socket socket, int id) {
        log("Client #" + id + " connected from " + socket.getInetAddress().getHostAddress());
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String request;
            while ((request = in.readLine()) != null) {
                log("Client #" + id + " request : " + request);
                String reply = process(request.trim());
                out.println(reply);
                log("Server reply      : " + reply);
                if (request.trim().equalsIgnoreCase("EXIT")) {
                    break;
                }
            }
        } catch (IOException e) {
            log("Client #" + id + " error: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) { }
            log("Client #" + id + " disconnected.\n");
        }
    }

    static String process(String request) {
        String[] parts = request.split("\\s+");
        String command = parts[0].toUpperCase();

        switch (command) {
            case "LOOKUP":
                if (parts.length < 2) return "ERROR usage: LOOKUP name";
                String address = nameTable.get(parts[1].toLowerCase());
                return address != null ? "OK " + address : "NOT_FOUND " + parts[1];

            case "REGISTER":
                if (parts.length < 3) return "ERROR usage: REGISTER name address";
                boolean existed = nameTable.containsKey(parts[1].toLowerCase());
                nameTable.put(parts[1].toLowerCase(), parts[2]);
                return (existed ? "UPDATED " : "REGISTERED ") + parts[1].toLowerCase() + " -> " + parts[2];

            case "UNREGISTER":
                if (parts.length < 2) return "ERROR usage: UNREGISTER name";
                return nameTable.remove(parts[1].toLowerCase()) != null
                        ? "REMOVED " + parts[1].toLowerCase() : "NOT_FOUND " + parts[1];

            case "LIST":
                StringBuilder sb = new StringBuilder("OK " + nameTable.size() + " entries:");
                new TreeMap<>(nameTable).forEach((n, a) -> sb.append(" ").append(n).append("=").append(a).append(";"));
                return sb.toString();

            case "EXIT":
                return "BYE";

            default:
                return "ERROR unknown command";
        }
    }
}
