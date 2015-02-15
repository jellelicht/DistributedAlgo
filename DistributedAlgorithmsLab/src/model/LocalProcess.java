package model;

import impl.Clock;
import listeners.AckListener;
import listeners.MessageDeliveredListener;


public interface LocalProcess<T> {
	//public void addPeersChangedListener(PeersChangedListener p);
	public void deliverMessage(Message<T> m);
	public Clock getClock();
	public void addMessageDeliveredListener(MessageDeliveredListener mdl);
	public void addAckListener(AckListener al);	
}
