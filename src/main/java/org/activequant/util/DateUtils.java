package org.activequant.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class DateUtils {
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

	public static List<Integer> createDateRange(Integer startDate, Integer endDate) throws ParseException{		
		Date sd = sdf.parse(startDate.toString());
		Date ed = sdf.parse(endDate.toString());
		
		Calendar sdc = GregorianCalendar.getInstance();
		sdc.setTime(sd);
		
		Calendar edc = GregorianCalendar.getInstance();
		edc.setTime(ed);
		
		List<Integer> ret = new ArrayList<Integer>();
		while(sdc.before(edc) | sdc.equals(edc)){
			ret.add(Integer.parseInt(sdf.format(sdc.getTime())));
			sdc.add(Calendar.DATE, 1);
		}
		return ret; 
		
	}
}
