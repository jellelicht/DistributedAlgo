package model;

public interface Peer extends java.rmi.Remote {
	public void msg(Message m) throws  java.rmi.RemoteException;
	public void ack(Message m) throws java.rmi.RemoteException;
}
