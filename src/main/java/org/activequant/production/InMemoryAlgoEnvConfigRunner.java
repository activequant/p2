package org.activequant.production;


import java.util.ArrayList;
import java.util.List;

import org.activequant.broker.AccountManagingBrokerProxy;
import org.activequant.broker.IBroker;
import org.activequant.broker.PaperBroker;
import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.account.BrokerAccount;
import org.activequant.core.domainmodel.account.Portfolio;
import org.activequant.core.domainmodel.account.Position;
import org.activequant.core.domainmodel.data.Quote;
import org.activequant.dao.IFactoryDao;
import org.activequant.dao.ISpecificationDao;
import org.activequant.dao.hibernate.FactoryLocatorDao;
import org.activequant.data.retrieval.IQuoteSubscriptionSource;
import org.activequant.data.retrieval.ISubscription;
import org.activequant.optimization.domainmodel.AlgoEnvConfig;
import org.activequant.reporting.PnlLogger2;
import org.activequant.reporting.VoidValueReporter;
import org.activequant.tradesystems.AlgoEnvironment;
import org.activequant.tradesystems.IBatchTradeSystem;
import org.activequant.util.AlgoEnvBase;
import org.activequant.util.VirtualQuoteSubscriptionSource;
import org.activequant.util.pattern.events.IEventListener;
import org.activequant.util.spring.ServiceLocator;
import org.activequant.util.tempjms.InternalQuoteSubscriptionSource;
import org.activequant.util.tempjms.JMS;
import org.activequant.util.tempjms.MessageHandler;
import org.apache.log4j.Logger;
import org.springframework.jmx.export.annotation.ManagedAttribute;

/**
 * Runner that runs with an in memory algo env config. 
 * It takes a class name as parameter and instantiates the class behind this classname
 * , assuming that the class name points to an algo env config. 
 * 
 * @author Ulrich Staudinger
 * 
 * 
 */
public class InMemoryAlgoEnvConfigRunner extends AlgoEnvBase implements
		IEventListener<Quote>{

	protected VirtualQuoteSubscriptionSource quoteSource = new VirtualQuoteSubscriptionSource();
	private static Logger log = Logger.getLogger(InMemoryAlgoEnvConfigRunner.class);
	protected IFactoryDao factoryDao = new FactoryLocatorDao("data/config.xml");
	protected ISpecificationDao specDao = factoryDao.createSpecificationDao();
	protected IQuoteSubscriptionSource quoteSubscriptionSource;
	protected IBatchTradeSystem system;
	protected BrokerAccount brokerAccount = new BrokerAccount("", "");
	private PnlLogger2 pnlLog;
	protected JMS jmsConnection;
	
	/**
	 * plain constructor
	 * @param logFile
	 * @param jmsConnection
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public InMemoryAlgoEnvConfigRunner(String logFile, JMS jmsConnection) throws Exception {
		// initialize properly. 
		pnlLog = new PnlLogger2(true, logFile);
		this.jmsConnection = jmsConnection; 
	}

	public void init(String algoEnvClassFile) throws Exception {

		/// 
		quoteSubscriptionSource = new InternalQuoteSubscriptionSource();
		///		
		
		// instantiate the trade system
		@SuppressWarnings("unchecked")
		Class<AlgoEnvConfig> clazz1 = (Class<AlgoEnvConfig>) Class
				.forName(algoEnvClassFile);
		AlgoEnvConfig algoEnvConfig = clazz1.newInstance();

		log.info("Loaded: ");
		log.info("ID: " + algoEnvConfig.getId());
		log.info("Instruments: " + algoEnvConfig.getInstruments());
		log.info("Start/Stop Times: " + algoEnvConfig.getStartStopTimes());

		// instantiate trade system.RunMode
		initializeStartStops(algoEnvConfig.getStartStopTimes());

		
		// instantiate the paper broker
		PaperBroker paperBroker = new PaperBroker(quoteSource);
		paperBroker.setLogger(pnlLog);
		IBroker broker = new AccountManagingBrokerProxy(paperBroker,
				brokerAccount);		

		
		// set the instruments ... 
		List<InstrumentSpecification> specs = new ArrayList<InstrumentSpecification>();
		for (int i = 0; i < algoEnvConfig.getInstruments().size(); i++) {
			InstrumentSpecification spec = specDao.find(algoEnvConfig.getInstruments().get(i)); 
			specs.add(spec);
			// wire quote subscription source and jms source. Very dirty, i know. 
			MessageHandler handler = new MessageHandler((InternalQuoteSubscriptionSource)quoteSubscriptionSource, null, spec);			
			jmsConnection.subscribeMessageHandler(jmsConnection.getTopicName(spec), handler);
		}

		
		// instantiate the trade system
		@SuppressWarnings("unchecked")
		Class<IBatchTradeSystem> clazz2 = (Class<IBatchTradeSystem>) Class
				.forName(algoEnvConfig.getAlgoConfig().getAlgorithm());
		system = clazz2.newInstance();

		
		
		// configure trade system through algo env config
		AlgoEnvironment algoEnv = new AlgoEnvironment();
		algoEnv.setBroker(broker);
		algoEnv.setBrokerAccount(brokerAccount);
		algoEnv.setAlgoEnvConfig(algoEnvConfig);
		algoEnv.setValueReporter(new VoidValueReporter());
		
		if (!system.initialize(algoEnv, algoEnvConfig.getAlgoConfig()))
			return;


		//
		log.info("All set, starting data feeds");

		// 
		for (int i = 0; i < specs.size(); i++) {
			ISubscription<Quote> quoteSub = quoteSubscriptionSource.subscribe(specs.get(i));
			quoteSub.addEventListener(this);
			quoteSub.activate();
		}
		//
		log.info("Awaiting data ...");

	}
	
	
	/**
	 * called whenever a quote has arrived.
	 */
	public void eventFired(Quote q) {
		quoteSource.distributeQuote(q);		
		// check if the quote is within the start and stop times or outside. 
		if (isQuoteWithinStartStopTimes(q)) {
			// ... and then to the system
			system.onQuote(q);
			pnlLog.log(q);
		} else {
			// force liquidation at market price
			system.forcedTradingStop();
		}
	}
	
	@ManagedAttribute(description="Current PNL", currencyTimeLimit=15)
	public double getCurrentPnl()
	{
		if(pnlLog.getPnlValueSeries().size()>0)
			return pnlLog.getPnlValueSeries().lastElement().getValue();
		return 0.0;
	}
	
	@ManagedAttribute(description="Current Portfolio as String", currencyTimeLimit=15)
	public String getPortfolioAsString()
	{
		String ret = ""; 
		
		Portfolio p = brokerAccount.getPortfolio();
		for(Position pos : p.getPositions())
			ret+=pos.toString()+"; ";
		return ret; 
	}
	
	public static void main(String[] args) throws Exception {
		if(args.length==1)
		{
			InMemoryAlgoEnvConfigRunner runner = (InMemoryAlgoEnvConfigRunner) ServiceLocator.instance("data/inmemoryalgoenvrunner.xml").getContext().getBean("runner");
			runner.init(args[0]);
		}
		else
		{
			System.out.println("Need class name as parameter.");
		}
	}

}
