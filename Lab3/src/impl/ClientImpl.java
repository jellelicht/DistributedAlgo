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
		private int capturing = -1;
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
		
		private boolean equalityCheck(Message m){
			return (m.getPId() < c.id) && (m.getLevel() < this.level);
		}
		
		public void handleMessage(Message m) throws RemoteException{
			if(killed == false && (Integer.compare(m.getPId(), c.id) == 0)){
				this.level++;
				System.out.println("[CANDIDATE] captured " + m.getOriginId() );
				this.untraversed.remove(m.getOriginId());
				this.capturing = -1;
			} else {
				if ( this.equalityCheck(m) ) {
					System.out.println("[YOLO] SHOULD NOT HAPPEN");// Should not happen (should happen in ordinary message handling?)
				} else { // MessageType should be RECAPTURED
					//Send killed to c.peers.get(m.getPId()).p.putMessage(...)
					System.out.println("[CANDIDATE] Got killed");
					c.peers.get(m.getPId()).p.putMessage(killedMessage(m));
					this.killed = true;
				}
			}
		}
		
		public void attemptCapture() throws RemoteException{
			if(this.capturing != -1) {
				System.out.println("[CANDIDATE] Outstanding capture request [index]" + capturing);
				return;
			}
			int index = rndGen.nextInt(untraversed.size());
			
			Peer p = (Peer) untraversed.values().toArray()[index];

			p.putMessage(captureMessage());
			this.capturing = index;

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
	public void activate(List<PeerEntry> peers, Integer ownId, Boolean isCand)
			throws RemoteException {
		this.id = ownId;
		this.peers = peers;
		this.pod = new PotentialOwnerData();
		this.pod.id = -1;
		this.pod.level = -1;
		if(isCand) {
			System.out.println("[CANDIDATE] proc " + this.id +  " is a candidate process");
			this.isCandidate = true;
			this.cd = new CandidateData(this);
			this.owner = ownId;
		}
		this.loopFlag = true;
	}
	
	// Algorithm implementation
	public void mainLoop() throws InterruptedException {
		// Pull messages from loop
		System.out.println("[MAIN] Loop counter " + this.loopCounter);
		this.loopCounter--;
		Message m = mq.pop();
		if(m != null){
			System.out.println("[MESSAGE] handling received message of type " + m.getMessageType().toString());
			try {
				handleMessage(m);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		if (isCandidate){
			if(cd.isElected()){
				System.out.println("[CANDIDATE] - ELECTED: " + this.id);
				loopCounter = 0;
			}
			else if (cd.isAlive()){
				try {
					cd.attemptCapture();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				 System.out.println("[CANDIDATE] dead candidate - noop");
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
				
			} else {
				System.out.println("[CAPTURE] ignoring capture from " + m.getPId() +  " while this.pod.id = " + this.pod.id);
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
		if(this.pod.id == -1){
			System.out.println("[OWNER] Killed before getting pod");
			return;
		}
		System.out.println("[OWNER] Changing owner from " + owner + " to " + this.pod.id);
		this.owner = this.pod.id;
		
		this.peers.get(this.owner).p.putMessage(new MessageImpl(MessageType.CAPTURED, this.pod.level, this.pod.id, this.id));
	}
	
	public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException, InterruptedException{
		// setup registry
		Server server = (Server) java.rmi.Naming.lookup("rmi://localhost:1089/register");				
		ClientImpl c = new ClientImpl();
		int loops;// = 10000; //Integer.valueOf(args[0]);
		if(args.length < 1){
			loops = 100;
			//throw new RuntimeException("First argument for server should be amount of ms before activate event");
		} else {
			loops = Integer.valueOf(args[0]);
		}
		c.loopCounter = loops;
		
		
		server.register(c); 
		Thread.sleep(1000);
		
		int waitRounds = 20;
		
		while(!c.loopFlag && waitRounds > 0){
			Thread.sleep(500);
			waitRounds--;
		}
			Thread.sleep(2000); // settling time for other processes
			System.out.println("[MAIN] starting process with id " + c.id);
			c.mainLoop();
	}
}
