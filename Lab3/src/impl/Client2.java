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

public class Client2 extends java.rmi.server.UnicastRemoteObject implements Client{

	private Integer id;
	private Integer noAckSent = 0;
	private Integer noTimesCaptured = 0;
	private List<PeerEntry> peers;
	private MessageDeliveryQueue mq;
	private boolean loopFlag = false;
	
	private class OwnerTuple implements Comparable<OwnerTuple> {
		public int level;
		public int id;
		@Override
		public int compareTo(OwnerTuple o) {
			// TODO Auto-generated method stub
			//Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
			if(this.level < o.level) return -1;
			if(this.level == o.level){
				if(this.id == o.id) return 0;
				if(this.id < o.id) return -1;
			}
			return 1;
		}
	}
	
	private Peer potential_father = null;
	private Peer father = null;
	private class Candidate{
		//private Map<Integer, Peer> untraversed;
		private List<PeerEntry> untraversed;
		private boolean killed = false;
		private PeerEntry capturing = null;
		private Client2 c;
		Random rndGen = new Random();

		
		public Candidate(Client2 c, List<PeerEntry> peers){
			this.c = c;
			this.untraversed = new ArrayList<PeerEntry>();
			for(PeerEntry pe: peers){
				if(pe.peerId == this.c.id){
					continue;
				}
				untraversed.add(pe);
			}
		}
		
		private boolean canCapture(){
			return !untraversed.isEmpty() && !killed && capturing ==null;
		}
		
		public void attemptCapture() throws RemoteException{
			if(canCapture()){
				int index = rndGen.nextInt(untraversed.size());
				capturing = untraversed.get(index);
				capturing.p.putMessage(new MessageImpl(MessageType.ANY, od.level, od.id, c.id));
			} else {
				System.out.println("Can't capture");
			}
		}
		
		public void handleMessage(Message m) throws RemoteException{
			PeerEntry linkE = peers.get(m.getLink());
			OwnerTuple ph = new OwnerTuple();
			ph.id = m.getPId();
			ph.level = m.getLevel();
			if((ph.id == id) && !killed){
				od.level++;
				untraversed.remove(linkE);
				capturing = null;
			} else if (ph.compareTo(od) == -1) {
				//ignore
			} else {
				System.out.println("[CANDIDATE] Got killed to peer " + m.getPId() + " (received : " + m.getMessageType().toString());
				linkE.p.putMessage(new MessageImpl(MessageType.ANY, ph.level, ph.id, c.id));
				killed = true;
			}			
		}

		public boolean isElected() {
			return untraversed.isEmpty() && !killed;
		}
		
 
	}
	
	private Candidate cd;
	private OwnerTuple od;

	private boolean isCandidate = false;
	private int loopCounter = 100;
	
	private static final long serialVersionUID = 1L;
	
	
	protected Client2() throws RemoteException {
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
		this.od = new OwnerTuple();
		this.od.id = -1;
		this.od.level = 0;
		if(isCand) {
			System.out.println("[CANDIDATE] proc " + this.id +  " is a candidate process");
			this.isCandidate = true;
			this.cd = new Candidate(this,  this.peers);
			this.od.id = this.id;
		}
		this.loopFlag = true;
		System.out.println("LOLWUT SET");
	}
	
	// Algorithm implementation
	public void mainLoop() throws InterruptedException, RemoteException {
		// Pull messages from loop
		System.out.println("[MAIN] Loop counter " + this.loopCounter);
		this.loopCounter--;
		Message m = mq.pop();
		if(m != null){
			System.out.println("[MESSAGE] handling received message of type " + m.getMessageType().toString());
			try {
				if(isCandidate){
					cd.handleMessage(m);
				} else {
					handleMessage(m);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}	
		if(isCandidate){
			cd.attemptCapture();
			if(cd.isElected()){
				this.loopCounter = 0;
				System.out.println("I was elected: " + this.id + " level: " + this.od.level);
			}
		}
		if(this.loopCounter > 0) {
			Thread.sleep(1000);
			this.mainLoop();
		} else {
			System.out.println("PId: " + this.id + " Acks: " + this.noAckSent + " captured: " + this.noTimesCaptured);
			return;
		}
	}
	
	private void handleMessage(Message m) throws RemoteException{
		if(peers == null) System.out.println("LOLWUT");
		PeerEntry linkE = peers.get(m.getLink());
		OwnerTuple ph = new OwnerTuple();
		ph.id = m.getPId();
		ph.level = m.getLevel();
		switch(ph.compareTo(od)){
		case -1:
			break;
		case 1:
			potential_father = linkE.p;
			od = ph;
			if(father == null) father = potential_father;
			father.putMessage(new MessageImpl(MessageType.ANY, ph.level, ph.id, this.id));
			this.noTimesCaptured++;
			break;
		case 0:
			father = potential_father;
			father.putMessage(new MessageImpl(MessageType.ANY, ph.level, ph.id, this.id));
			this.noAckSent++;
			break;
		}
		return;
	}
	
	public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException, InterruptedException{
		// setup registry
		Server server = (Server) java.rmi.Naming.lookup("rmi://localhost:1089/register");				
		Client2 c = new Client2();
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
		
		int waitRounds = 100;
		
		while(!c.loopFlag && waitRounds > 0){
			Thread.sleep(500);
			waitRounds--;
		}
			Thread.sleep(2000); // settling time for other processes
			System.out.println("[MAIN] starting process with id " + c.id);
			c.mainLoop();
	}
}
