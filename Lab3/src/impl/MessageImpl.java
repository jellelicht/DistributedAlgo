package impl;

import model.Message;

public class MessageImpl implements Message {
	
	private static final long serialVersionUID = 1L;
	private int level;
	private int pid;
	private int ownerid;
	
	public MessageImpl(int level, int pid) {
		this.level = level;
		this.pid = pid;
	}
	
	@Override
	public Integer getLevel() {
		return this.level;
	}
	
	@Override
	public Integer getPId() {
		return this.pid;
	}

	@Override
	public Integer getOwnerID() {
		return this.ownerid;
	}
}
