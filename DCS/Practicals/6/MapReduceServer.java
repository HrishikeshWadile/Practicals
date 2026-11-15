import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * Word Count with the MapReduce model - server side.
 * The client sends a set of documents. The server
 *   MAP     : one mapper thread per document turns it into (word, 1) pairs,
 *   SHUFFLE : groups and sorts all pairs by word,
 *   REDUCE  : reducer threads add the 1s of every word,
 * prints every phase on this window and sends the final counts back to the client.
 *
 * Run:  java MapReduceServer      (then start:  java MapReduceClient)
 */
public class MapReduceServer {

    static final int PORT = 8000;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("==================================================");
            System.out.println("          MAPREDUCE WORD COUNT SERVER");
            System.out.println("==================================================");
            System.out.println("Listening on port " + PORT + "  (press Ctrl+C to stop)\n");

            while (true) {
                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                    stamp("Client connected from " + socket.getInetAddress().getHostAddress());
                    while (true) {
                        List<String> documents = new ArrayList<>();
                        String line;
                        boolean closed = false;
                        while (true) {
                            line = in.readLine();
                            if (line == null) { closed = true; break; }
                            if (line.equals("END")) break;
                            documents.add(line);
                        }
                        if (closed) break;
                        stamp("Received " + documents.size() + " document(s) from the client");
                        for (String line2 : run(documents)) out.println(line2);
                        out.println("END");
                    }
                    stamp("Client disconnected.\n");
                } catch (Exception e) {
                    System.out.println("Request error: " + e);
                }
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    static void stamp(String message) {
        System.out.println("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + message);
    }

    /** Runs the whole job and returns the lines that are sent back to the client. */
    static List<String> run(List<String> documents) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(4);

        // ------------------------------------------------------------------ MAP
        System.out.println("\n===== INPUT =====");
        for (int i = 0; i < documents.size(); i++) System.out.println("Document " + (i + 1) + ": " + documents.get(i));

        System.out.println("\n===== MAP PHASE (one mapper per document, run in parallel) =====");
        List<Future<String[]>> mapFutures = new ArrayList<>();
        for (String doc : documents) {
            mapFutures.add(pool.submit(() -> {
                StringBuilder pairs = new StringBuilder();
                for (String word : doc.toLowerCase().replaceAll("[^a-z0-9 ]", " ").trim().split("\\s+")) {
                    if (!word.isEmpty()) pairs.append("(").append(word).append(",1) ");
                }
                return new String[]{Thread.currentThread().getName(), pairs.toString().trim()};
            }));
        }
        List<String[]> mapOutput = new ArrayList<>();
        for (int i = 0; i < mapFutures.size(); i++) {
            String[] result = mapFutures.get(i).get();
            mapOutput.add(result);
            System.out.println("Mapper for Document " + (i + 1) + " [" + result[0] + "]:");
            System.out.println("   " + result[1]);
        }

        // -------------------------------------------------------------- SHUFFLE
        System.out.println("\n===== SHUFFLE AND SORT (group by word) =====");
        Map<String, List<Integer>> grouped = new TreeMap<>();
        for (String[] result : mapOutput) {
            if (result[1].isEmpty()) continue;
            for (String pair : result[1].split(" ")) {
                String word = pair.substring(1, pair.indexOf(','));
                grouped.computeIfAbsent(word, k -> new ArrayList<>()).add(1);
            }
        }
        grouped.forEach((word, ones) -> System.out.printf("%-15s -> %s%n", word, ones));

        // --------------------------------------------------------------- REDUCE
        System.out.println("\n===== REDUCE PHASE (one reducer task per word) =====");
        Map<String, Future<String[]>> reduceFutures = new LinkedHashMap<>();
        for (Map.Entry<String, List<Integer>> e : grouped.entrySet()) {
            reduceFutures.put(e.getKey(), pool.submit(() -> {
                int sum = 0;
                for (int v : e.getValue()) sum += v;
                return new String[]{Thread.currentThread().getName(), String.valueOf(sum)};
            }));
        }
        Map<String, Integer> counts = new TreeMap<>();
        for (Map.Entry<String, Future<String[]>> e : reduceFutures.entrySet()) {
            String[] result = e.getValue().get();
            counts.put(e.getKey(), Integer.parseInt(result[1]));
            System.out.printf("%-15s -> %-3s   [%s]%n", e.getKey(), result[1], result[0]);
        }
        pool.shutdown();

        // --------------------------------------------------------------- RESULT
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(counts.entrySet());
        sorted.sort((a, b) -> b.getValue() != a.getValue().intValue()
                ? b.getValue() - a.getValue() : a.getKey().compareTo(b.getKey()));
        int total = counts.values().stream().mapToInt(Integer::intValue).sum();

        List<String> reply = new ArrayList<>();
        reply.add("WORD COUNT RESULT");
        reply.add(String.format("%-15s %s", "WORD", "COUNT"));
        reply.add("--------------- -----");
        for (Map.Entry<String, Integer> e : sorted) reply.add(String.format("%-15s %d", e.getKey(), e.getValue()));
        reply.add("--------------- -----");
        reply.add("Documents: " + documents.size() + "   Total words: " + total + "   Unique words: " + counts.size());

        System.out.println("\n===== FINAL RESULT (sent to client) =====");
        for (String line : reply) System.out.println(line);
        System.out.println();
        return reply;
    }
}
