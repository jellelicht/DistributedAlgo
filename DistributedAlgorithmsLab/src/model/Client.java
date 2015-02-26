package model;

import java.rmi.RemoteException;
import java.util.ArrayList;

public interface Client<T> extends Peer<T>, LocalProcess<T>, java.rmi.Remote {

	void activate(ArrayList<Client> clients, int i) throws RemoteException;

}
