package impl;

import model.Message;

public class MessageImpl extends Message<String>{

	public MessageImpl(String payload, Clock timestamp) {
		super(payload, timestamp);
	}

}
