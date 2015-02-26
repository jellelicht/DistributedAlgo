package model;

import impl.Clock;

public class Ack {
	public Clock ackStamp;
	public Clock messageStamp;
	
	public <T> Ack(Message<T> m, Clock current_time){
		this.ackStamp = current_time;
		this.messageStamp = m.getTimestamp();
	}
}
