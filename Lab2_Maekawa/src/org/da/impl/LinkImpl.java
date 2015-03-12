package org.da.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.da.model.Link;
import org.da.model.Message;

public class LinkImpl implements Link {

	private List<Message> syncedBackingList;
	
	public LinkImpl(){
		this.syncedBackingList = Collections.synchronizedList(new ArrayList<Message>());
	}
	
	@Override
	public void addMessage(Message d) {
		this.syncedBackingList.add(d);		
	}

	@Override
	public Message popMessage() {
		Message retval = null;
		synchronized (syncedBackingList){
			if(!syncedBackingList.isEmpty()){
				retval = syncedBackingList.get(0);
				syncedBackingList.remove(0);
			}
		}
		return retval;
	}
}
