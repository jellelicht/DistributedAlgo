package org.da.impl;

import org.da.model.Message;

public class MessageImpl implements Message{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TimeStamp stamp;
	
	public MessageImpl(TimeStamp stamp){
		this.stamp = stamp;
	}
	
	@Override
	public TimeStamp getTimeStamp() {
		// TODO Auto-generated method stub
		return stamp;
	}
	
	@Override
	public String toString(){
		return "Message: " + stamp.toString();
	}

}
