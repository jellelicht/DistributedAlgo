package listeners;

import model.Ack;
import model.Message;

public interface AckListener extends java.util.EventListener {
	public void receiveAck(Ack a);
}
