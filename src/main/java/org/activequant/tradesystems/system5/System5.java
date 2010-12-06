package org.activequant.tradesystems.system5;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.activequant.container.report.SimpleReport;
import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.account.Order;
import org.activequant.core.domainmodel.account.Position;
import org.activequant.core.domainmodel.data.Candle;
import org.activequant.core.domainmodel.data.Quote;
import org.activequant.core.domainmodel.events.OrderEvent;
import org.activequant.core.domainmodel.events.OrderExecutionEvent;
import org.activequant.core.types.TimeFrame;
import org.activequant.core.types.TimeStamp;
import org.activequant.math.algorithms.EMAAccumulator;
import org.activequant.optimization.domainmodel.AlgoConfig;
import org.activequant.tradesystems.AlgoEnvironment;
import org.activequant.tradesystems.BasicTradeSystem;
import org.activequant.util.RecorderCandleDao;
import org.activequant.util.messaging.jabber.JabberMessenger;
import org.activequant.util.pattern.events.IEventListener2;
import org.activequant.util.tools.ArrayUtils;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;


/**

    System 5. 

**/
public class System5 extends BasicTradeSystem {

	private List<Quote> quoteList = new ArrayList<Quote>();
	private Quote formerQuote;
	private int period1; 
	private double mpOld, p1Old, p2Old; 
	private int formerDirection = 0;
	private double stopLossPnl = 100;  

    protected final static Logger log = Logger.getLogger(System5.class);
	

	private List<Double> lows = new ArrayList<Double>();
	private List<Double> highs = new ArrayList<Double>();
	private List<Double> opens = new ArrayList<Double>();
	private List<Double> closes = new ArrayList<Double>();
	BufferedWriter candleWriter;
	private RecorderCandleDao candleDao; 
	private EMAAccumulator emaAcc = new EMAAccumulator();

	private double open, high, low = Double.MAX_VALUE, close;
	private JabberMessenger jm;
	private XMPPConnection con; 
	private MultiUserChat muc; 

	int formerPosition = 0;

	int quoteUpdateCount = 0;
	boolean shortStopped = false;
	boolean longStopped = false;

	@Override
	public boolean initialize(AlgoEnvironment algoEnv, AlgoConfig algoConfig) {
		super.initialize(algoEnv, algoConfig);
		try{
			candleDao = new RecorderCandleDao("/home/share/archive");
			String server = System.getProperty("XMPP_SERVER");
			con = new XMPPConnection(server);
			con.connect();
			String login=System.getProperty("XMPP_UID");
			String pass=System.getProperty("XMPP_PWD");
			con.login(login, pass);
			muc = new MultiUserChat(con, "system5@conference.activequant.org");
			muc.join("system5");
			silentSend("System 5 is coming up.");
			// initialize the candle stream writer
			candleWriter = new BufferedWriter(new FileWriter("candles.csv")); 	
			
			// register for the order events. 
			getOrderEvents().addEventListener(new IEventListener2<Order, OrderEvent>() {				
				@Override
				public void eventFired(Order arg0, OrderEvent arg1) throws Exception {
					if(arg1 instanceof OrderExecutionEvent)
					{
						silentSend("[OrderEvent] "+arg0.toString()+" -> "+((OrderExecutionEvent)arg1).getQuantity()+" @ "+((OrderExecutionEvent)arg1).getPrice());
					}
					else{
						silentSend("[OrderEvent] "+arg0.toString()+" -> "+arg1.getMessage());
					}
				}
			});
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
		period1 = 7; 
		emaAcc.setPeriod(period1);
		return true;
	}

	/**
	 * Silently send a message to a multi user conference room. 
	 * @param to
	 * @param subj
	 * @param msg
	 */
	private void silentSend(String msg)
	{
		try{
			muc.sendMessage(msg);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void silentWriteCandle(InstrumentSpecification spec, double open, double high, double low, double close, double ema)
	{
		try{
			candleWriter.write(System.currentTimeMillis()+";"+open+";"+high+";"+low+";"+close+";"+ema+"\n");
			candleWriter.flush();
			Candle c = new Candle(spec, new TimeStamp(new Date()), open, high, low, close, 0.0, TimeFrame.TIMEFRAME_1_TICK);
			candleDao.update(c);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}		

	}

	@Override
	public void onQuote(Quote quote) {
	
		
		// check if it is the first quote we receive. 
		if(open==0.0)
			open = quote.getMidpoint();
		
		double currentPosition = 0.000;
		if (getAlgoEnv().getBrokerAccount().getPortfolio().hasPosition(
				quote.getInstrumentSpecification())) {
			Position pos = getAlgoEnv().getBrokerAccount().getPortfolio().getPosition(quote.getInstrumentSpecification());
			currentPosition = getAlgoEnv().getBrokerAccount().getPortfolio()
					.getPosition(quote.getInstrumentSpecification())
					.getQuantity();
			// get the price difference
			double priceDiff = pos.getPriceDifference(quote);
			
			// compute the current pnl
			double currentPositionPnl = currentPosition * priceDiff; 
			
			// check if the current pnl is lower than our stop loss pnl 
			if(currentPositionPnl < stopLossPnl)
			{
			    double stopLimitPrice = quote.getBidPrice();
			    if(currentPosition < 0) stopLimitPrice = quote.getAskPrice();
			    // liquidate 
			    setTargetPosition(quote.getTimeStamp(), quote.getInstrumentSpecification(), 0, stopLimitPrice);
			    return;
			}			
		}

		if (formerQuote != null) {
			if ((quote.getBidPrice() == formerQuote.getBidPrice() && quote
					.getAskPrice() == formerQuote.getAskPrice())
					|| (quote.getBidPrice() == Quote.NOT_SET || quote
							.getAskPrice() == Quote.NOT_SET))
				return;
		}
		log.info("New quote: "+quote.toString());

		System.out.print("\n"+quote.getBidPrice()+"/"+quote.getAskPrice()+" ");
		formerQuote = quote; 

		// only 100% sane quotes ...
		if (quote.getBidPrice() == Quote.NOT_SET
				|| quote.getAskPrice() == Quote.NOT_SET)
			return;
	
		quoteUpdateCount++;

		// aggregating five quotes into one candle (non-time discrete)
		if (quoteUpdateCount == 10) {
			quoteUpdateCount = 0;
			if (open > 0) {
				lows.add(low);
				opens.add(open);
				highs.add(high);
				closes.add(close);
			}
			emaAcc.accumulate(close);
			silentWriteCandle(quote.getInstrumentSpecification(), open, high, low, close, emaAcc.getMeanValue());
			log.info("New OHLC: "+open+"/"+high+"/"+low+"/"+close+". Have "+lows.size());
			open = quote.getMidpoint();
			close = quote.getMidpoint();
			high = 0;
			low = Double.MAX_VALUE;
			//quote = new Quote();
		}

		if (quote.getMidpoint() > high) {
			high = quote.getMidpoint();
			log.info("New high: "+high);
		}
		if (quote.getMidpoint() < low) {
			low = quote.getMidpoint();
			log.info("New low: "+low);
		}
		close = quote.getMidpoint();

		if(quoteUpdateCount!=0) 
			return;

		// slice ..
		if (opens.size() > period1 + 2) {
			opens.remove(0);
			highs.remove(0);
			lows.remove(0);
			closes.remove(0);
		}

		Collections.reverse(opens);
		Collections.reverse(highs);
		Collections.reverse(lows);
		Collections.reverse(closes);

		double[] opensArray = ArrayUtils.convert(opens);
		double[] highsArray = ArrayUtils.convert(highs);
		double[] lowsArray = ArrayUtils.convert(lows);
		double[] closesArray = ArrayUtils.convert(closes);

		Collections.reverse(opens);
		Collections.reverse(highs);
		Collections.reverse(lows);
		Collections.reverse(closes);
		// log.info("Opens: " + opens.size());
		if (opens.size() < period1 + 1)
		{
			log.warn("Not enough OHLC datasets, yet: "+ opens.size()+" but would need " +(period1+1));
			return;
		}
		
		double p1 = emaAcc.getMeanValue();
		double lastLow = lows.get(closes.size()-1);
		double lastHigh = highs.get(closes.size()-1);
		log.info("Calc output: "+p1+" <> L:" + lastLow + " <> H:" + lastHigh);

		// 
		if(lastLow < p1 && p1 < lastHigh)
		{
			silentSend(lastLow +" < *"+p1+"* < " + lastHigh);
		}
		else if(p1 < lastLow)
		{
			silentSend(" *"+ p1 + "* < " + lastLow +" < " + lastHigh);
		}
		else if(p1 > lastHigh)
		{
			silentSend(lastLow +" < " + lastHigh + " < *" + p1 + "*");
		}

		// 

		if(p1>0.0)
		{
			if(lastLow > p1 && formerDirection != 1)
			{
				log.info("Detecting a long at "+quote.toString());
				formerDirection = 1;
				silentSend("System 5 says long at "+quote.toString());		
				setTargetPosition(quote.getTimeStamp(), quote.getInstrumentSpecification(), 1, quote.getAskPrice());
				
			}
			else if(lastHigh < p1 && formerDirection != -1)
			{
				log.info("Detecting a short at "+quote.toString());
				formerDirection = -1; 
				silentSend("System 5 says short at "+quote.toString());		
				setTargetPosition(quote.getTimeStamp(), quote.getInstrumentSpecification(), -1, quote.getAskPrice());
			}
		}	
	}

	public void populateReport(SimpleReport report) {
	}

	@Override
	public void forcedTradingStop() {

		// // log.info("Forced liquidation");
		quoteList.clear();
		if (formerQuote != null)
			setTargetPosition(formerQuote.getTimeStamp(), formerQuote
					.getInstrumentSpecification(), 0, 0.0);
		formerQuote = null;

		lows.clear();
		opens.clear();
		highs.clear();
		closes.clear();

	}

}
