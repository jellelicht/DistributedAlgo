package org.da.model;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Peer extends Remote, Serializable{
	public void putMessage(Message m) throws RemoteException;
	public void putAck(Ack a) throws RemoteException;
}