package org.activequant.statprocessors.valueseries;

import java.io.File;

import javax.imageio.ImageIO;

import org.activequant.container.report.SimpleReport;
import org.activequant.core.domainmodel.data.TimedValue;
import org.activequant.core.domainmodel.data.ValueSeries;
import org.activequant.statprocessors.ValueSeriesProcessor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 * Pnl Chart generating processor 
 * 
 * Spring instantiation code: <br><pre>
 * <!CDATA[[
 * <bean id="pnlChartGenerator" class="org.activequant.statprocessors.valueseries.PnlChartGenerator"> 
 * 	<constructor-arg type="String"><value>/var/www/html</value></constructor-arg>
 *  <constructor-arg type="int"><value>600</value></constructor-arg>
 *  <constructor-arg type="int"><value>400</value></constructor-arg>
 * </bean> 
 * ]]>
 * </pre>
 *
 * @author Ghost Rider
 *
 */
public class PnlChartGenerator implements ValueSeriesProcessor {

	private String targetFolder; 
	private int width, height; 
	
	/**
	 * main constructor. 
	 * 
	 * @param targetFolder
	 * @param width
	 * @param height
	 */
	public PnlChartGenerator(String targetFolder, int width, int height)
	{
		this.targetFolder = targetFolder;
		this.width = width; 
		this.height = height;
	}

	@Override
	/**
	 * Processor method. 
	 */
	public void process(ValueSeries input, SimpleReport output) throws Exception {
		
		// 
        TimeSeries cumulatedSeries = generateXYSeries("Cumulated PNL", input, true);
        TimeSeries uncumulatedSeries = generateXYSeries("Uncumulated PNL", input, false);
        TimeSeriesCollection collection = new TimeSeriesCollection();        
        collection.addSeries(cumulatedSeries);
        collection.addSeries(uncumulatedSeries);
        JFreeChart chart = ChartFactory.createTimeSeriesChart("PnL","Date", "PnL", collection, true, true, false);
        
        // render the chart.
        long chartId = System.currentTimeMillis();        
        File targetFile = new File(targetFolder, chartId+".png");
        ImageIO.write(chart.createBufferedImage(width, height), "PNG", targetFile);
        
        // set the target file name in the report output. 
        output.getReportValues().put("PnlChartName", chartId+".png");       
	}

	/**
	 * support method to generate an xy series. 
	 * 
	 * @param aSeriesLabel
	 * @param valueSeries
	 * @param cumulated
	 * @return
	 * @throws Exception
	 */
    private static TimeSeries
    generateXYSeries(String aSeriesLabel, ValueSeries valueSeries, boolean cumulated) throws Exception
    {
    	double lastValue = 0.0; 
        TimeSeries pnlSeries = new TimeSeries(aSeriesLabel, Minute.class);
        if (valueSeries.size() > 0) {        
        	int modulator = 1; 
        	if(valueSeries.size()>500)
        		modulator = 10; 
        	if(valueSeries.size()>5000)
        		modulator = 100;         	
			Minute minuteStart = new Minute(valueSeries.get(0).getTimeStamp().getDate());
			pnlSeries.addOrUpdate(minuteStart, valueSeries.get(0).getValue());
			for (int i = 1; i < valueSeries.size(); i++) {
				if (i % modulator == 0) {
					TimedValue value = valueSeries.get(i);
					if (cumulated) {
						Minute minute = new Minute(value.getTimeStamp().getDate());
						pnlSeries.addOrUpdate(minute, value.getValue());
					} else {
						pnlSeries.addOrUpdate(new Minute(value.getTimeStamp().getDate()), value.getValue() - lastValue);
						lastValue = value.getValue();
					}
					lastValue = value.getValue();
				}
			}
			
		}
        return pnlSeries;
    }

}
