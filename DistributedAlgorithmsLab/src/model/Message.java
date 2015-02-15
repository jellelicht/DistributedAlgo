package model;

import impl.Clock;

public class Message<T> implements Comparable<Message<T>> {
	private T payload;
	private Clock timestamp;
	
	public Message(T payload, Clock timestamp){
		this.payload = payload;
		this.timestamp = timestamp;
	}
	
	public Clock getTimestamp(){
		return timestamp;
	}

	@Override
	public int compareTo(Message<T> o) {
		return this.timestamp.compareTo(o.timestamp);
	}
}
