package org.activequant.reporting;

import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.activequant.core.types.TimeStamp;

/**
 * The CsvFile value reporter can keep track of a list of key/value pairs,
 * which must be timed. Upon flush it will write the values to disc. 
 * <br>
 * Example:<br> 
 * <br>
 * 	CsvFileValueReporter csvVals = new CsvFileValueReporter(fileName);<br>
 *  csvVals.report(timeStamp, "key", 1.0);<br>
 * @author Ulrich Staudinger
 * 
 */
public class CsvFileValueReporter implements IValueReporter {

	private List<String> keyList = new ArrayList<String>();
	private TreeMap<TimeStamp, HashMap<String, Double>> timedValues = new TreeMap<TimeStamp, HashMap<String, Double>>();
	private String fileName;	
	DecimalFormat df = new DecimalFormat("#.########");

	public CsvFileValueReporter(String fileName) {
		this.fileName = fileName;
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');		
		df.setDecimalFormatSymbols(dfs);
	}

	private HashMap<String, Double> getHashMap(TimeStamp timeStamp) {
		if (!timedValues.containsKey(timeStamp))
			timedValues.put(timeStamp, new HashMap<String, Double>());
		return timedValues.get(timeStamp);
	}

	@Override
	public void report(TimeStamp timeStamp, String valueKey, Double value) {
		// check if the key exists.
		if (!keyList.contains(valueKey))
			keyList.add(valueKey);

		// 
		HashMap<String, Double> map = getHashMap(timeStamp);
		map.put(valueKey, value);
	}

	public void flush() {
		try {
			FileWriter fw = new FileWriter(fileName);
			Iterator<TimeStamp> timeStampIterator = timedValues.keySet()
					.iterator();
			fw.write("TimeStamp;");
			for(String key : keyList)
			{
				fw.write(key+";");
			}
			fw.write("\n");
			while (timeStampIterator.hasNext()) {
				TimeStamp timeStamp = timeStampIterator.next();
				fw.write(timeStamp.getNanoseconds()+";");
				
				HashMap<String, Double> map = getHashMap(timeStamp);
				for(String key : keyList)
				{
					Double val = map.get(key);
					if(val==null)
						fw.write(";");					
					else
						fw.write(df.format(val)+";");
				}
				fw.write("\n");
			}
			fw.flush();
			fw.close();
		} catch (Exception anEx) {
			throw new RuntimeException(anEx);
		}
	}

}
