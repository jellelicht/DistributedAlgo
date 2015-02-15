package model;

// remote object
// I send them a message
// I send them an ack
public interface Peer<T> extends java.rmi.Remote {
	public void putMessage(Message<T> m) throws  java.rmi.RemoteException;
	public void ackMessage(Ack a) throws java.rmi.RemoteException;
}
