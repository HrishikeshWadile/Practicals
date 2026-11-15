import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Remote object. Every call that arrives is printed on the server window together with
 * the caller's address and the server thread that handled it (RMI is multi-threaded:
 * simultaneous calls are executed by different threads).
 */
public class CalculatorImpl extends UnicastRemoteObject implements Calculator {

    private final AtomicInteger callCount = new AtomicInteger(0);

    public CalculatorImpl() throws RemoteException {
        super();
    }

    private void log(String operation, int a, int b, String result) {
        String client;
        try {
            client = RemoteServer.getClientHost();
        } catch (ServerNotActiveException e) {
            client = "local";
        }
        System.out.printf("[%s] call #%-3d from %-10s thread %-28s %s(%d, %d) = %s%n",
                new SimpleDateFormat("HH:mm:ss").format(new Date()),
                callCount.incrementAndGet(), client, Thread.currentThread().getName(),
                operation, a, b, result);
    }

    @Override
    public int add(int a, int b) throws RemoteException {
        int r = a + b;
        log("add", a, b, String.valueOf(r));
        return r;
    }

    @Override
    public int subtract(int a, int b) throws RemoteException {
        int r = a - b;
        log("subtract", a, b, String.valueOf(r));
        return r;
    }

    @Override
    public int multiply(int a, int b) throws RemoteException {
        int r = a * b;
        log("multiply", a, b, String.valueOf(r));
        return r;
    }

    @Override
    public int divide(int a, int b) throws RemoteException {
        if (b == 0) {
            log("divide", a, b, "ERROR (division by zero)");
            throw new ArithmeticException("Cannot divide by zero");
        }
        int r = a / b;
        log("divide", a, b, String.valueOf(r));
        return r;
    }
}
