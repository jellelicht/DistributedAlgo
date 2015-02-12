package listeners;

import model.Message;

public interface AckListener extends java.util.EventListener {
	public void acknowledge(Message m);
}
