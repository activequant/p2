package org.activequant.util;

import java.util.*;
import java.net.*;
import java.io.*;

import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.data.Candle;
import org.activequant.core.domainmodel.data.Quote;
import org.activequant.core.types.TimeFrame;
import org.activequant.dao.IFactoryDao;
import org.activequant.dao.ISpecificationDao;
import org.activequant.dao.hibernate.FactoryLocatorDao;
import org.activequant.data.retrieval.ISubscription;
import org.activequant.data.util.converter.QuoteToTradeIndicationSubscriptionSourceConverter;
import org.activequant.data.util.converter.TradeIndicationToCandleConverter;
import org.activequant.util.pattern.events.IEventListener;
import org.activequant.util.tempjms.JMS;
import org.activequant.util.tempjms.JMSQuoteSubscriptionSource;
import org.activequant.util.tools.UniqueDateGenerator;

/**
 * Collects data for one instrumentId, builds OHLC 1minute candles out of these and relays these over a TCP socket in CSV format to connected clients.
 * It subscribes to quotes for a specific instrument specification
 * and candleizes these, when candleized, the last X candles are relayed over a TCP socket to subscribers.  
 * 
 * <p>
 * History: <br> - [24.09.2010] Created (GhostRider)<br>
 * 
 */

public class DebugQuoteDumper  {

	public DebugQuoteDumper() throws Exception {
		// read the jms host
		if (System.getProperties().containsKey("JMS_HOST"))
			jmsHost = System.getProperty("JMS_HOST");
		else
			jmsHost = "83.169.9.78";

		if (System.getProperties().containsKey("JMS_PORT"))
			jmsPort = Integer.parseInt(System.getProperty("JMS_PORT"));
		else
			jmsPort = 7676;
		
		if (System.getProperties().containsKey("SPECIFICATION_ID"))
			specificationId = Integer.parseInt(System.getProperty("SPECIFICATION_ID"));
		else
			specificationId = 86; 
		
		
		initQuoteFeeds();
	}

	

	private void initQuoteFeeds() throws Exception {
		jmsQuoteSubscriptionSource = new JMSQuoteSubscriptionSource(jmsHost, jmsPort);
		
		InstrumentSpecification spec = specDao.find(specificationId);
		ISubscription<Quote> subs = jmsQuoteSubscriptionSource.subscribe(spec);
		subs.addEventListener(new IEventListener<Quote>(){
			public void eventFired(Quote q){
				System.out.println(q.toString());
			}
		});
		subs.activate();
	}

	public static void main(String[] args) throws Exception {
		new DebugQuoteDumper();
	}

	private int quoteCount = 0; 	
	private int specificationId = 86; 
	private int theCacheSize = 10000;
	private JMS jms;
	private IFactoryDao factoryDao = new FactoryLocatorDao("data/config.xml");
	private ISpecificationDao specDao = factoryDao.createSpecificationDao();
	private JMSQuoteSubscriptionSource jmsQuoteSubscriptionSource;
	private String jmsHost = "";
	private int jmsPort = 7676;
	private UniqueDateGenerator uniqueDateGen = new UniqueDateGenerator();
}
