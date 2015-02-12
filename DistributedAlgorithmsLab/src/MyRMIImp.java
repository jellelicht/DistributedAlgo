import java.rmi.*;


public class MyRMIImp implements MyRMI {

	@SuppressWarnings("deprecation")
	@Override
	public void testMethod(int arg) throws RemoteException {
		// Create and install a security manager
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}
		
		
	}

}
