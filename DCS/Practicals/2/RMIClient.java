import java.rmi.Naming;
import java.util.Scanner;
import java.util.concurrent.*;

/**
 * RMI Client - looks up the remote Calculator and calls its methods.
 * Menu option 6 fires several calls at the same time so that the server window shows
 * them being served by different threads (multi-threaded RMI).
 *
 * Run:  java RMIClient [host]
 */
public class RMIClient {

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";

        try (Scanner sc = new Scanner(System.in)) {
            Calculator calc = (Calculator) Naming.lookup("rmi://" + host + ":1099/Calculator");

            System.out.println("==================================================");
            System.out.println("                   RMI CLIENT");
            System.out.println("==================================================");
            System.out.println("Connected to RMI Server at " + host + ":1099 (object: Calculator)");

            boolean running = true;
            while (running) {
                System.out.println("\n  1. Add          4. Divide");
                System.out.println("  2. Subtract     5. All four operations");
                System.out.println("  3. Multiply     6. Concurrent calls (multi-threaded demo)");
                System.out.println("                  7. Exit");
                System.out.print("Choice: ");
                if (!sc.hasNextLine()) break;
                String choice = sc.nextLine().trim();

                try {
                    switch (choice) {
                        case "1": case "2": case "3": case "4": case "5": {
                            System.out.print("Enter the first number : ");
                            int a = Integer.parseInt(sc.nextLine().trim());
                            System.out.print("Enter the second number: ");
                            int b = Integer.parseInt(sc.nextLine().trim());
                            System.out.println();
                            if (choice.equals("1") || choice.equals("5"))
                                System.out.println("Addition       : " + a + " + " + b + " = " + calc.add(a, b));
                            if (choice.equals("2") || choice.equals("5"))
                                System.out.println("Subtraction    : " + a + " - " + b + " = " + calc.subtract(a, b));
                            if (choice.equals("3") || choice.equals("5"))
                                System.out.println("Multiplication : " + a + " * " + b + " = " + calc.multiply(a, b));
                            if (choice.equals("4") || choice.equals("5"))
                                System.out.println("Division       : " + a + " / " + b + " = " + calc.divide(a, b));
                            break;
                        }
                        case "6":
                            concurrentDemo(calc);
                            break;
                        case "7":
                            running = false;
                            break;
                        default:
                            System.out.println("Invalid choice.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Please enter whole numbers only.");
                } catch (ArithmeticException e) {
                    System.out.println("Server reported an error: " + e.getMessage());
                } catch (java.rmi.RemoteException e) {
                    System.out.println("Remote error: " + e.getMessage());
                }
            }
            System.out.println("Client closed.");

        } catch (java.rmi.ConnectException e) {
            System.out.println("Could not connect - start RMIServer first.");
        } catch (Exception e) {
            System.out.println("Client error: " + e);
        }
    }

    /** Five threads call the server at the same time. */
    static void concurrentDemo(Calculator calc) throws InterruptedException {
        System.out.println("Sending 5 calls at the same time...");
        ExecutorService pool = Executors.newFixedThreadPool(5);
        CountDownLatch go = new CountDownLatch(1);
        for (int i = 1; i <= 5; i++) {
            final int n = i;
            pool.execute(() -> {
                try {
                    go.await();
                    System.out.println("  client thread " + n + ": " + n + " * " + (n + 1) + " = " + calc.multiply(n, n + 1));
                } catch (Exception e) {
                    System.out.println("  client thread " + n + " failed: " + e.getMessage());
                }
            });
        }
        go.countDown();
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println("Done - check the server window to see which threads handled the calls.");
    }
}
