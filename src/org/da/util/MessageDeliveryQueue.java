package org.da.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.da.impl.TimeStamp;
import org.da.model.Ack;
import org.da.model.Message;

// Not thread safe!
public class MessageDeliveryQueue{
	private final int num_acks; 	
	private final List<MessageQueueEntry> backingList;
	
	
	public MessageDeliveryQueue(int peersAmt){
		this.num_acks = peersAmt;
		 backingList = new ArrayList<MessageQueueEntry>();
	}
	
	public void insert(Message m){
		// pre: m not in backingList
		int size = backingList.size();
		TimeStamp newMessage = m.getTimeStamp();
		TimeStamp placeholder;
		int i=0;
		for(; i<size; i++){
			placeholder = backingList.get(i).m.getTimeStamp();
			if(newMessage.compareTo(placeholder) == -1){
				break;
			} else if (newMessage.compareTo(placeholder) == 0){
				throw new RuntimeException("Double message insertion");
			}
		}
		backingList.add(i, new MessageQueueEntry(m));
		System.out.println("Added to queue: " + m.toString() + "(pos = " + i + ")");
	}
	
	public void acknowledge(Ack a){
		// pre: m that belongs to a in backingList
		TimeStamp ta = a.getTimeStamp();
		TimeStamp ma;
		for(MessageQueueEntry mqe : backingList){
			ma = mqe.m.getTimeStamp();
			if(ma.compareTo(ta) == 0){
				mqe.acks.add(a);
				return;
			}
		}
		throw new RuntimeException("Received ACK before corresponding Message is on the list: " + a.toString());
	}
	
	// only pops head of queue if received_acks == num_acks
	public Message pop(){
		Message retval = null;
		if(backingList.size() > 0){
			MessageQueueEntry mqe = backingList.get(0);
			if(mqe.acks.size() == num_acks){
				retval = mqe.m;
				backingList.remove(0);
			}
		}
		return retval;
	}
	
	public Message peekNotAcked(){
		Message retval = null;
		if(backingList.size() > 0){
			MessageQueueEntry mqe = backingList.get(0);
			if(!mqe.acked){
				retval = mqe.m;				
			}
		}
		return retval;
	}
	
	public void setAckedTop(){
		if(backingList.size() > 0){
			MessageQueueEntry mqe = backingList.get(0);
			mqe.acked = true;
		}
	}
	
	class MessageQueueEntry {
		public Message m;
		public List<Ack> acks;
		public boolean acked;
		
		public MessageQueueEntry(Message m){
			this.m = m;
			acks = new ArrayList<Ack>(num_acks);
			acked = false;
		}
	}
}


