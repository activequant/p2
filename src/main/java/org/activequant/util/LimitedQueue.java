package org.activequant.util;

import java.util.LinkedList;

public class LimitedQueue<E> extends LinkedList<E> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6154581011427320546L;
	private int limit;

    public LimitedQueue(int limit) {
        this.limit = limit;
    }

    public boolean isFull(){
    	if (size() < limit) return false; 
    	return true; 
    }
    
    @Override
    public boolean add(E o) {
        super.add(o);
        while (size() > limit) { super.remove(); }
        return true;
    }
}