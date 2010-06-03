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
import org.activequant.util.SimpleSerializer;
import org.activequant.util.VirtualQuoteSubscriptionSource;
import org.activequant.util.pattern.events.IEventListener;
import org.activequant.util.spring.ServiceLocator;
import org.activequant.util.tempjms.InternalQuoteSubscriptionSource;
import org.activequant.util.tempjms.JMS;
import org.activequant.util.tempjms.MessageHandler;
import org.apache.log4j.Logger;
import org.springframework.jmx.export.annotation.ManagedAttribute;

/**
 * Runner that runs a given algo env config in production mode. 1) Loads algo
 * env config file. 2) loads algo config file 3) instantiates trade system 4)
 * loads specifications, etc. 5) inits trade system 6) wires quote sources 7)
 * starts relaying. ... and some steps in between.
 * 
 * @author Ghost Rider
 * 
 */
public class AlgoEnvConfigRunner extends AlgoEnvBase implements
		IEventListener<Quote>{

	protected VirtualQuoteSubscriptionSource quoteSource = new VirtualQuoteSubscriptionSource();
	private static Logger log = Logger.getLogger(AlgoEnvConfigRunner.class);
	protected IFactoryDao factoryDao = new FactoryLocatorDao("data/config.xml");
	protected ISpecificationDao specDao = factoryDao.createSpecificationDao();
	protected IQuoteSubscriptionSource quoteSubscriptionSource;
	protected IBatchTradeSystem system;
	protected BrokerAccount brokerAccount = new BrokerAccount("", "");
	private PnlLogger2 pnlLog;
	protected JMS jmsConnection;
	
	@SuppressWarnings("unchecked")
	public AlgoEnvConfigRunner(String algoConfigFolder, long configId, String logFile, JMS jmsConnection) throws Exception {
		// initialize properly. 
		pnlLog = new PnlLogger2(true, logFile);
		this.jmsConnection = jmsConnection; 
		
		/// 
		quoteSubscriptionSource = new InternalQuoteSubscriptionSource();
		///		
		
		// proceed by loading it.
		SimpleSerializer<AlgoEnvConfig> ser = new SimpleSerializer<AlgoEnvConfig>();
		AlgoEnvConfig algoEnvConfig = ser.load(algoConfigFolder, configId);

		log.info("Loaded: ");
		log.info("ID: " + algoEnvConfig.getId());
		log.info("Instruments: " + algoEnvConfig.getInstruments());
		log.info("Start/Stop Times: " + algoEnvConfig.getStartStopTimes());

		// instantiate trade system.
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
			//
			MessageHandler handler = new MessageHandler((InternalQuoteSubscriptionSource)quoteSubscriptionSource, spec);			
			jmsConnection.subscribeMessageHandler(jmsConnection.getTopicName(spec), handler);
		}

		
		// instantiate the trade system
		Class<IBatchTradeSystem> clazz = (Class<IBatchTradeSystem>) Class
				.forName(algoEnvConfig.getAlgoConfig().getAlgorithm());
		system = clazz.newInstance();

		
		
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
		ServiceLocator.instance("data/algoenvrunner.xml").getContext().getBean("runner");
	}

}
