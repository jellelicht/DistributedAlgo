package listeners;

import model.Message;

public interface MessageListener extends java.util.EventListener{
	public void message(Message m);
}
