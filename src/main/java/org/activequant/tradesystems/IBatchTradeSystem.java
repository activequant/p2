package org.activequant.tradesystems;

import org.activequant.container.report.SimpleReport;
import org.activequant.core.domainmodel.data.Quote;
import org.activequant.optimization.domainmodel.AlgoConfig;

public interface IBatchTradeSystem {
	
	/**
	 * 
	 * @param algoEnv
	 * @param algoConfig
	 */
	public boolean initialize(AlgoEnvironment algoEnv, AlgoConfig algoConfig);
	
	/**
	 * Start method. 
	 */
	public void start();
	
	/**
	 * Stop method.
	 */
	public void stop();
	
	/**
	 * trade system uses this method when it wants to add values to 
	 * the simple report. The simple report is for example generated after 
	 * a simulation run. 
	 * 
	 * @param reportObject
	 */
	public void populateReport(SimpleReport reportObject);
	
	/**
	 * Called to announce quotes. 
	 * @param quote
	 */
	public void onQuote(Quote quote);

	/**
	 * Called when the trading time is over. 
	 */
	public void forcedTradingStop();

}
