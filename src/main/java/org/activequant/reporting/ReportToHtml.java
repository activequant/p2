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
public class ReportToHtml {
	public String toHtmlTableRow(SimpleReport report)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<tr>");
		List<String> keys = new ArrayList<String>(report.getReportValues().keySet());
		Collections.sort(keys);
		for(String key : keys)
			if(report.getReportValues().get(key)!=null)
			{
				String value = report.getReportValues().get(key).toString(); 
				if(value.endsWith(".png"))
					value = "<img src='file:///"+value+"'>";
				sb.append("<td valign='top'>").append(value).append("</td>");
			}
			else
				sb.append("<td>N/A</td>");
		
		
		sb.append("</tr>");
		return sb.toString();
	}		
}
