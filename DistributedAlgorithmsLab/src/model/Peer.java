package model;

// remote object
// I send them a message
public interface Peer extends java.rmi.Remote {
	public void putMessage(Message m) throws  java.rmi.RemoteException;
	public void ackMessage(Message m) throws java.rmi.RemoteException;
}
