package org.activequant.util;

import java.util.*;
import java.net.*;
import java.io.*;

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
 * A very simple recorder and relay for candles. It subscribes to quotes for a specific instrument specification
 * and candleizes these, when candleized, the last X candles are relayed over a TCP socket to subscribers.  
 * 
 * <p>
 * History: <br> - [24.09.2010] Created (Ulrich Staudinger)<br>
 * 
 */

public class CandleSocketRelay  {

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
			// append the candle to the current cache. 
			
			lastCandles.add(event.toString());
			if(lastCandles.size()>200)lastCandles.remove(0);
			// distribute the candle
			List<BufferedWriter> toBeRemoved = new ArrayList<BufferedWriter>();
			for(BufferedWriter writer : writers)
			{	
				try{
					for(String l : lastCandles)
					{
						writer.write(l);
						writer.write("\n");
					}
					writer.write(".");
					writer.write("\n");
					writer.flush();
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
					toBeRemoved.add(writer);
				}
			}
			// clean out
			for(BufferedWriter writer : toBeRemoved)
			{
				writers.remove(writer);
			}
		}
	}

	public CandleSocketRelay() throws Exception {
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
		
		if (System.getProperties().containsKey("LISTENER_PORT"))
			tcpListenerPort = Integer.parseInt(System.getProperty("LISTENER_PORT"));
		else
			tcpListenerPort = 13431; 
		
		initQuoteFeeds();
	}

	

	private void initQuoteFeeds() throws Exception {
		jmsQuoteSubscriptionSource = new JMSQuoteSubscriptionSource(jmsHost, jmsPort);
		System.out.println("JMS quote source constructed. ");
		QuoteToTradeIndicationSubscriptionSourceConverter conv = new QuoteToTradeIndicationSubscriptionSourceConverter(
				jmsQuoteSubscriptionSource);
		System.out.println("Q2T constructed.");
		candleSource1Min = new TradeIndicationToCandleConverter(conv);
		System.out.println("Candle sources constructed.");
		candleSource1Min.setForceFrameOnExternalSync(true);
		candleSource1Min.setUseExternalSyncOnly(true);
		
		InstrumentSpecification spec = specDao.find(specificationId);
		manageSubscription(candleSource1Min, TimeFrame.TIMEFRAME_1_MINUTE, spec);
		
		// 
		Task task = new Task(candleSource1Min, 1000 * 60);
		new Thread(task).start();
		// 
		
		// init the tcp listeners. 
		Runnable r = new Runnable(){
			public void run()
			{
				
				while(true)
				{
						try{
							Thread.sleep(5000);
							ServerSocket ss = new ServerSocket(tcpListenerPort);
							Socket s = ss.accept();
							BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
							writers.add(bw);
						}
						catch(Exception ex)
						{
							ex.printStackTrace();
						}
				}
			}
			
		};
		Thread t = new Thread(r);
		t.start();
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
	
	private int specificationId = 86; 
	private int theCacheSize = 10000;
	private JMS jms;
	private IFactoryDao factoryDao = new FactoryLocatorDao("data/config.xml");
	private ISpecificationDao specDao = factoryDao.createSpecificationDao();
	private JMSQuoteSubscriptionSource jmsQuoteSubscriptionSource;
	private TradeIndicationToCandleConverter candleSource1Min;
	private String jmsHost = "";
	private int jmsPort = 7676;
	private List<String> lastCandles = new ArrayList<String>();
	private int tcpListenerPort; 
	private List<BufferedWriter> writers = new ArrayList<BufferedWriter>();
	private UniqueDateGenerator uniqueDateGen = new UniqueDateGenerator();
}
