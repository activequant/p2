package org.activequant.reporting;

import org.activequant.core.types.TimeStamp;

/**
 * Can be used to report a value from a trade system. 
 * 
 * @author GhostRider
 *
 */
public interface IValueReporter {

	public void report(TimeStamp timeStamp, String valueKey, Double value);
	public void flush();
	
}
