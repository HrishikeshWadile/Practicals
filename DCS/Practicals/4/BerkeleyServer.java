import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Berkeley Algorithm - MASTER (time daemon) side.
 *
 *   1. The master reads its own clock and waits until every slave has connected and
 *      sent its clock value.
 *   2. The master computes every slave's difference from the master clock.
 *   3. It averages the differences (the master counts as difference 0).
 *   4. It sends every computer the correction it must apply (average - its own difference),
 *      so all clocks become equal to the same average time.
 *
 * Run:  java BerkeleyServer      (then start:  java BerkeleyClient)
 */
public class BerkeleyServer {

    static final int PORT = 6000;

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        System.out.println("==================================================");
        System.out.println("            BERKELEY ALGORITHM - MASTER");
        System.out.println("==================================================");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            boolean again = true;
            while (again) {
                System.out.print("\nEnter master clock time (Computer 0): ");
                double masterClock = Double.parseDouble(sc.nextLine().trim());
                System.out.print("Enter number of slave computers expected: ");
                int n = Integer.parseInt(sc.nextLine().trim());

                runRound(serverSocket, masterClock, n);

                System.out.print("\nRun another synchronisation round? (y/n): ");
                again = sc.hasNextLine() && sc.nextLine().trim().equalsIgnoreCase("y");
            }
        }
        System.out.println("Master stopped.");
    }

    static void runRound(ServerSocket serverSocket, double masterClock, int n) throws Exception {
        System.out.println("\nMaster clock = " + fmt(masterClock) + ".  Waiting for " + n
                + " slave(s) on port " + PORT + "...\n");

        double[] clocks = new double[n + 1];
        Socket[] sockets = new Socket[n + 1];
        BufferedReader[] readers = new BufferedReader[n + 1];
        PrintWriter[] writers = new PrintWriter[n + 1];
        clocks[0] = masterClock;

        // ---- step 1: collect the clock of every slave ----------------------------------
        for (int id = 1; id <= n; id++) {
            sockets[id] = serverSocket.accept();
            readers[id] = new BufferedReader(new InputStreamReader(sockets[id].getInputStream()));
            writers[id] = new PrintWriter(sockets[id].getOutputStream(), true);
            writers[id].println("WELCOME " + id);

            String message = readers[id].readLine();                 // "CLOCK <value>"
            clocks[id] = Double.parseDouble(message.split("\\s+")[1]);
            System.out.println("Slave connected  -> Computer " + id + " reports clock " + fmt(clocks[id])
                    + "   (" + id + "/" + n + ")");
        }

        // ---- step 2: differences from the master ---------------------------------------
        double[] diff = new double[n + 1];
        double sum = 0;
        for (int i = 0; i <= n; i++) {
            diff[i] = clocks[i] - masterClock;
            sum += diff[i];
        }

        // ---- step 3: average (master included) -----------------------------------------
        double average = sum / (n + 1);

        // ---- step 4: corrections -------------------------------------------------------
        double[] adjustment = new double[n + 1];
        for (int i = 0; i <= n; i++) {
            adjustment[i] = average - diff[i];
        }

        System.out.println("\n+----------+----------+------------+------------+-------------+");
        System.out.println("| Computer |  Clock   | Difference | Adjustment | New clock   |");
        System.out.println("+----------+----------+------------+------------+-------------+");
        for (int i = 0; i <= n; i++) {
            System.out.printf("| %-8s | %8s | %10s | %10s | %11s |%n",
                    i == 0 ? "0 (M)" : String.valueOf(i), fmt(clocks[i]), fmt(diff[i]),
                    fmt(adjustment[i]), fmt(clocks[i] + adjustment[i]));
        }
        System.out.println("+----------+----------+------------+------------+-------------+");
        System.out.println("Sum of differences     : " + fmt(sum));
        System.out.println("Average difference     : " + fmt(sum) + " / " + (n + 1) + " = " + fmt(average));
        System.out.println("Synchronised time      : " + fmt(masterClock + average));

        for (int id = 1; id <= n; id++) {
            writers[id].println("ADJUST " + adjustment[id]);
            System.out.println("Correction sent    -> Computer " + id + ": " + fmt(adjustment[id]));
        }
        System.out.println("Master (Computer 0) applies its own correction: " + fmt(adjustment[0]));

        for (int id = 1; id <= n; id++) {
            sockets[id].close();
        }
    }

    static String fmt(double v) {
        return (v == Math.rint(v)) ? String.valueOf((long) v) : String.format("%.2f", v);
    }
}
