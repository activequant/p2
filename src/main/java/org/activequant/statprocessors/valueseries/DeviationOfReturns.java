package org.activequant.statprocessors.valueseries;

import org.activequant.container.report.SimpleReport;
import org.activequant.core.domainmodel.data.ValueSeries;
import org.activequant.statprocessors.ValueSeriesProcessor;

/**
 * Deviation of Returns processor 
 * 
 * 
 * Spring instantiation code: <br><pre>
 * <!CDATA[[
 * <bean id="deviationOfReturnsProcessor" class="org.activequant.statprocessors.valueseries.DeviationOfReturns"> 
 * 	<constructor-arg type="String"><value>MinPnl</value></constructor-arg>
 * </bean> 
 * ]]>
 * </pre>
 * 
 * @author Ghost Rider
 *
 */
public class DeviationOfReturns implements ValueSeriesProcessor {

	@SuppressWarnings("unused")
	private String valueKey; 
	
	public DeviationOfReturns(String valueKey)
	{
		this.valueKey = valueKey; 
	}
	
	@Override
	public void process(ValueSeries input, SimpleReport output) {
		// To be done.  
	}

}
