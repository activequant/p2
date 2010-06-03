package org.activequant.statprocessors.valueseries;

import org.activequant.container.report.SimpleReport;
import org.activequant.core.domainmodel.data.TimedValue;
import org.activequant.core.domainmodel.data.ValueSeries;
import org.activequant.core.types.TimeStamp;
import org.activequant.statprocessors.ValueSeriesProcessor;

/**
 * Min Value calculating processor 
 * 
 * 
 * Spring instantiation code: <br><pre>
 * <!CDATA[[
 * <bean id="minValueProcessor" class="org.activequant.statprocessors.valueseries.MinValue"> 
 * 	<constructor-arg type="String"><value>MinPnl</value></constructor-arg>
 * </bean> 
 * ]]>
 * </pre>
 * 
 * @author Ghost Rider
 *
 */
public class MinValue implements ValueSeriesProcessor {

	private String valueKey; 
	
	public MinValue(String valueKey)
	{
		this.valueKey = valueKey; 
	}
	
	@Override
	public void process(ValueSeries input, SimpleReport output) {
		double minValue = Double.MAX_VALUE;
		TimeStamp minTimeStamp = null; 
		for(TimedValue value : input)
		{
			if(value.getValue()<minValue)
			{
				minValue = value.getValue();
				minTimeStamp = value.getTimeStamp();
			}
		}
		
		// 
		if(minTimeStamp!=null)
		{
			output.getReportValues().put(valueKey+"/Value", minValue);
			output.getReportValues().put(valueKey+"/TimeStamp", minTimeStamp.getDate());
		}
	}

}
