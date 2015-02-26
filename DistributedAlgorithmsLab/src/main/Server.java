package main;

import impl.ClientImpl;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import model.Client;
import model.RegisterService;

public class Server extends java.rmi.server.UnicastRemoteObject implements RegisterService {
	private static Server _instance;
	private int port;
	private ArrayList<ClientImpl> clients;
	private Registry registry;
	
	public Registry getRegistry(){
		return registry;
	}
	
	public Server(int port) throws RemoteException {
		this.port = port;
		clients = new ArrayList<ClientImpl>();
		
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}
		this.registry = LocateRegistry.createRegistry(port);
	}
	
	
	
	@Override
	public void register(ClientImpl c) throws RemoteException {
		// TODO Auto-generated method stub
		clients.add(c);
		// TODO synchronised?
	}
	
	public void activate() {
		for(int i=0; i<clients.size(); i++){
			clients.get(i).activate(clients, i);
		}		
	}
	
	
	
	//TODO; implement connectToServerService
	
	public static void main(String[] args) throws RemoteException, InterruptedException, NotBoundException, AlreadyBoundException{
		// Args; first arg is required; amount of milliseconds for registration period
		// second arg is optional, and is the port for the registry
		int port = 1099;
//		if(!args[1].isEmpty()){
//			port = Integer.parseInt(args[1]);
//		}
		// int millis = Integer.parseInt(args[0]);
		_instance = new Server(port);
		Registry r = _instance.getRegistry();
		r.bind("register", _instance);
		Thread.sleep(20000);
		_instance.activate();
		// r.unbind("register");
		
		
	}

	

}
