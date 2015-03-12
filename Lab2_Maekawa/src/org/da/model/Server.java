package org.da.model;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Server extends Remote {
	public void register(Client c) throws RemoteException;
}
