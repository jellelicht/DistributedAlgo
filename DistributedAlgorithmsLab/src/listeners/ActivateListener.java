package listeners;

import model.Peer;

public interface ActivateListener extends java.util.EventListener {	
	public void activate(Peer[] peers);
}
