package impl;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import util.MessageDeliveryQueue;
import util.PeerEntry;
import model.Client;
import model.Message;
import model.Peer;
import model.Server;

public class ClientImpl extends java.rmi.server.UnicastRemoteObject implements Client{

	private Integer id;
	private List<PeerEntry> peers;
	private MessageDeliveryQueue mq;
	
	private class CandidateData {
		private boolean killed;
		private List<Peer> untraversed;
		private int level;
		private ClientImpl c;
		private Random rndGen;
		
		public CandidateData(ClientImpl c){
			this.killed = false;
			this.untraversed = new ArrayList(peers);// Does this copy or mutate?
			this.level = 0;
			this.c = c;
			this.rndGen = new Random();
		}
		
		public void handleMessage(Message m){
			if(killed == false && m.getPId() == c.id){
				this.level++;
				this.untraversed.remove(c.peers.get(m.getPId()));
			} else {
				if (m.getPId() < c.id) {
					// Should not happen (should happen in ordinary message handling?)
				} else {
					//Send killed to c.peers.get(m.getPId()).p.putMessage(...)
					this.killed = true;
				}
			}
		}
		
		public void attemptCapture(){
			int index = rndGen.nextInt(untraversed.size());
			Peer p = untraversed.get(index);
			// Send CAPTURE to p.putMessage(...)
		}
		
		private Message killedMessage(Message recap){
			return null;
		}
		
		private Message captureMessage(){
			return null;
		}
		
		public boolean isAlive(){
			return !this.killed;
		}
		public boolean isElected(){
			return this.killed == false && this.untraversed.isEmpty();
		}
	}
	
	private Integer captorLevel = -1;
	private Integer captorId;
	
	private boolean isCandidate = false;
	
	
	private static final long serialVersionUID = 1L;
	
	
	protected ClientImpl() throws RemoteException {
		super();
	}
	

	@Override
	public void putMessage(Message msg) throws RemoteException {
		// TODO Auto-generated method stub
		mq.insert(msg);
	}

	@Override
	public void activate(List<PeerEntry> peers, Integer ownId)
			throws RemoteException {
		// TODO Auto-generated method stub
		this.id = ownId;
		this.peers = peers;
	}
	
	// Algorithm implementation
	public void mainLoop() {
		// Pull messages from loop
		// 
		
		
	}
	
	public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException{
		// setup registry
		Server server = (Server) java.rmi.Naming.lookup("rmi://localhost:1089/register");				
		ClientImpl c = new ClientImpl();
	}
}
