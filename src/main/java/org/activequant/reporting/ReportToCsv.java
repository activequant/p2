package org.activequant.reporting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.activequant.container.report.SimpleReport;


/**
 * Converts a simple report to one CSV string.
 *
 * @author Ghost Rider
 * 
 */
public class ReportToCsv {
	public String toCsv(SimpleReport report)
	{
		StringBuffer sb = new StringBuffer();
		List<String> keys = new ArrayList<String>(report.getReportValues().keySet());
		Collections.sort(keys);
		for(String key : keys)
			if(report.getReportValues().get(key)!=null)
				sb.append(report.getReportValues().get(key).toString()).append(";");
			else
				sb.append("N/A;");
		return sb.toString();
	}		
}
