package impl;

import model.Message;
import model.MessageType;

public class MessageImpl implements Message, Comparable<Message> {
	
	private static final long serialVersionUID = 1L;
	private MessageType msgType;
	private int level;
	private int pid;
	private int originId;
	
	public MessageImpl(MessageType msgType, int level, int pid, int originId) {
		this.msgType = msgType;
		this.level = level;
		this.pid = pid;
		this.originId = originId;
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
	public Integer getOriginId() {
		return this.originId;
	}
	
	@Override
	public MessageType getMessageType() {
		return this.msgType;
	}

	@Override
	public int compareTo(Message o) {
		if (this.pid < o.getPId()){
			return -1;
		} else if (this.pid > o.getPId()) {
			return 1;
		} else {
			return 0;
		}
	}
	
	@Override
	public String toString(){
		return "MessageImpl["+msgType.name()+"]: (Origin: " + originId + ", pid: " + pid + ", level: " + level + ")";
	}
}
