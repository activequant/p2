package org.activequant.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.activequant.core.domainmodel.data.Quote;
import org.activequant.core.types.TimeStamp;
import org.activequant.core.types.Tuple;

/**
 * Base class for algo environments. Provides some convenience methods. 
 * 
 * @author Ghost Rider
 *
 */
public class AlgoEnvBase {

	protected List<Tuple<Long, Long>> convertedStartStopTimes;
	protected SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

	protected AlgoEnvBase() {
		super();
	}

	protected void initializeStartStops(List<Tuple<Integer, Integer>> startStopTimes) {
		convertedStartStopTimes = new ArrayList<Tuple<Long, Long>>();
		for(Tuple<Integer, Integer> t : startStopTimes)
		{
			//
			long hours1 = (int)(t.getObject1() / 10000) * ( 60 * 60);
			long minutes1 = (int)((t.getObject1() / 100) % 100) * ( 60);
			long seconds1 = (int)(t.getObject1() % 100);			
			long refTime1Seconds = hours1 + minutes1 + seconds1;			
				
			long hours2 = (int)(t.getObject2() / 10000) * ( 60 * 60);
			long minutes2 = (int)((t.getObject2() / 100) % 100) * ( 60);
			long seconds2 = (int)(t.getObject2() % 100) ;
			long refTime2Seconds = hours2 + minutes2 + seconds2;
			//
			Tuple<Long, Long> tuple = new Tuple<Long, Long>(refTime1Seconds, refTime2Seconds);
			convertedStartStopTimes.add(tuple);
		}
	}

	protected boolean isQuoteWithinStartStopTimes(Quote quote) {
		long secondsQuote = quote.getTimeStamp().getNanoseconds() / 1000000000; 
		long secondsSinceStart = secondsQuote % (60 * 60 * 24);
		for(Tuple<Long, Long> t : convertedStartStopTimes)
		{			
			if(secondsSinceStart>t.getObject1() && secondsSinceStart<t.getObject2())
				return true;			
		}
		return false;
	}

	/**
	 * Create time stamp converts an integer number, i.e. 20090102 or -5 
	 * to the corresponding time stamp. If the number is negative, then a relative
	 * amount of days to now is assumed. 
	 * 
	 * @param dateStamp
	 * @return
	 * @throws Exception
	 */
	protected TimeStamp createTimeStamp(Integer dateStamp) throws Exception {
		TimeStamp ts; 
		if(dateStamp<0)
		{
			// relative time frame in days given. 
			Calendar cal = GregorianCalendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, dateStamp);
			ts = new TimeStamp(cal.getTimeInMillis() * 1000000);
		}
		else
		{
			ts = new TimeStamp(sdf.parse(dateStamp.toString()));
		}
		return ts; 
	}

}