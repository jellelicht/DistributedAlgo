package org.da.util;

import java.io.Serializable;

import org.da.model.Peer;

public class PeerEntry implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Peer p;
	public Integer peerId;
	
	public PeerEntry(Integer pid, Peer p){
		this.peerId = pid;
		this.p = p;
	}
}
