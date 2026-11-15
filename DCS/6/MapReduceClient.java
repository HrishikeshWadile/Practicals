import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Word Count client - type the documents, the server runs MapReduce and returns the counts.
 * The server window shows the Map, Shuffle and Reduce phases.
 *
 * Run:  java MapReduceClient [host]
 */
public class MapReduceClient {

    static final String[] SAMPLE = {
            "Hello world hello",
            "Hadoop is powerful",
            "Hello Hadoop world",
            "MapReduce is powerful",
            "Hadoop makes big data processing easy"
    };

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";

        try (Socket socket = new Socket(host, 8000);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner sc = new Scanner(System.in)) {

            System.out.println("==================================================");
            System.out.println("          MAPREDUCE WORD COUNT CLIENT");
            System.out.println("==================================================");
            System.out.println("Connected to MapReduce Server at " + host + ":8000");

            while (true) {
                System.out.println("\n  1. Type my own documents");
                System.out.println("  2. Use the sample documents");
                System.out.println("  3. Exit");
                System.out.print("Choice: ");
                if (!sc.hasNextLine()) break;
                String choice = sc.nextLine().trim();
                if (choice.equals("3")) break;

                List<String> documents = new ArrayList<>();
                if (choice.equals("1")) {
                    System.out.println("Enter one document per line. Press Enter on an empty line to finish:");
                    while (sc.hasNextLine()) {
                        String line = sc.nextLine();
                        if (line.trim().isEmpty()) break;
                        documents.add(line);
                    }
                    if (documents.isEmpty()) {
                        System.out.println("No documents entered.");
                        continue;
                    }
                } else if (choice.equals("2")) {
                    documents.addAll(Arrays.asList(SAMPLE));
                    System.out.println("Sample documents:");
                    for (int i = 0; i < documents.size(); i++) System.out.println("  " + (i + 1) + ". " + documents.get(i));
                } else {
                    System.out.println("Invalid choice.");
                    continue;
                }

                for (String d : documents) out.println(d);
                out.println("END");
                System.out.println("\nSent " + documents.size() + " document(s). Waiting for the result...\n");

                String line;
                while ((line = in.readLine()) != null && !line.equals("END")) {
                    System.out.println(line);
                }
            }
            System.out.println("Client closed.");

        } catch (ConnectException e) {
            System.out.println("Could not connect - start MapReduceServer first.");
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }
}
