package org.da.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.da.model.Deliverable;
import org.da.model.Link;

public class LinkImpl implements Link{
	private List<Deliverable> syncedBackingList;
	
	public LinkImpl(){
		this.syncedBackingList = Collections.synchronizedList(new ArrayList<Deliverable>());
	}
	
	@Override
	public void addDeliverable(Deliverable d) {
		this.syncedBackingList.add(d);
		
	}

	// Can return null
	@Override
	public Deliverable popDeliverable() {
		Deliverable retval = null;
		synchronized (syncedBackingList){
			if(!syncedBackingList.isEmpty()){
				retval = syncedBackingList.get(0);
				syncedBackingList.remove(0);
			}
		}
		return retval;
	}

}
