package model;

import listeners.AckListener;
import listeners.MessageListener;


public interface LocalProcess {
	//public void addPeersChangedListener(PeersChangedListener p);
	public void addMessageListener(MessageListener m);
	public void addAckListener(AckListener a);	
}
