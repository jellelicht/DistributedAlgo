package model;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import util.PeerEntry;

public interface Client extends Remote, Peer {
	public void activate(List<PeerEntry> peers, Integer ownId, Boolean isCand) throws RemoteException;
}
