package model;

import java.io.Serializable;

import impl.Clock;

public class Ack implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Clock ackStamp;
	public Clock messageStamp;
	
	public <T> Ack(Message<T> m, Clock current_time){
		this.ackStamp = current_time;
		this.messageStamp = m.getTimestamp();
	}
}
