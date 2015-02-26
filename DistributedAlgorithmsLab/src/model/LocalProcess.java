package model;

import java.rmi.Remote;
import java.rmi.RemoteException;

import impl.Clock;
import listeners.AckListener;
import listeners.MessageDeliveredListener;


public interface LocalProcess<T> extends Remote{
	//public void addPeersChangedListener(PeersChangedListener p);
	public void deliverMessage(Message<T> m) throws RemoteException;
	public Clock getClock() throws RemoteException;
	public void addMessageDeliveredListener(MessageDeliveredListener mdl) throws RemoteException;
	public void addAckListener(AckListener al) throws RemoteException;	
}
