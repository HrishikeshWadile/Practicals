import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Berkeley Algorithm - SLAVE side.
 * Enter how many slave computers this window should start and the clock of each one.
 * Each slave opens its own connection to the master, sends its clock and receives the
 * correction it has to apply. The master window shows the full calculation.
 *
 * Run:  java BerkeleyClient [host]
 */
public class BerkeleyClient {

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        Scanner sc = new Scanner(System.in);

        System.out.println("==================================================");
        System.out.println("            BERKELEY ALGORITHM - SLAVES");
        System.out.println("==================================================");
        System.out.print("How many slave computers do you want to start here? ");
        int k = Integer.parseInt(sc.nextLine().trim());

        double[] clock = new double[k];
        Socket[] socket = new Socket[k];
        BufferedReader[] in = new BufferedReader[k];
        int[] id = new int[k];

        try {
            for (int i = 0; i < k; i++) {
                socket[i] = new Socket(host, 6000);
                in[i] = new BufferedReader(new InputStreamReader(socket[i].getInputStream()));
                PrintWriter out = new PrintWriter(socket[i].getOutputStream(), true);

                id[i] = Integer.parseInt(in[i].readLine().split("\\s+")[1]);   // "WELCOME <id>"
                System.out.print("Enter clock time of Computer " + id[i] + ": ");
                clock[i] = Double.parseDouble(sc.nextLine().trim());
                out.println("CLOCK " + clock[i]);
                System.out.println("  Computer " + id[i] + " sent its clock " + fmt(clock[i]) + " to the master.");
            }

            System.out.println("\nWaiting for the master to calculate...\n");

            for (int i = 0; i < k; i++) {
                double adjustment = Double.parseDouble(in[i].readLine().split("\\s+")[1]);   // "ADJUST <value>"
                System.out.println("Computer " + id[i] + ": old clock " + fmt(clock[i])
                        + "   adjustment " + (adjustment >= 0 ? "+" : "") + fmt(adjustment)
                        + "   NEW CLOCK " + fmt(clock[i] + adjustment));
                socket[i].close();
            }
            System.out.println("\nAll slave clocks are synchronised.");

        } catch (ConnectException e) {
            System.out.println("Could not connect - start BerkeleyServer first.");
        } catch (Exception e) {
            System.out.println("Client error: " + e);
        }
    }

    static String fmt(double v) {
        return (v == Math.rint(v)) ? String.valueOf((long) v) : String.format("%.2f", v);
    }
}
