package model;

import impl.ClientImpl;

import java.rmi.RemoteException;

public interface RegisterService extends java.rmi.Remote {
	public void register(ClientImpl c) throws RemoteException;
}
