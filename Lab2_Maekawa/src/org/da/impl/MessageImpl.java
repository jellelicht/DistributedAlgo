package org.da.impl;

import org.da.model.Message;
import org.da.model.MessageType;
import org.da.model.TimeStamp;

public class MessageImpl implements Message {

	private static final long serialVersionUID = 1L;
	private MessageType msgType;
	private TimeStamp ts;
	private Integer pid;
	
	public MessageImpl(MessageType type, TimeStamp stamp, Integer id) {
		this.msgType = type;
		this.ts = stamp;
		this.pid = id;
	}

	@Override
	public MessageType getMessageType() {
		
		return this.msgType;
	}
	
	@Override
	public TimeStamp getTimeStamp() {
		
		return this.ts;
	}
	
	@Override
	public Integer getPID() {
		
		return this.pid;
	}
	
	@Override
	public String toString(){
		return "MessageType: " + this.msgType.toString() + " TS: " + ts.toString();
	}
}
