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
import org.da.util.PeerEntry;

public class ClientImpl extends java.rmi.server.UnicastRemoteObject implements Client {

	private static final long serialVersionUID = 1L;
	private GrantData grantData;
	private List<PeerEntry> peers;
	private Map<Integer, Link> requestSet;
	private List<Message> backlog;
	private Boolean inCriticalState;
	private Boolean inquiring;
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
	
	// Upon receiving a release message from a process
	private void releaseProccess(int pid) {
		
	}
	
	// Enter critical state
	private void enterCriticalState(){
		this.inCriticalState = true;
	}
	
	private boolean isAllowdToEnterCS() {
		return false;
	}
	
	@Override
	public void putMessage(Message msg) throws RemoteException{
		int id = msg.getPID();
		Link l = this.requestSet.get(id);
		l.addMessage(msg);
	}
	
	public void BroadCastMsg() {
		if(!this.hasGrantedProcess) {
			Message msg = new MessageImpl(MessageType.REQUEST, this.ts, this.pid);
			
			for (PeerEntry pe : this.peers) {
				try {
					pe.p.putMessage(msg);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void ReceiveMsg(Message msg) throws RemoteException {
		switch(msg.getMessageType()) {
			case REQUEST : 
				if(!this.hasGrantedProcess) {
					this.ts = this.ts.sync(msg.getTimeStamp());
					this.grantData.pid = msg.getPID();
					this.grantData.ts = msg.getTimeStamp();
					Message currentGrant = new MessageImpl(MessageType.GRANT, ts, this.pid);
					
					Link l = this.requestSet.get(msg.getPID());
					l.addMessage(currentGrant);				
					this.hasGrantedProcess = true;
				} else {
					this.backlog.add(msg);
					Message head = this.backlog.get(0);
					
					if (( this.grantData.ts.compareTo(msg.getTimeStamp()) < 0 ) ||
							head.getTimeStamp().compareTo(msg.getTimeStamp()) < 0) {
						int id = msg.getPID();
						Peer p = findPeer(id);
						
						p.putMessage(new MessageImpl(MessageType.POSTPONED, this.ts, this.pid));
					} else  {
						if (!this.inquiring) {
							this.inquiring = true;
							int id = this.grantData.pid;
							Peer p = findPeer(id);
							p.putMessage(new MessageImpl(MessageType.INQUIRE, this.ts, this.pid));
						}
					}
				}	
				break;
			case GRANT : 
				this.noGrants += 1;
				if(this.noGrants == this.requestSet.size()) {
					
				}
				break;
			case INQUIRE : 
				break;
			case POSTPONED : 
				break;
			case RELEASE : 
				break;
			case RELINQUISH : 
				break;
		default:
			break;
		}
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
	
	@Override
	public void activate(List<PeerEntry> peers, Integer ownId)
			throws RemoteException {
		this.peers = peers;
		this.inCriticalState = false;
		this.hasGrantedProcess = false;
		this.inquiring = false;
		this.postponed = false;
		this.pid = ownId;
		this.ts = new TimeStamp(0, this.pid);
		this.requestSet = new HashMap<Integer, Link>();
		this.backlog = new ArrayList<Message>();
		this.grantData = null;
		this.noGrants = 0;
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
