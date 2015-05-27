package util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import model.Message;


// Not thread safe!
public class MessageDeliveryQueue{
	private final List<Message> backingList;
	
	public MessageDeliveryQueue(){
		backingList = Collections.synchronizedList(new ArrayList<Message>());
	}
	
	public synchronized void insert(Message m){
		backingList.add(m);
		System.out.println("[DELIVERY] Added to queue: " + m.toString());
	}
	
	public synchronized Message pop(){
		Message retval = null;
		if(backingList.size() > 0){
			retval = backingList.remove(0);			
		}
		return retval;
	}
}