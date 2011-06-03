package org.activequant.backtesting;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.activequant.broker.AccountManagingBrokerProxy;
import org.activequant.broker.IBroker;
import org.activequant.broker.PaperBroker;
import org.activequant.container.report.SimpleReport;
import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.SeriesSpecification;
import org.activequant.core.domainmodel.account.BrokerAccount;
import org.activequant.core.domainmodel.data.Quote;
import org.activequant.core.types.TimeFrame;
import org.activequant.dao.ISpecificationDao;
import org.activequant.data.retrieval.ISeriesDataIteratorSource;
import org.activequant.math.algorithms.MergeSortIterator;
import org.activequant.optimization.domainmodel.SimulationConfig;
import org.activequant.reporting.BrokerAccountToSimpleReport;
import org.activequant.reporting.IValueReporter;
import org.activequant.reporting.PnlLogger3;
import org.activequant.statprocessors.StatisticsGenerator;
import org.activequant.statprocessors.ValueSeriesProcessor;
import org.activequant.statprocessors.valueseries.PnlChartGenerator;
import org.activequant.statprocessors.valueseries.PositionChartGenerator;
import org.activequant.tradesystems.AlgoEnvironment;
import org.activequant.tradesystems.IBatchTradeSystem;
import org.activequant.util.AlgoEnvBase;
import org.activequant.util.SimpleReportInitializer;
import org.activequant.util.VirtualQuoteSubscriptionSource;
import org.activequant.util.tools.TimeMeasurement;
import org.apache.log4j.Logger;

/**
 * 
 * Backtester. 
 * 
 * @author GhostRider
 *
 */
public class SingularBacktester extends AlgoEnvBase {
	
	private ISpecificationDao specDao;
	private IValueReporter valueReporter; 
	private static Logger log = Logger.getLogger(SingularBacktester.class);
	
	
	/**
	 * Dependency Injection constructor.  
	 * 
	 * @param specDao
	 */
	public SingularBacktester(ISpecificationDao specDao, IValueReporter valueReporter)
	{
		this.specDao = specDao; 
		this.valueReporter = valueReporter;
	}
	
	// 
	// 
	@SuppressWarnings("unchecked")
	public SimpleReport simulate(String logTarget, SimulationConfig simConfig, ISeriesDataIteratorSource<Quote> seriesDataIteratorSource) throws Exception
	{
		log.info("Simulation sim config ... ");
				
		//
		initializeStartStops(simConfig.getAlgoEnvConfig().getStartStopTimes());
		
		// Set up broker.  
		BrokerAccount brokerAccount = new BrokerAccount("", ""); 
		
		VirtualQuoteSubscriptionSource quoteSource = new VirtualQuoteSubscriptionSource();
		PaperBroker paperBroker = new PaperBroker(quoteSource);		
		
		String fullLogFile =  logTarget+File.separator+System.currentTimeMillis()+".log";
		PnlLogger3 pnlLog = new PnlLogger3(valueReporter);
		paperBroker.setLogger(pnlLog);		
		
		// instantiate the account managing paper prox. 
		IBroker broker = new AccountManagingBrokerProxy(paperBroker, brokerAccount); 	
			
		// set the instruments ... 
		List<InstrumentSpecification> specs = new ArrayList<InstrumentSpecification>();
		for (int i = 0; i < simConfig.getAlgoEnvConfig().getInstruments().size(); i++) {
			InstrumentSpecification spec = specDao.find(simConfig.getAlgoEnvConfig().getInstruments().get(i)); 
			specs.add(spec);
		}
		
		// configure trade system through algo env config
		AlgoEnvironment algoEnv = new AlgoEnvironment();
		algoEnv.setBroker(broker);
		algoEnv.setBrokerAccount(brokerAccount);
		algoEnv.setAlgoEnvConfig(simConfig.getAlgoEnvConfig());
		algoEnv.setValueReporter(valueReporter);
		
		// instantiate the trade system 		
		Class<IBatchTradeSystem> clazz = (Class<IBatchTradeSystem>)
			Class.forName(simConfig.getAlgoEnvConfig().getAlgoConfig().getAlgorithm());
		IBatchTradeSystem system = clazz.newInstance();
		
		// initialize the trade system through algo env config and algo env
		if(!system.initialize(algoEnv, simConfig.getAlgoEnvConfig().getAlgoConfig()))
			return null;
		
		// populate the instrument spec array and add iterators. 
		MergeSortIterator<Quote> quoteIterator = new MergeSortIterator<Quote>(new Comparator<Quote>(){
			@Override
			public int compare(Quote o1, Quote o2) {
				// return o1.getTimeStamp().compareTo(o2.getTimeStamp());
				// UGLY FIX AT THE MOMENT
				return -1;
			}});		
		
		for(int i=0;i<specs.size();i++)
		{	
			// 						
			SeriesSpecification sspec = new SeriesSpecification(specs.get(i), TimeFrame.TIMEFRAME_1_TICK);
			sspec.setStartTimeStamp(createTimeStamp(simConfig.getSimulationDays().get(0)));
			sspec.setEndTimeStamp(createTimeStamp(simConfig.getSimulationDays().get(simConfig.getSimulationDays().size()-1)));
			quoteIterator.addIterator(seriesDataIteratorSource.fetch(sspec).iterator());				
		}	
		
		//
		log.info("Starting quote feeding ... ");
		
		// replay.
		TimeMeasurement.start("replay");
		long nq = 0L; 		
		while(quoteIterator.hasNext())
		{
			Quote q = quoteIterator.next();
			pnlLog.log(q);
			// time frame check. (has to be moved to environment bracket around system)			
			// distribute quote to subscribers ... (i.e. paperbroker) 
			quoteSource.distributeQuote(q);
			if(!quoteIsSane(q))
				continue;
			if(isQuoteWithinStartStopTimes(q))
			{
				// ... and then to the system
				system.onQuote(q);				
			}
			else {
				// force liquidation at market price 
				system.forcedTradingStop();
				
			}
			nq++;
		}
		// force liquidation (if possible)
		system.forcedTradingStop();
		
		TimeMeasurement.stop("replay");
		log.info("Replayed " + nq + " quotes. ");
		log.info("Generating report ... ");
		
		//
		TimeMeasurement.start("report");
		SimpleReport report = new SimpleReport();
		report.getReportValues().put("Report TimeStamp", new Date());
		SimpleReportInitializer.initialize(report, simConfig);
		
		//
		report.getReportValues().put("SimConfigId", simConfig.getId());
		report.getReportValues().put("ReplayTime (in ms)", TimeMeasurement.getRuntime("replay"));
		report.getReportValues().put("QuotesReplayed", nq);
		report.getReportValues().put("Data throughput (quotes/second)", nq/(TimeMeasurement.getRuntime("replay")/1000.0));
		report.getReportValues().put("PNLObject", pnlLog.getPnlValueSeries());
		report.getReportValues().put("FullLogFile", fullLogFile);
	
		// pnl value series ...
		List<ValueSeriesProcessor> valueSeriesProcessors = new ArrayList<ValueSeriesProcessor>();
		valueSeriesProcessors.add(new PnlChartGenerator(logTarget, 600,400));		
		StatisticsGenerator generator = new StatisticsGenerator(valueSeriesProcessors);
		generator.process(pnlLog.getPnlValueSeries(), report);
		
		// position value series ... 
		valueSeriesProcessors.clear();
		valueSeriesProcessors.add(new PositionChartGenerator(logTarget, 600,400));		
		StatisticsGenerator generator2 = new StatisticsGenerator(valueSeriesProcessors);
		
		for(int i=0;i<specs.size();i++)
		{	
			generator2.process(pnlLog.getPositionValueSeries(specs.get(i)), report);			
		}	
		
	
		//
		new BrokerAccountToSimpleReport().transform(brokerAccount, report);

		TimeMeasurement.stop("report");
		report.getReportValues().put("Report Generation Time", TimeMeasurement.getRuntime("report"));
		
		
		//
		return report;
	}
	
	private boolean quoteIsSane(Quote quote)
	{
		if(quote.getAskPrice()==Quote.NOT_SET || quote.getBidPrice()==Quote.NOT_SET)
			return false; 
		return true; 
	}
	
}
