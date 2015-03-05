package org.da.model;

public interface Deliverable {
	public boolean isAck();
	public boolean isMessage();
	
	public Message getMessage();
	public Ack getAck();
}
