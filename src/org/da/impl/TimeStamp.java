package org.da.impl;

import java.io.Serializable;

public class TimeStamp implements Comparable<TimeStamp>, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int tick;
	private int pid;
	
	
	public TimeStamp(int tick, int pid){
		this.tick = tick;
		this.pid = pid;
	}
	
	public TimeStamp inc(){
		return new TimeStamp(this.tick+1, this.pid);
	}
	
	public TimeStamp sync(TimeStamp other){
		return new TimeStamp(Math.max(other.tick, this.tick), this.pid).inc();
	}
	
	@Override
	public String toString(){
		return "(" + this.tick + "/"  + this.pid + ")";
	}
	
	@Override
	public int compareTo(TimeStamp o) {
		if (this.tick < o.tick){
			return -1;
		} else if (this.tick == o.tick){
			if(this.pid < o.pid){
				return -1;
			} else if(this.pid > o.pid){
				return 1;
			} else {
				return 0;
			}
		} else {
			return 1;
		}
	}
	
	public int getPid(){
		return this.pid;
	}

}
