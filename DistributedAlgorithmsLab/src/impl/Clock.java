package impl;

public class Clock implements Comparable<Clock>{
	private int tick;
	private int pid;
	
	public Clock(int pid){
		this.tick = 0;
		this.pid = pid;
	}
	
	
	public Clock inc(){
		return new Clock(this.tick+1);
	}
	
	public Clock sync(Clock o){
		tick = Math.max(this.tick, o.tick);
		return new Clock(tick+1);
	}
	
	@Override
	public int compareTo(Clock o) {
		if(this.tick < o.tick) { return -1; }
		else if (this.tick == o.tick && this.pid < o.pid) { return -1; }
		else { return 1; }
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + pid;
		result = prime * result + tick;
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Clock other = (Clock) obj;
		if (pid != other.pid)
			return false;
		if (tick != other.tick)
			return false;
		return true;
	}
	
	
	
}
