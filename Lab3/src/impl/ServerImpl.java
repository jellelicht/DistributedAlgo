package impl;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import util.PeerEntry;
import model.Client;
import model.Server;

public class ServerImpl extends java.rmi.server.UnicastRemoteObject implements Server {
	
	private static final long serialVersionUID = 1L;
	private List<Client> clients;
	private Random rndGen;
	
	protected ServerImpl() throws RemoteException {
		super();
		this.clients = Collections.synchronizedList(new ArrayList<Client>());
		this.rndGen = new Random();
	}

	@Override
	public void register(Client c) throws RemoteException {
		clients.add(c);
	}
	
	private void activateClients() throws RemoteException{
		List<PeerEntry> peers = generatePeerEntries();
		int index =0;
		boolean isCand = false;
		for(Client c: clients){
			isCand = rndGen.nextBoolean();
			c.activate(peers, index++, isCand);
		}		
	}
	
	private List<PeerEntry> generatePeerEntries(){
		List<PeerEntry> retval = new ArrayList<PeerEntry>();
		for(int i = 0; i<clients.size(); i++){
			retval.add(new PeerEntry(i, clients.get(i)));
		}
		return retval;
	}
	
	public static void main(String [] args) throws RemoteException, InterruptedException{
//		if(args.length < 1){
//			throw new RuntimeException("First argument for server should be amount of ms before activate event");
//		}
		int wait = 10000; //Integer.valueOf(args[0]);

		ServerImpl s= new ServerImpl();
		Registry registry = LocateRegistry.createRegistry(1089);
		registry.rebind("register", s);
		Thread.sleep(wait);
		System.out.println("Server started:");
		s.activateClients();		
	}
}
