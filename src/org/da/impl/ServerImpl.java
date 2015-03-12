package org.da.impl;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.da.model.Client;
import org.da.model.Server;
import org.da.util.PeerEntry;

public class ServerImpl extends java.rmi.server.UnicastRemoteObject implements Server {

	private List<Client> clients;
	protected ServerImpl() throws RemoteException {
		super();
		clients = Collections.synchronizedList(new ArrayList<Client>());
	}

	@Override
	public void register(Client c) throws RemoteException {
		clients.add(c);
	}
	
	private List<PeerEntry> generatePeerEntries(){
		List<PeerEntry> retval = new ArrayList<PeerEntry>();
		for(int i = 0; i<clients.size(); i++){
			retval.add(new PeerEntry(i, clients.get(i)));
		}
		return retval;
	}
	private void activateClients() throws RemoteException{
		List<PeerEntry> peers = generatePeerEntries();
		int index =0;
		for(Client c: clients){
			c.activate(peers, index++);
		}		
	}
	
	public static void main(String [] args) throws RemoteException, InterruptedException{
		ServerImpl s= new ServerImpl();
		Registry registry = LocateRegistry.createRegistry(1089);
		if(args.length < 1){
			throw new RuntimeException("First argument for server should be amount of ms before activate event");
		}
		int wait = Integer.valueOf(args[0]);
		registry.rebind("register", s);
		System.out.println("Waiting for " + wait + " ms");
		Thread.sleep(wait);
		s.activateClients();
		System.out.println("Activated all clients, server is useless now");
	}

}
