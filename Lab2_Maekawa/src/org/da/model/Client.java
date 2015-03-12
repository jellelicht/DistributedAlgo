package org.da.model;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import org.da.util.PeerEntry;

public interface Client extends Remote, Peer {
	public void activate(List<PeerEntry> peers, Integer ownId) throws RemoteException;
}