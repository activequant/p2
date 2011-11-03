package org.activequant.statprocessors.valueseries;

import java.util.ArrayList;
import java.util.List;

import org.activequant.core.domainmodel.data.TimedValue;
import org.activequant.core.domainmodel.data.ValueSeries;

/**
 * 
 * @author Ghost Rider
 *
 */
public class TimeSlotter {

	private int slotSize; 
	private int slotsPerDay; 
	private int currentDate  = 0; 
	
	/**
	 * main constructor. 
	 * 
	 * @param slotSizeInMilliseconds
	 */
	public TimeSlotter(int slotSizeInMilliseconds)
	{
		this.slotsPerDay = (1000 * 60 * 60 * 24) / slotSizeInMilliseconds;
		this.slotSize = slotSizeInMilliseconds;
	}
	
	/**
	 * 
	 * @return a candle array structured by day / time slot
	 */
	@SuppressWarnings("deprecation")
	public double[][] process(ValueSeries valueSeries)
	{		
		//
		List<double[]> tempList = new ArrayList<double[]>();
		double[] valueArray = null; 
		
		// iterate over the timed values 
		for(TimedValue value : valueSeries)
		{
			// 
			int date = 
				((((value.getTimeStamp().getDate().getYear()+1900) * 10000) 
						+ value.getTimeStamp().getDate().getMonth() * 100) 
						+ value.getTimeStamp().getDate().getDate());			
			int slot = (int)Math.floor(value.getTimeStamp().getNanoseconds() / 1000000.0 / slotSize);
			//
			if(date != currentDate)
			{
				valueArray = new double[slotsPerDay];
				tempList.add(valueArray);
				currentDate = date; 
			}			
			valueArray[slot] = value.getValue();			
		}
		
		// convert the temporary list to a value array. 
		double[][] ret = new double[tempList.size()][slotsPerDay];
		for(int i=0;i<tempList.size();i++)
		{
			ret[i] = tempList.get(i);
		}
		return ret;
		
	}
	
}
