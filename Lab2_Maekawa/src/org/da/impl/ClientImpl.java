package org.da.impl;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.da.model.Client;
import org.da.model.Link;
import org.da.model.Message;
import org.da.model.MessageType;
import org.da.model.Peer;
import org.da.model.Server;
import org.da.model.TimeStamp;
import org.da.util.MessageDeliveryQueue;
import org.da.util.PeerEntry;

public class ClientImpl extends java.rmi.server.UnicastRemoteObject implements Client {

	private static final long serialVersionUID = 1L;
	private GrantData grantData;
	private List<PeerEntry> peers;
	private Map<Integer, Link> requestSet;
	private MessageDeliveryQueue requestBacklog;
	private MessageDeliveryQueue queue;
	private List<Integer> inquirers;
	private Boolean inCriticalState;
	private Boolean inquiring;
	private Boolean inquired;
	private Boolean postponed;
	private Boolean hasGrantedProcess;
	private Integer pid;
	private TimeStamp ts;
	private int noGrants;
	private boolean outstandingRequest;

	@Override
	public String toString() {
		return "ClientImpl [grantData=" + grantData + ", inCriticalState="
				+ inCriticalState + ", inquiring=" + inquiring + ", inquired="
				+ inquired + ", postponed=" + postponed
				+ ", hasGrantedProcess=" + hasGrantedProcess + ", pid=" + pid
				+ ", ts=" + ts + ", noGrants=" + noGrants
				+ ", outstandingRequest=" + outstandingRequest + "]";
	}

	protected ClientImpl() throws RemoteException {
		super();
		requestSet = new HashMap<Integer, Link>();
	}

	private boolean getRandBoolean(){
	    Random random = new Random();
	    return random.nextBoolean();
	}


	// upon receiving a request message from a process
	private void grantProccess(int pid) {
		// Send grantmsg to the process
	}
	
	private Message topMsg(){
		return null;
	}
	
	private Message pop(){
		return null;
	}
	
	// Upon receiving a release message from a process
	private void releaseProccess(int pid) {
		
	}
	
	// Enter critical state
	private void enterCriticalState(){
		log("Activate critical state");
		this.inCriticalState = true;
		this.noGrants = 0;
	}
	
	@Override
	public void putMessage(Message msg) throws RemoteException{
		int id = msg.getPID();
		Link l = this.requestSet.get(id);
		l.addMessage(msg);
	}
	
	private void setupLinks(){
		for(PeerEntry pe: this.peers){
			requestSet.put(pe.peerId, new LinkImpl());
		}
	}

	
	// msg should be either REQUEST OR RELEASE
	public void BroadCastMsg(Message msg) {
		log("Broadcasting message /and inc stuff"+ msg.toString());
		this.ts = this.ts.inc();
		Message newMessage = new MessageImpl(msg.getMessageType(), msg.getTimeStamp().inc(), this.pid);
		for (PeerEntry pe : this.peers) {
			try {
				pe.p.putMessage(newMessage);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		if(!this.hasGrantedProcess) {
//			Message msg = new MessageImpl(MessageType.REQUEST, this.ts, this.pid);
//
//		}
	}
	
	

	public void ReceiveMsg(Message msg) throws RemoteException {
		this.ts = this.ts.sync(msg.getTimeStamp());
		log("ReceiveMsg: " + msg.toString());
		switch(msg.getMessageType()) {
			case REQUEST :
				this.requestBacklog.insert(msg);
				if(this.hasGrantedProcess) {
					Message request = this.requestBacklog.peek();
					if(msg.getTimeStamp().compareTo(request.getTimeStamp()) == -1 &&
							grantData.ts.compareTo(msg.getTimeStamp()) == -1){
						sendMsg(MessageType.POSTPONED, msg.getPID());
					}
				}	
				break;
			case GRANT : 
				log("Received grant, old noGrants:" + noGrants);
				this.noGrants += 1;
				if(this.noGrants == this.requestSet.size()) {
					log("Enough grants to enter CS");
					this.postponed = false;
					enterCriticalState();
				}
				break;
			case INQUIRE :
				this.inquired = true;
				this.inquirers.add(msg.getPID());
				break;
			case RELINQUISH : 
				this.inquiring = false;
				this.hasGrantedProcess = false;
				//this.requestBacklog.insert(new MessageImpl(MessageType.REQUEST, this.grantData.ts, this.grantData.pid));
				break;
			case RELEASE : 
				Release();
				break;
			case POSTPONED : 
				this.postponed = true;
				break;
		default:
			break;
		}
	}
	
	private void handleRequests() throws RemoteException{
		Message request = null;
		if(null != (request = this.requestBacklog.peek())) {	
			if(hasGrantedProcess){
				if(!isGrantedRequest(request)){
					log("COULD inquire");
					if (!this.inquiring) {
						log("inquiring...");
						this.inquiring = true;
						sendMsg(MessageType.INQUIRE, this.grantData.pid);
					}
				}
			} else {
				Grant(request);
			}
		}
	}

	
	private boolean isGrantedRequest(Message m){
		return (m.getTimeStamp().compareTo(grantData.ts) == 0);
	}
	
	private void Grant(Message request) throws RemoteException{
		int id = request.getPID();
		sendMsg(MessageType.GRANT, id);
		this.grantData.pid = id;
		this.grantData.ts = request.getTimeStamp();
		this.hasGrantedProcess = true;
	}
	
	private void Release(){
		log("Releasing...");
		this.hasGrantedProcess = false;
		this.inquiring = false;		
		this.requestBacklog.Remove(new MessageImpl(MessageType.REQUEST, this.grantData.ts, this.grantData.pid));
	}
	
	private Peer findPeer(int id) {
		Peer p = null;
		for (PeerEntry pe : this.peers) {
			if(pe.peerId == id) {
				p = pe.p;
			}
		}
		
		if(p == null) {
			throw new RuntimeException("Peer ID not known");
		}
		
		return p;
	}
	
	private void sendMsg(MessageType t, int peerId){
		
		this.ts = this.ts.inc();
		log("Sending MessageType " + t.toString() + " to Peer "+ peerId + " at " + this.ts.toString());
		try {
			findPeer(peerId).putMessage(new MessageImpl(t, this.ts, this.pid));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void activate(List<PeerEntry> peers, Integer ownId)
			throws RemoteException {
		this.peers = peers;
		this.inCriticalState = false;
		this.hasGrantedProcess = false;
		this.inquiring = false;
		this.inquired = false;
		this.postponed = false;
		this.pid = ownId;
		this.ts = new TimeStamp(0, this.pid);
		this.requestSet = new HashMap<Integer, Link>();
		this.requestBacklog = new MessageDeliveryQueue("requestBacklog");
		this.queue= new MessageDeliveryQueue("queue");
		this.inquirers = new ArrayList<Integer>();
		this.grantData = new GrantData();
		this.noGrants = 0;
		this.outstandingRequest = false;
		this.setupLinks();
	}
	
	private void updateMessages(){
		log("updateMessages");
		for (Link l: this.requestSet.values()){
			Message m;
			while(null != (m = l.popMessage())){
				this.queue.insert(m);
			}
		}
	}
	
	private void startRequesting() throws InterruptedException{
		// call broadcast
		log("startRequesting");
		this.outstandingRequest = true;
		Thread.sleep(new Random().nextInt(1500));

		this.BroadCastMsg(new MessageImpl(MessageType.REQUEST, this.ts, this.pid));
		
	}
	
	private boolean chance(int frac){
		for(int i=0; i<frac; i++){
			if(!getRandBoolean()) return false;
		}
		return true;
	}
	
	public void mainLoop(int num_rounds, int num_requests) throws InterruptedException, RemoteException{
		while(num_rounds-- > 0){
			Thread.sleep(new Random().nextInt(100));

			log("Round " + num_rounds);
			if(!outstandingRequest && chance(3) && num_requests-- > 0){
				startRequesting();
			}
			if(inquired){
				// do inquire receive msg body
				log("handling inquired, whole state: " + this.toString());
				if(this.postponed || (this.noGrants == this.requestSet.size())){
					log("either postponed or noGrants is sufficient");
					if(this.postponed) {
						for(Integer i : this.inquirers) {
							sendMsg(MessageType.RELINQUISH, i);
							this.noGrants -= 1;
						}
						
						this.inquired = false;
						this.inquirers.clear();
					}
				}
				
			} else {
				if(inCriticalState) {
					// do random amount of work
					log("In critical section");
					Thread.sleep(new Random().nextInt(500));
					log("Done critical section");
					this.inCriticalState = false;
					this.inquired = false;
					this.outstandingRequest = false;
					// Release bcast
					this.BroadCastMsg(new MessageImpl(MessageType.RELEASE, this.ts, this.pid));
				}
			} 
			updateMessages();
			Message m = null;
			while (null != (m = this.queue.pop())){
				this.ReceiveMsg(m);
			}
			handleRequests();
		}
	}
	
	private void log(String s){
		System.out.println("[PID: " + this.pid + "] " + s);
	}
	
	public static void main(String[] args) throws InterruptedException, MalformedURLException, RemoteException, NotBoundException{
		// setup registry
		Server server = (Server) java.rmi.Naming.lookup("rmi://localhost:1089/register");				
		ClientImpl c = new ClientImpl();

		if(args.length < 2){
			throw new RuntimeException("You need to supply both CS_entries and numRounds as arguments");
		}
		
		int CS_entries = Integer.valueOf(args[0]);
		int rounds = Integer.valueOf(args[1]);


		
		server.register(c);
		Thread.sleep(10000);
		
		c.mainLoop(rounds, CS_entries);
//		int waitRounds = 20;
//		while(!c.loopFlag && waitRounds > 0){
//			Thread.sleep(500);
//			waitRounds--;
//		}
//		Thread.sleep(2000); // settling time for other processes
//		c.mainLoop();
	}
	
	private class GrantData {
		public int pid;
		public TimeStamp ts;
	}
}
