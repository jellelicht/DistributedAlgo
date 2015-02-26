package impl;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import listeners.AckListener;
import listeners.MessageDeliveredListener;
import model.Ack;
import model.Client;
import model.Message;
import model.MessageQueue;
import model.Peer;
import model.RegisterService;

public class ClientImpl implements Client<String>, AckListener, MessageDeliveredListener {
	private ArrayList<Peer<String>> peers;
	private int pid;
	private ArrayList<AckListener> ackListeners;
	private ArrayList<MessageDeliveredListener> messageDeliveredListeners;
	private MessageQueue<String> msgQueue;
	private Clock clock;
	
	public ClientImpl(){
		ackListeners = new ArrayList<AckListener>();
		messageDeliveredListeners = new ArrayList<MessageDeliveredListener>();
	}

	public void activate(ArrayList<ClientImpl> peers, int i) {
		for (ClientImpl c : peers){
			this.peers.add(c);
		}
		
		this.msgQueue = new MessageQueue<String>(this.peers);
		this.pid = i;
		this.clock = new Clock(pid);
	}
	
	public void addMsg(Message<String> m){
		this.msgQueue.insert(m);
	}
	
	public void Broadcast(Message<String> m) throws RemoteException{
		for(Peer<String> p : peers){
			p.Peer_putMessage(m);
		}
	}
	
	public Message<String> GenerateMsg(){
		return new Message<String>("TEST MESSAGE", this.getClock());
		
	}

	@Override
	public void Peer_putMessage(Message<String> m) throws RemoteException {
		this.msgQueue.insert(m);
		//this.clock = this.clock.inc();
		
		for(Peer<String> p : peers){
			// TODO something clock
			// TODO something update clock in message?
			p.Peer_ackMessage(new Ack(m, this.clock));
		}	
	}	
	
	//someone ack'd a message
	@Override
	public void Peer_ackMessage(Ack a) throws RemoteException {
		this.msgQueue.handleAck(a);
		Message<String> m = this.msgQueue.checkDelivery();
		
		if(m != null){
			this.deliverMessage(m);
		}
		
//		for(AckListener al: ackListeners){
//			al.receiveAck(a);
//		}
	}
	
	@Override
	public Clock getClock() {
		return this.clock;
	}

	@Override
	public void addMessageDeliveredListener(MessageDeliveredListener mdl) {
		// TODO Auto-generated method stub
		messageDeliveredListeners.add(mdl);		
	}

	@Override
	public void addAckListener(AckListener al) {
		// TODO Auto-generated method stub
		ackListeners.add(al);
		
	}

	@Override
	public void deliverMessage(Message<String> m) {
		this.clock = this.clock.sync(m.getTimestamp());
		System.out.println("Message recieved " + m.toString());
	}
	
	// DeliveredMessageListener
	@Override
	public void deliveredMessage(Message m) {
		System.out.println("something got delivered in my inbox");
	}

	//Ack Listener
	@Override
	public void receiveAck(Ack a) {
		System.out.println("someone acked something to me");
	}
	
	public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException{
		
		RegisterService r = (RegisterService) java.rmi.Naming.lookup("rmi://localhost:1099/register");
		ClientImpl _instance = new ClientImpl();
		
		r.register(_instance);
		
	}

}
