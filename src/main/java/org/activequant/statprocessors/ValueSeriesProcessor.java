package org.activequant.statprocessors;

import org.activequant.container.report.SimpleReport;
import org.activequant.core.domainmodel.data.ValueSeries;

public interface ValueSeriesProcessor {
	
	/**
	 * Processes input series and fills in data into output statistics. 
	 * 
	 * @param input
	 * @param output
	 */
	public void process(ValueSeries input, SimpleReport output) throws Exception;  
	
}
