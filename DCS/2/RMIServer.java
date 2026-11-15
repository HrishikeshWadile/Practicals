import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * RMI Server - creates the remote object, starts the RMI registry (port 1099) and binds
 * the object under the name "Calculator". It then waits; every remote call is printed.
 *
 * Run:  java RMIServer          (then start:  java RMIClient)
 */
public class RMIServer {

    public static void main(String[] args) {
        try {
            // Makes the stub carry "localhost" instead of a wrong network-adapter address
            if (System.getProperty("java.rmi.server.hostname") == null) {
                System.setProperty("java.rmi.server.hostname", "localhost");
            }

            Calculator calculator = new CalculatorImpl();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("Calculator", calculator);

            System.out.println("==================================================");
            System.out.println("                   RMI SERVER");
            System.out.println("==================================================");
            System.out.println("RMI registry started on port 1099");
            System.out.println("Remote object bound as : Calculator");
            System.out.println("Waiting for remote calls (press Ctrl+C to stop)...\n");

        } catch (Exception e) {
            System.out.println("Server error: " + e);
        }
    }
}
