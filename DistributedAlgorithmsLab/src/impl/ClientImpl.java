package impl;

import java.rmi.RemoteException;
import java.util.ArrayList;

import listeners.AckListener;
import listeners.MessageListener;
import model.Client;
import model.Message;
import model.Peer;

public class ClientImpl implements Client {
	private ArrayList<Peer> peers;
	private int pid;

	public void activate(ArrayList<ClientImpl> peers, int i) {
		// TODO Auto-generated method stub
		for (ClientImpl c : peers){
			this.peers.add(c);
		}
		this.pid = i;
	}

	@Override
	public void putMessage(Message m) throws RemoteException {
		for(Peer p : peers){
			// TODO something clock
			// TODO something update clock in message?			
			p.ackMessage(m);
		}
		// TODO Auto-generated method stub		
	}
	
	@Override
	public void ackMessage(Message m) throws RemoteException {
		// TODO Auto-generated method stub		
	}
	
	public static void main(String[] args){
		
	}

}
