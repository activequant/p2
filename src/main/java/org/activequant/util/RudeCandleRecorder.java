package org.activequant.util;

import java.text.SimpleDateFormat;

import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.data.Candle;
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
 * A very simple recorder for candles. It subscribes to quotes for all known 
 * instrument specifications and candleizes these. 
 * These candles are stored through a CandleDao, for example RecorderCandleDao. 
 * <p>
 * History: <br> - [27.09.2009] Created (GhostRider)<br>
 * 
 */

public class RudeCandleRecorder  {

	private RecorderCandleDao rcd;

	class Task implements Runnable {
		private TradeIndicationToCandleConverter conv;
		private long msOfFrame;

		public Task(TradeIndicationToCandleConverter conv, long msOfFrame) {
			this.conv = conv;
			this.msOfFrame = msOfFrame;
		}
		public void localSleep(int ms){
			try { Thread.sleep(ms); } catch(Exception ex) {ex.printStackTrace();}
		}
	
		public void run() {
			while (true) {
				try {
					long ms = System.currentTimeMillis();
					long delay = msOfFrame - (ms % msOfFrame);
					Thread.sleep(delay);
					boolean rerun = true; 
					while (rerun) {
						try {
							conv.getSyncEventListener().eventFired(
									uniqueDateGen.generate(System.currentTimeMillis()));
							rerun = false; 
						} catch(Exception ex) { localSleep(10); rerun = true; }

					}
				} catch (Exception anEx) {
					anEx.printStackTrace();
				}
			}
		}
	}
	
	class CandleAggregator implements IEventListener<Candle> {
		@Override
		public void eventFired(Candle event) {			
			// save the candle. 
			rcd.update(event);
		}
	}

	public RudeCandleRecorder() throws Exception {
		// read the jms host
		if (System.getProperties().containsKey("JMS_HOST"))
			jmsHost = System.getProperty("JMS_HOST");
		else
			jmsHost = "83.169.9.78";

		if (System.getProperties().containsKey("JMS_PORT"))
			jmsPort = Integer.parseInt(System.getProperty("JMS_PORT"));
		else
			jmsPort = 7676;
		
		if (System.getProperties().containsKey("ARCHIVE_BASE_FOLDER"))
			rcd = new RecorderCandleDao(System.getProperty("ARCHIVE_BASE_FOLDER"));
		else
			rcd = new RecorderCandleDao(System.getProperty("./"));
		
		initializeChannels();
		initQuoteFeeds();
	}

	private void initializeChannels() {
		System.out.println("Initializing channels ...");	
		InstrumentSpecification[] specs = specDao.findAll();
		theChannelsToBeCached = new String[specs.length];
		for (int i = 0; i < specs.length; i++) {
			// 
			theChannelsToBeCached[i] = "AQID" + specs[i].getId().toString();
		}
	}

	

	private void initQuoteFeeds() throws Exception {
		jmsQuoteSubscriptionSource = new JMSQuoteSubscriptionSource(jmsHost,
				jmsPort);
		System.out.println("JMS quote source constructed. ");
		QuoteToTradeIndicationSubscriptionSourceConverter conv = new QuoteToTradeIndicationSubscriptionSourceConverter(
				jmsQuoteSubscriptionSource);
		System.out.println("Q2T constructed.");
		candleSource1Min = new TradeIndicationToCandleConverter(conv);
		candleSource2Min = new TradeIndicationToCandleConverter(conv);
		candleSource5Min = new TradeIndicationToCandleConverter(conv);
		candleSource10Min = new TradeIndicationToCandleConverter(conv);
		candleSource15Min = new TradeIndicationToCandleConverter(conv);
		candleSource30Min = new TradeIndicationToCandleConverter(conv);
		candleSource1Hour = new TradeIndicationToCandleConverter(conv);
		candleSource2Hours = new TradeIndicationToCandleConverter(conv);
		System.out.println("Candle sources constructed.");
		candleSource1Min.setForceFrameOnExternalSync(true);
		candleSource1Min.setUseExternalSyncOnly(true);
		candleSource2Min.setForceFrameOnExternalSync(true);
		candleSource2Min.setUseExternalSyncOnly(true);
		candleSource5Min.setForceFrameOnExternalSync(true);
		candleSource5Min.setUseExternalSyncOnly(true);
		candleSource10Min.setForceFrameOnExternalSync(true);
		candleSource10Min.setUseExternalSyncOnly(true);
		candleSource15Min.setForceFrameOnExternalSync(true);
		candleSource15Min.setUseExternalSyncOnly(true);
		candleSource30Min.setForceFrameOnExternalSync(true);
		candleSource30Min.setUseExternalSyncOnly(true);
		candleSource1Hour.setForceFrameOnExternalSync(true);
		candleSource1Hour.setUseExternalSyncOnly(true);
		candleSource2Hours.setForceFrameOnExternalSync(true);
		candleSource2Hours.setUseExternalSyncOnly(true);

		InstrumentSpecification[] specs = specDao.findAll();
		for (InstrumentSpecification spec : specs) {
			manageSubscription(candleSource1Min, TimeFrame.TIMEFRAME_1_MINUTE,
					spec);
			manageSubscription(candleSource2Min, TimeFrame.TIMEFRAME_2_MINUTES,
					spec);
			manageSubscription(candleSource5Min, TimeFrame.TIMEFRAME_5_MINUTES,
					spec);
			manageSubscription(candleSource10Min,
					TimeFrame.TIMEFRAME_10_MINUTES, spec);
			manageSubscription(candleSource15Min,
					TimeFrame.TIMEFRAME_15_MINUTES, spec);
			manageSubscription(candleSource30Min,
					TimeFrame.TIMEFRAME_30_MINUTES, spec);
			manageSubscription(candleSource1Hour,
					TimeFrame.TIMEFRAME_60_MINUTES, spec);
		}

		// 
		Task task = new Task(candleSource1Min, 1000 * 60);
		new Thread(task).start();
		task = new Task(candleSource2Min, 1000 * 60 * 2);
		new Thread(task).start();
		task = new Task(candleSource5Min, 1000 * 60 * 5);
		new Thread(task).start();
		task = new Task(candleSource10Min, 1000 * 60 * 10);
		new Thread(task).start();
		task = new Task(candleSource15Min, 1000 * 60 * 15);
		new Thread(task).start();
		task = new Task(candleSource30Min, 1000 * 60 * 30);
		new Thread(task).start();
		task = new Task(candleSource1Hour, 1000 * 60 * 60);
		new Thread(task).start();
		// 

	}

	private void manageSubscription(TradeIndicationToCandleConverter conv,
			TimeFrame timeFrame, InstrumentSpecification spec) {
		System.out.println("Activating subscription for "+timeFrame+"/"+spec);
		ISubscription<Candle> subs = conv.subscribe(spec, timeFrame);
		subs.addEventListener(new CandleAggregator());
		subs.activate();
	}


	public static void main(String[] args) throws Exception {
		new RudeCandleRecorder();
	}

	private String[] theChannelsToBeCached = new String[] { "89","94" };
	private int theCacheSize = 10000;
	private JMS jms;
	private IFactoryDao factoryDao = new FactoryLocatorDao("data/config.xml");
	private ISpecificationDao specDao = factoryDao.createSpecificationDao();
	private JMSQuoteSubscriptionSource jmsQuoteSubscriptionSource;
	private TradeIndicationToCandleConverter candleSource1Min;
	private TradeIndicationToCandleConverter candleSource2Min;
	private TradeIndicationToCandleConverter candleSource5Min;
	private TradeIndicationToCandleConverter candleSource10Min;
	private TradeIndicationToCandleConverter candleSource15Min;
	private TradeIndicationToCandleConverter candleSource30Min;
	private TradeIndicationToCandleConverter candleSource1Hour;
	private TradeIndicationToCandleConverter candleSource2Hours;
	private String jmsHost = "";
	private int jmsPort = 7676;
	private UniqueDateGenerator uniqueDateGen = new UniqueDateGenerator();
}
