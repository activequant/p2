package org.activequant.statprocessors;

import java.util.List;

import org.activequant.container.report.SimpleReport;
import org.activequant.core.domainmodel.data.ValueSeries;
import org.apache.log4j.Logger;

/**
 * 
 * Spring instantiation code: <br><pre>
 * <!CDATA[[
 * <bean id="pnlChartGenerator" class="org.activequant.statprocessors.StatisticsGenerator"> 
 * 	<constructor-arg>
 * 		<list>
 *			<ref bean="some processor1"/>
 *   		<ref bean="maxValueProcessor"/>
 *   		<ref bean="minValueProcessor"/>
 *		</list>
 * </constructor-arg>
 * </bean> 
 * ]]>
 * </pre>
 * 
 * 
 * @author Ghost Rider
 *
 */

public class StatisticsGenerator {
	
	private List<ValueSeriesProcessor> valueSeriesProcessors; 	
	
	public StatisticsGenerator(List<ValueSeriesProcessor> valueSeriesProcessors)
	{
		this.valueSeriesProcessors = valueSeriesProcessors; 
	}

	/**
	 * processes input objects
	 * 
	 * @param report
	 * @param timeSeries
	 */
	public void process(ValueSeries input, SimpleReport report)
	{
		for(ValueSeriesProcessor c : valueSeriesProcessors)
		{
			try{
				c.process(input, report);
			}
			catch(Exception anEx)
			{
				log.error("Error while processing value series", anEx);
			}
		}
			
	}
	
	private static Logger log = Logger.getLogger(StatisticsGenerator.class);
	
	
}
