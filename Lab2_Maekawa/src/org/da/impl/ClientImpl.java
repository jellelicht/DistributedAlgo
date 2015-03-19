package org.da.impl;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	protected ClientImpl() throws RemoteException {
		super();
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
		this.inCriticalState = true;
	}
	
	@Override
	public void putMessage(Message msg) throws RemoteException{
		int id = msg.getPID();
		Link l = this.requestSet.get(id);
		l.addMessage(msg);
	}
	
	// msg should be either REQUEST OR RELEASE
	public void BroadCastMsg(Message msg) {
		this.ts = this.ts.inc();
		for (PeerEntry pe : this.peers) {
			try {
				pe.p.putMessage(msg);
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
		switch(msg.getMessageType()) {
			case REQUEST :
				this.requestBacklog.insert(msg);
				if(this.hasGrantedProcess) {
					Message request = this.requestBacklog.peek();
					if(msg != request){
						sendMsg(MessageType.POSTPONED, msg.getPID());
					} else  {
						
					}
				}	
				break;
			case GRANT : 
				this.noGrants += 1;
				if(this.noGrants == this.requestSet.size()) {
					this.postponed = false;
					this.inCriticalState = true;
					for (PeerEntry pe : this.peers) {
						try {
							pe.p.putMessage(new MessageImpl(MessageType.RELEASE, this.ts, this.pid));
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				break;
			case INQUIRE :
				this.inquired = true;
				this.inquirers.add(msg.getPID());
				break;
			case RELINQUISH : 
				this.inquiring = false;
				this.hasGrantedProcess = false;
				this.requestBacklog.insert(new MessageImpl(MessageType.REQUEST, this.grantData.ts, this.grantData.pid));
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
					if (!this.inquiring) {
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
		return m.getMessageType() == MessageType.REQUEST &&
				(m.getTimeStamp().compareTo(grantData.ts) == 0);
	}
	
	private void Grant(Message request) throws RemoteException{
		int id = request.getPID();
		Peer p = findPeer(id);
		p.putMessage(new MessageImpl(MessageType.GRANT, this.ts, this.pid));
		this.grantData.pid = request.getPID();
		this.grantData.ts = request.getTimeStamp();
		this.hasGrantedProcess = true;
	}
	
	private void Release(){
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
		this.requestBacklog = new MessageDeliveryQueue();
		this.queue= new MessageDeliveryQueue();
		this.inquirers = new ArrayList<Integer>();
		this.grantData = null;
		this.noGrants = 0;
	}
	
	private void updateMessages(){
		for (Link l: this.requestSet.values()){
			Message m;
			while(null != (m = l.popMessage())){
				this.queue.insert(m);
			}
		}
	}
	
	private void startRequesting(){
		// call broadcast
		this.BroadCastMsg(new MessageImpl(MessageType.REQUEST, this.ts, this.pid));
		
	}
	
	public void mainLoop(int num_rounds) throws InterruptedException, RemoteException{
		while(num_rounds-- > 0){
			if(inquired){
				// do inquire receive msg body
				if(this.postponed || (this.noGrants == this.requestSet.size())){
					if(this.postponed) {
						this.noGrants -= 1;
						for(Integer i : this.inquirers) {
							Peer p = this.findPeer(i);
							p.putMessage(new MessageImpl(MessageType.RELINQUISH, this.ts, this.pid));
						}
					}
				}
				
			} else {
				if(inCriticalState) {
					// do random amount of work
					Thread.sleep(100);
					this.inCriticalState = false;
					// Release bcast
					this.BroadCastMsg(new MessageImpl(MessageType.RELEASE, this.ts, this.pid));
				} else {
					updateMessages();
					Message m = null;
					while (null != (m = this.queue.pop())){
						this.ReceiveMsg(m);
					}
				}
			} 
		}
	}
	
	public static void main(String[] args) throws InterruptedException, MalformedURLException, RemoteException, NotBoundException{
		// setup registry
		Server server = (Server) java.rmi.Naming.lookup("rmi://localhost:1089/register");				
		ClientImpl c = new ClientImpl();
		
		server.register(c);
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
