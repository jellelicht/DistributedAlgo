package impl;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

import util.PeerEntry;
import model.Client;
import model.Message;
import model.Server;

public class ClientImpl extends java.rmi.server.UnicastRemoteObject implements Client{

	private static final long serialVersionUID = 1L;
	
	protected ClientImpl() throws RemoteException {
		super();
	}

	@Override
	public void putMessage(Message msg) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void activate(List<PeerEntry> peers, Integer ownId)
			throws RemoteException {
		// TODO Auto-generated method stub
		
	}
	
	// Algorithm implementation
	public void mainLoop() {
		
	}
	
	public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException{
		// setup registry
		Server server = (Server) java.rmi.Naming.lookup("rmi://localhost:1089/register");				
		ClientImpl c = new ClientImpl();
		
	}
}
