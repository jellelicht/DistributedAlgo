package org.da.model;

import java.io.Serializable;

public interface Message extends Serializable {
	
	public MessageType getMessageType();
	public TimeStamp getTimeStamp();	
	public Integer getPID();

}
