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
import model.MessageType;
import model.Peer;
import model.Server;

public class ClientImpl extends java.rmi.server.UnicastRemoteObject implements Client{

	private Integer id;
	private List<PeerEntry> peers;
	private MessageDeliveryQueue mq;
	
	private class PotentialOwnerData {
		public int level;
		public int id;
	}
	
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
		
		public void handleMessage(Message m) throws RemoteException{
			if(killed == false && m.getPId() == c.id){
				this.level++;
				this.untraversed.remove(c.peers.get(m.getPId()));
			} else {
				if (m.getPId() < c.id) {
					// Should not happen (should happen in ordinary message handling?)
				} else { // MessageType should be RECAPTURED
					//Send killed to c.peers.get(m.getPId()).p.putMessage(...)
					c.peers.get(m.getPId()).p.putMessage(killedMessage(m));
					this.killed = true;
				}
			}
		}
		
		public void attemptCapture() throws RemoteException{
			int index = rndGen.nextInt(untraversed.size());
			Peer p = untraversed.get(index);
			p.putMessage(captureMessage());
		}
		
		private Message killedMessage(Message recap){
			return new MessageImpl(MessageType.KILLED, recap.getLevel(), recap.getPId(), c.id);
		}
		
		private Message captureMessage(){
			return new MessageImpl(MessageType.CAPTURE, level, c.id, c.id);
		}
		
		public boolean isAlive(){
			return !this.killed;
		}
		public boolean isElected(){
			return this.killed == false && this.untraversed.isEmpty();
		}
	}
	private CandidateData cd;
	private Integer potentialOwner;
	private PotentialOwnerData pod;
	private Integer owner = -1;
	
	private boolean isCandidate = false;
	
	
	private static final long serialVersionUID = 1L;
	
	
	protected ClientImpl() throws RemoteException {
		super();
	}
	

	@Override
	public void putMessage(Message msg) throws RemoteException {
		mq.insert(msg);
	}

	@Override
	public void activate(List<PeerEntry> peers, Integer ownId)
			throws RemoteException {
		this.id = ownId;
		this.peers = peers;
		this.pod = new PotentialOwnerData();
		this.pod.id = -1;
		this.pod.level = -1;
		// HARDCODED:
		if(this.id == 1){
			this.isCandidate = true;
			this.cd = new CandidateData(this);
		}
	}
	
	// Algorithm implementation
	public void mainLoop() {
		// Pull messages from loop
		Message m = mq.pop();
		if(m != null){
			try {
				handleMessage(m);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void handleMessage(Message m) throws RemoteException{
		switch(m.getMessageType()){
		case CAPTURE:
			if(m.getPId() > this.pod.id) {
				this.potentialOwner = m.getPId();
				this.pod.id = m.getPId();
				this.pod.level = m.getLevel();
				if (this.owner == -1){
					this.promoteOwner(m);
				} else {
					this.peers.get(this.owner).p.putMessage(new MessageImpl(MessageType.RECAPTURED, this.pod.level, this.pod.id, this.id));
				}
			}
			break;
		case RECAPTURED:
			cd.handleMessage(m);
			break;
		case CAPTURED:
			cd.handleMessage(m);
			break;
		case KILLED:
			this.promoteOwner(m);
			break;
		}
	}
	
	private void promoteOwner(Message msg) throws RemoteException{
		this.owner = this.pod.id;
		this.peers.get(this.owner).p.putMessage(new MessageImpl(MessageType.CAPTURED, this.pod.level, this.pod.id, this.id));
	}
	
	public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException{
		// setup registry
		Server server = (Server) java.rmi.Naming.lookup("rmi://localhost:1089/register");				
		ClientImpl c = new ClientImpl();
	}
}
