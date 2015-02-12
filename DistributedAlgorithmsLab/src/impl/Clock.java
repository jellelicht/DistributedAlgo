package impl;

public class Clock implements Comparable<Clock>{
	private int tick;
	private int pid;
	
	public Clock(int pid){
		this.tick = 0;
		this.pid = pid;
	}
	
	public void inc(){
		this.tick++;
	}
	
	public void sync(Clock o){
		this.tick = Math.max(this.tick, o.tick);
		this.inc();
	}
	
	@Override
	public int compareTo(Clock o) {
		// TODO Auto-generated method stub
		
		if(this.tick < o.tick) { return -1; }
		else if (this.tick == o.tick && this.pid < o.pid) { return -1; }
		else { return 1; }
	}
	
}
