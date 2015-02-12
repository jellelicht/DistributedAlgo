package impl;

import java.rmi.RemoteException;
import java.util.ArrayList;

import listeners.AckListener;
import listeners.MessageListener;
import model.Client;
import model.Message;

public class ClientImpl implements Client {

	public void activate(ArrayList<ClientImpl> clients) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void msg(Message m) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ack(Message m) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addMessageListener(MessageListener m) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addAckListener(AckListener a) {
		// TODO Auto-generated method stub
		
	}

}
