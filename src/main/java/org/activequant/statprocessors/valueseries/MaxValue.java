package org.activequant.statprocessors.valueseries;

import org.activequant.container.report.SimpleReport;
import org.activequant.core.domainmodel.data.TimedValue;
import org.activequant.core.domainmodel.data.ValueSeries;
import org.activequant.core.types.TimeStamp;
import org.activequant.statprocessors.ValueSeriesProcessor;

/**
 * Max Value calculating processor 
 * 
 * Spring instantiation code: <br><pre>
 * <!CDATA[[
 * <bean id="maxValueProcessor" class="org.activequant.statprocessors.valueseries.MaxValue"> 
 * 	<constructor-arg type="String"><value>MaxPnl</value></constructor-arg>
 * </bean> 
 * ]]>
 * </pre>
 * 
 * @author Ghost Rider
 *
 */
public class MaxValue implements ValueSeriesProcessor {

	private String valueKey; 
	
	public MaxValue(String valueKey)
	{
		this.valueKey = valueKey; 
	}
	
	@Override
	public void process(ValueSeries input, SimpleReport output) {
		double maxValue = Double.MIN_VALUE;
		TimeStamp maxTimeStamp = null; 
		for(TimedValue value : input)
		{
			if(value.getValue()>maxValue)
			{
				maxValue = value.getValue();
				maxTimeStamp = value.getTimeStamp();
			}
		}
		
		// 
		if(maxTimeStamp!=null)
		{
			output.getReportValues().put(valueKey+"/Value", maxValue);
			output.getReportValues().put(valueKey+"/TimeStamp", maxTimeStamp.getDate());
		}
	}

}
