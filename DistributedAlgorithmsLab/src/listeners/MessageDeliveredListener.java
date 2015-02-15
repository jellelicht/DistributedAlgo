package listeners;

import model.Message;

public interface MessageDeliveredListener extends java.util.EventListener{
	public void deliveredMessage(Message m);
}
