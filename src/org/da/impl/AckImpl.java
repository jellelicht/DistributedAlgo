package org.da.impl;

import org.da.model.Ack;

public class AckImpl implements Ack {

	private static final long serialVersionUID = 1L;
	private TimeStamp stamp;
		
	public AckImpl(TimeStamp stamp){
		this.stamp = stamp;
	}
	
	@Override
	public TimeStamp getTimeStamp() {
		// TODO Auto-generated method stub
		return stamp;
	}
	
	@Override
	public String toString(){
		return "Ack: " + stamp.toString();
	}
}
