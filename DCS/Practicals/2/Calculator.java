import java.rmi.Remote;
import java.rmi.RemoteException;

/** Remote interface - the methods a client may call on the server. */
public interface Calculator extends Remote {

    int add(int a, int b) throws RemoteException;

    int subtract(int a, int b) throws RemoteException;

    int multiply(int a, int b) throws RemoteException;

    int divide(int a, int b) throws RemoteException;
}
