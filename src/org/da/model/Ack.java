package org.da.model;

import java.io.Serializable;

import org.da.impl.TimeStamp;

public interface Ack extends Serializable{
	public TimeStamp getTimeStamp();
}
