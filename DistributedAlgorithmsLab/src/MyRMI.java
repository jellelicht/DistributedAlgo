import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

public interface MyRMI extends Remote {
	
	public void testMethod(int arg) throws java.rmi.RemoteException;

}
