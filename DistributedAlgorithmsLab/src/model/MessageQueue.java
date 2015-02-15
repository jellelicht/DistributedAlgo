package model;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;

public class MessageQueue  {

	private PriorityQueue messageQueue;
	
	public void insert(Message m){
		messageQueue.add(m);
	}
	
	private class MessageEntry implements Comparable<MessageEntry> {
		public Message m;
		public ArrayList<Ack> acks;

		@Override
		public int compareTo(MessageEntry o) {
			return m.compareTo(o.m);
		}
	}
	
}
