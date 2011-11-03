package org.activequant.reporting;
import java.io.FileOutputStream;
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
public class ReportToKeyValue {
	public void render(String fileName, SimpleReport report) throws Exception 
	{
		
		FileOutputStream fout = new FileOutputStream(fileName);
		
		StringBuffer sb = new StringBuffer();
		sb.append("<html><body><table border='1'>");
		
		List<String> keys = new ArrayList<String>(report.getReportValues().keySet());
		Collections.sort(keys);
		for(String key : keys)
		{
			sb.append("<tr><td valign='top'>");
			sb.append(key);
			sb.append("</td><td valign='top'>");
			
			if(report.getReportValues().get(key)!=null)
			{
				String val = report.getReportValues().get(key).toString();
				if(val.endsWith(".png"))
					sb.append("<img src='"+val+"'>");
				else 
					sb.append(val);
			}
			sb.append("</td></tr>");		
		}
		
		sb.append("</table></body></html>");
		fout.write(sb.toString().getBytes());
		fout.flush();
		fout.close();
	}		
}
