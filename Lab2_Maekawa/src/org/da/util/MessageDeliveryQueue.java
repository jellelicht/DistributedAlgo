package org.da.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.da.model.TimeStamp;

import org.da.model.Message;

// Not thread safe!
public class MessageDeliveryQueue{ 	
	private final List<Message> backingList;
	
	
	public MessageDeliveryQueue(){
		 backingList = new ArrayList<Message>();
	}
	
	public void insert(Message m){
		// pre: m not in backingList
		int size = backingList.size();
		TimeStamp newMessage = m.getTimeStamp();
		TimeStamp placeholder;
		int i=0;
		for(; i<size; i++){
			placeholder = backingList.get(i).getTimeStamp();
			if(newMessage.compareTo(placeholder) == -1){
				break;
			} else if (newMessage.compareTo(placeholder) == 0){
				throw new RuntimeException("Double message insertion");
			}
		}
		backingList.add(i, m);
		System.out.println("Added to queue: " + m.toString() + "(pos = " + i + ")");
	}
	
	// only pops head of queue if received_acks == num_acks
	public Message peek(){
		Message retval = null;
		if(backingList.size() > 0){
			retval = backingList.get(0);
		}
		return retval;
	}
	
	public Message pop(){
		Message retval = peek();
		if(retval != null){
			this.backingList.remove(0);
		}
		return retval;
	}
	
	public void Remove(Message request){
		for(Message m: backingList){
			if(request.getTimeStamp().compareTo(m.getTimeStamp()) == 0){
				backingList.remove(m);
			}
		}
	}
}


