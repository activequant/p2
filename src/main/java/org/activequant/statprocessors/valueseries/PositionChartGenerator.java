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
 * Spring instantiation code: <br>
 * 
 * <pre>
 * &lt;!CDATA[[
 * &lt;bean id=&quot;pnlChartGenerator&quot; class=&quot;org.activequant.statprocessors.valueseries.PositionChartGenerator&quot;&gt; 
 * 	&lt;constructor-arg type=&quot;String&quot;&gt;&lt;value&gt;/var/www/html&lt;/value&gt;&lt;/constructor-arg&gt;
 *  &lt;constructor-arg type=&quot;int&quot;&gt;&lt;value&gt;600&lt;/value&gt;&lt;/constructor-arg&gt;
 *  &lt;constructor-arg type=&quot;int&quot;&gt;&lt;value&gt;400&lt;/value&gt;&lt;/constructor-arg&gt;
 * &lt;/bean&gt; 
 * ]]&gt;
 * </pre>
 * 
 * @author Ghost Rider
 * 
 */
public class PositionChartGenerator implements ValueSeriesProcessor {

	private String targetFolder;
	private int width, height;

	/**
	 * main constructor.
	 * 
	 * @param targetFolder
	 * @param width
	 * @param height
	 */
	public PositionChartGenerator(String targetFolder, int width, int height) {
		this.targetFolder = targetFolder;
		this.width = width;
		this.height = height;
	}

	@Override
	/*
	 * Processor method.
	 */
	public void process(ValueSeries input, SimpleReport output) throws Exception {

		if(input==null)
			return;
		
		// 
		TimeSeries cumulatedSeries = generateXYSeries("Position", input, true);
		TimeSeriesCollection collection = new TimeSeriesCollection();
		collection.addSeries(cumulatedSeries);
		JFreeChart chart = ChartFactory.createTimeSeriesChart("Position", "Date", "Position", collection, true, true, false);

		// render the chart.
		long chartId = System.currentTimeMillis();
		File targetFile = new File(targetFolder, chartId + ".png");
		ImageIO.write(chart.createBufferedImage(width, height), "PNG", targetFile);

		// set the target file name in the report output.
		output.getReportValues().put("PositionChartName", chartId + ".png");
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
	private static TimeSeries generateXYSeries(String aSeriesLabel, ValueSeries valueSeries, boolean cumulated) throws Exception {
		double lastValue = 0.0;
		TimeSeries timeSeries = new TimeSeries(aSeriesLabel, Minute.class);
		if (valueSeries.size() > 0) {
			Minute minuteStart = new Minute(valueSeries.get(0).getTimeStamp().getDate());
			timeSeries.addOrUpdate(minuteStart, valueSeries.get(0).getValue());
			for (int i = 1; i < valueSeries.size(); i++) {
				TimedValue value = valueSeries.get(i);
				if (cumulated) {
					Minute minute = new Minute(value.getTimeStamp().getDate());
					timeSeries.addOrUpdate(minute, value.getValue());
				} else {
					timeSeries.addOrUpdate(new Minute(value.getTimeStamp().getDate()), value.getValue() - lastValue);
					lastValue = value.getValue();
				}

			}
		}
		return timeSeries;
	}

}
