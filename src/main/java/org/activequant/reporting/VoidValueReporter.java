package org.activequant.reporting;

import org.activequant.core.types.TimeStamp;


/**
 * The void value reporter does no reporting at all. 
 * 
 * @author Ulrich Staudinger
 *
 */
public class VoidValueReporter implements IValueReporter {

	@Override
	public void report(TimeStamp timeStamp, String valueKey, Double value) {
		// doing nothing. 		
	}
	
	public void flush()
	{
		// doing nothing. 
	}

}
