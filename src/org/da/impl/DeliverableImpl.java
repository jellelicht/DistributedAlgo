package org.da.impl;

import org.da.model.Ack;
import org.da.model.Deliverable;
import org.da.model.Message;

public class DeliverableImpl implements Deliverable {

	private final Message m;
	private final Ack a;
	
	public DeliverableImpl(Ack a) {
		this.m = null;
		this.a = a;
	}
	
	public DeliverableImpl(Message m) {
		this.m = m;
		this.a = null;
	}
	
	@Override
	public boolean isAck() {
		return a != null;
	}

	@Override
	public boolean isMessage() {
		return m != null;
	}

	@Override
	public Message getMessage() {
		return m;
	}

	@Override
	public Ack getAck() {
		return a;
	}

}
