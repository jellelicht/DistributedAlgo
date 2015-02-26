package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;

public class MessageQueue<T>  {

	private ArrayList<Peer<T>> peers;
	public MessageQueue(ArrayList<Peer<T>> peers2){
		this.peers = peers2;
		this.messageQueue = new PriorityQueue<MessageEntry<T>>();
	}
	
	private PriorityQueue<MessageEntry<T>> messageQueue;
	
	public void insert(Message<T> m){
		messageQueue.add(new MessageEntry<T>(peers, m));
	}
	
	public void handleAck(Ack a){
		MessageEntry me = messageQueue.peek();
		if (a.messageStamp.equals(me.m.getTimestamp())){
			me.acks.add(a);
			// Check if all acks for head received?
			return;
		}	
		System.out.println("Received ACK for message not in head of messageQueue. Dropping ACK...");
		
	}
	
	public Message<T> checkDelivery(){
		MessageEntry me = messageQueue.peek();
		
		if(me != null && me.ackedByAll()){
			return messageQueue.poll().m;
		} else {
			return null;
		}
		
	}
	
	private class MessageEntry<T> implements Comparable<MessageEntry<T>> {
		public Message<T> m;
		public ArrayList<Ack> acks;
		private ArrayList<Peer<T>> peers;

		public MessageEntry(ArrayList<Peer<T>> peers2, Message<T> m){
			this.peers = peers2;
			acks = new ArrayList<Ack>();
			this.m = m;
		}
		
		@Override
		public int compareTo(MessageEntry<T> o) {
			return m.compareTo(o.m);
		}
		
		public boolean ackedByAll(){
			// No direct mapping between peer and ack right now
			return peers.size()>acks.size();
		}
	}
	
}
