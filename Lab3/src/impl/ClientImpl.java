package impl;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
	private boolean loopFlag = false;
	
	private class PotentialOwnerData {
		public int level;
		public int id;
	}
	
	private class CandidateData {
		private boolean killed;
		private Map<Integer, Peer> untraversed;
		private int level;
		private ClientImpl c;
		private Random rndGen;
		
		public CandidateData(ClientImpl c){
			this.killed = false;
			this.untraversed = new HashMap<Integer, Peer>();// Does this copy or mutate?
			for (PeerEntry pe : c.peers){
				if(pe.peerId != c.id ){
					untraversed.put(pe.peerId, pe.p);
				}
			}
			this.level = 0;
			this.c = c;
			this.rndGen = new Random();
		}
		
		public void handleMessage(Message m) throws RemoteException{
			System.out.println(Integer.compare(m.getPId(), c.id) == 0);
			if(killed == false && (Integer.compare(m.getPId(), c.id) == 0)){
				this.level++;
				System.out.println("I captured someone :D " + this.level );
				this.untraversed.remove(m.getOriginId());
			} else {
				if (m.getPId() < c.id) {
					System.out.println("SHOULD NOT HAPPEN");// Should not happen (should happen in ordinary message handling?)
				} else { // MessageType should be RECAPTURED
					System.out.println("This should say RECAPTURED: " + m.getMessageType().toString());
					//Send killed to c.peers.get(m.getPId()).p.putMessage(...)
					c.peers.get(m.getPId()).p.putMessage(killedMessage(m));
					this.killed = true;
				}
			}
		}
		
		public void attemptCapture() throws RemoteException{
			int index = rndGen.nextInt(untraversed.size());
			
			Peer p = (Peer) untraversed.values().toArray()[index];

			p.putMessage(captureMessage());

			//capMessageSent.put(p,true);
			//untraversed.remove(index);
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
			System.out.println("Size: " + this.untraversed.size());
			return this.killed == false && this.untraversed.isEmpty();
		}
	}
	private CandidateData cd;
	private Integer potentialOwner;
	private PotentialOwnerData pod;
	private Integer owner = -1;
	
	private boolean isCandidate = false;
	private int loopCounter = 100;
	
	private static final long serialVersionUID = 1L;
	
	
	protected ClientImpl() throws RemoteException {
		super();
		this.mq = new MessageDeliveryQueue();
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
//		if(this.id == 0){
			this.isCandidate = true;
			this.cd = new CandidateData(this);
//		}
		this.loopFlag = true;
	}
	
	// Algorithm implementation
	public void mainLoop() throws InterruptedException {
		// Pull messages from loop
		System.out.println("Loop nr: " + this.loopCounter);
		this.loopCounter--;
		Message m = mq.pop();
		if(m != null){
			System.out.println("handling message " + m.getMessageType().toString());
			try {
				handleMessage(m);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		if (isCandidate){
			System.out.println("Candidate stuff:");
			if(cd.isElected()){
				System.out.println("I was elected!: Pid: " + this.id);
				loopCounter = 0;
			}
			else if (cd.isAlive()){
				try {
					System.out.println("attempt capture");
					cd.attemptCapture();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				 System.out.println("Oh noes, dead candidate");
			}
		}
		
		if(this.loopCounter > 0) {
			Thread.sleep(1000);
			this.mainLoop();
		} else {
			return;
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
		System.out.println("Changing owner from " + owner + " to " + this.pod.id);
		this.owner = this.pod.id;
		
		this.peers.get(this.owner).p.putMessage(new MessageImpl(MessageType.CAPTURED, this.pod.level, this.pod.id, this.id));
	}
	
	public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException, InterruptedException{
		// setup registry
		Server server = (Server) java.rmi.Naming.lookup("rmi://localhost:1089/register");				
		ClientImpl c = new ClientImpl();
		
		server.register(c); 
		Thread.sleep(10000);
		
		int waitRounds = 20;
		while(!c.loopFlag && waitRounds > 0){
			Thread.sleep(500);
			waitRounds--;
		}
			Thread.sleep(2000); // settling time for other processes
			c.mainLoop();
	}
}
