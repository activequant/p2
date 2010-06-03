package org.activequant.reporting;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import org.activequant.container.report.SimpleReport;
import org.activequant.optimization.domainmodel.BatchConfig;
import org.apache.log4j.Logger;

/**
 * 
 * @author Ghost Rider
 *
 */
public class BatchSimReportWriter {

	private OutputStream outputStream;
	private OutputStream htmlOutputStream;
	private BatchConfig config; 
	private static Logger log = Logger.getLogger(BatchSimReportWriter.class);
	
	public BatchSimReportWriter(BatchConfig config) throws Exception
	{
		FileOutputStream fout = new FileOutputStream(config.getBatchReportTargetFileName());
		this.config = config; 
		this.outputStream = fout;
		this.htmlOutputStream = new FileOutputStream(config.getBatchReportTargetFileName()+".html");
	}
	
	public BatchSimReportWriter(OutputStream outputStream)
	{
		this.outputStream = outputStream;
	}
	
	public void write(List<SimpleReport> reports) throws Exception
	{
		log.info("Writing report to "+config.getBatchReportTargetFileName());
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
		ReportToCsv converter = new ReportToCsv();
		if(reports.size()<0)
			return; 
		String[] header = reports.get(0).getKeys();
		for(String s : header)
			writer.write(s+";");
		writer.write("\n");
		
		for(SimpleReport report : reports)
			writer.write(converter.toCsv(report)+"\n");
		
		writer.flush();
		writer.close();
		
		// doing the html reporting ... 
		writer = new BufferedWriter(new OutputStreamWriter(htmlOutputStream));
		writer.write("<html><body><table border='1'>\n");
		ReportToHtml htmlReporter = new ReportToHtml();
		for(SimpleReport report : reports)
			writer.write(htmlReporter.toHtmlTableRow(report)+"\n");		
		writer.write("</table></body></html>\n");
		writer.flush();
		writer.close();
		
		
	}
	
}
