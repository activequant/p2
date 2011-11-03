package org.activequant.tradesystems.system5;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.activequant.container.report.SimpleReport;
import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.SeriesSpecification;
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
import org.activequant.persistence.TradeStatistics;
import org.activequant.persistence.TradeStatisticsDao;
import org.activequant.reporting.MUCValueReporter;
import org.activequant.reporting.PnlLogger3;
import org.activequant.tradesystems.AlgoEnvironment;
import org.activequant.tradesystems.BasicTradeSystem;
import org.activequant.tradesystems.RunMode;
import org.activequant.util.RecorderCandleDao;
import org.activequant.util.pattern.events.IEventListener2;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.MultiUserChat;

/**
 * System 5.
 **/
public class System5 extends BasicTradeSystem {

	private List<Quote> quoteList = new ArrayList<Quote>();
	private Quote formerQuote;
	private int period1;
	private int formerDirection = 0;
	private double stopLossPnl = -100, currentPosition = 0.0,
			currentPositionPnl = 0.0;
	private double maxPnl = 0.0, minPnl = 0.0;
	private long tradeId =0; 
	protected final static Logger log = Logger.getLogger(System5.class);

	private TradeStatistics ts;
	private List<Double> lows = new ArrayList<Double>();
	private List<Double> highs = new ArrayList<Double>();
	private List<Double> opens = new ArrayList<Double>();
	private List<Double> closes = new ArrayList<Double>();
	BufferedWriter candleWriter;
	private RecorderCandleDao candleDao;

	private EMAAccumulator emaAcc = new EMAAccumulator();

	private double open, high, low = Double.MAX_VALUE, close;
	private XMPPConnection con;
	private MultiUserChat muc;
	private PnlLogger3 pnlLogger3;

	private int quoteUpdateCount = 0;
	private boolean tradeFlag = true;
	private TradeStatisticsDao tradeStatisticsDao;
	private String sessionId; 


	@Override
	public boolean initialize(AlgoEnvironment algoEnv, AlgoConfig algoConfig) {
		super.initialize(algoEnv, algoConfig);
		sessionId = "SESSION_"+System.currentTimeMillis();
		period1 = 7;
		emaAcc.setPeriod(period1);
		try {
			if (algoEnv.getRunMode().equals(RunMode.PRODUCTION)) {
				tradeStatisticsDao = new TradeStatisticsDao();
				// do the backfill
				backfill();
				//
				candleDao = new RecorderCandleDao("/home/share/archive");
				String server = System.getProperty("XMPP_SERVER");
				con = new XMPPConnection(server);
				con.connect();
				String login = System.getProperty("XMPP_UID");
				String pass = System.getProperty("XMPP_PWD");
				con.login(login, pass, "System5");
				muc = new MultiUserChat(con,
						"system5@conference.activequant.org");
				muc.join("system5");
				muc.addMessageListener(new PacketListener() {
					@Override
					public void processPacket(Packet arg0) {
						if (arg0 instanceof Message) {
							Message msg = (Message) arg0;
							String body = msg.getBody();
							if (body.equals("STOP")) {
								tradeFlag = false;
								silentSend("Stopping trading. ");
							} else if (body.equals("START")) {
								tradeFlag = true;
								silentSend("Starting trading. ");
							}
						}
					}
				});
				silentSend("System 5 is coming up.");

				// initialize the candle stream writer
				candleWriter = new BufferedWriter(new FileWriter("candles.csv"));

				//
				pnlLogger3 = new PnlLogger3(new MUCValueReporter(server, login,
						pass, "System5Pnl",
						"system5pnl@conference.activequant.org"));

				// register for the order events.
				getOrderEvents().addEventListener(
						new IEventListener2<Order, OrderEvent>() {
							@Override
							public void eventFired(Order arg0, OrderEvent arg1)
									throws Exception {
								if (getAlgoEnv().getRunMode().equals(
										RunMode.PRODUCTION))
									pnlLogger3.log(arg0, arg1);
								if (arg1 instanceof OrderExecutionEvent) {
									silentSend("[OrderExecution] "
											+ arg0.toString()
											+ "\nEXECUTED: "
											+ ((OrderExecutionEvent) arg1)
													.getQuantity()
											+ " @ "
											+ ((OrderExecutionEvent) arg1)
													.getPrice());
								} else {
									// silentSend("[OrderEvent] "+arg0.toString()+" -> "+arg1.getMessage());
								}
							}
						});

			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Mind that the DAOs have to be initialized properly.
	 */
	public void backfill() {
		int instrumentId = getAlgoEnv().getAlgoEnvConfig().getInstruments()
				.get(0);
		log.info("Loading specification with id " + instrumentId);
		InstrumentSpecification spec = getAlgoEnv().getSpecDao().find(
				instrumentId);
		SeriesSpecification query = new SeriesSpecification(spec);
		Calendar cal = GregorianCalendar.getInstance();
		cal.add(Calendar.HOUR, -2);
		query.setStartTimeStamp(new TimeStamp(cal.getTime()));
		query.setEndTimeStamp(new TimeStamp(new Date()));
		// set start and stop time frame.
		log.info("Loading quotes. ");
		Quote[] quotes = getAlgoEnv().getQuoteDao().findBySeriesSpecification(
				query);
		int length = quotes.length;
		boolean fomerTf = tradeFlag;
		tradeFlag = false;
		log.info("Replaying " + length + " quotes");
		for (int i = length - 1; i >= 0; i--) {
			Quote q = quotes[i];
			onQuote(q);
		}
		log.info("Replayed " + length + " quotes.");
		tradeFlag = fomerTf;
	}

	/**
	 * Silently send a message to a multi user conference room.
	 * 
	 * @param to
	 * @param subj
	 * @param msg
	 */
	private void silentSend(String msg) {
		try {
			if (getAlgoEnv().getRunMode().equals(RunMode.PRODUCTION)
					&& tradeFlag)
				muc.sendMessage(msg);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void silentWriteCandle(InstrumentSpecification spec, double open,
			double high, double low, double close, double ema) {
		try {
			if (getAlgoEnv().getRunMode().equals(RunMode.PRODUCTION)
					&& tradeFlag) {
				// save the ema output.
				candleWriter.write(System.currentTimeMillis() + ";" + open
						+ ";" + high + ";" + low + ";" + close + ";" + ema
						+ "\n");
				candleWriter.flush();
				// also save it to the archive.
				Candle c = new Candle(spec, new TimeStamp(new Date()), open,
						high, low, close, 0.0, TimeFrame.TIMEFRAME_1_TICK);
				candleDao.update(c);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@Override
	public void onQuote(Quote quote) {

		if (!okChecks(quote))
			return;
		positionChecks(quote);

		//
		formerQuote = quote;

		quoteUpdateCount++;

		// aggregating five quotes into one candle (non-time discrete)
		if (quoteUpdateCount == 100) {
			quoteUpdateCount = 0;
			lows.add(low);
			opens.add(open);
			highs.add(high);
			closes.add(close);
			emaAcc.accumulate(close);
			silentWriteCandle(quote.getInstrumentSpecification(), open, high,
					low, close, emaAcc.getMeanValue());
			log.info("New OHLC: " + open + "/" + high + "/" + low + "/" + close
					+ ". Have " + lows.size());
			open = quote.getMidpoint();
			close = quote.getMidpoint();
			high = 0;
			low = Double.MAX_VALUE;
		}

		if (quote.getMidpoint() > high) {
			high = quote.getMidpoint();
			log.info("New high: " + high);
		}
		if (quote.getMidpoint() < low) {
			low = quote.getMidpoint();
			log.info("New low: " + low);
		}
		close = quote.getMidpoint();

		// proceeding only when we have a full candle.
		if (quoteUpdateCount != 0)
			return;

		// slice ..
		if (opens.size() > period1 + 2) {
			opens.remove(0);
			highs.remove(0);
			lows.remove(0);
			closes.remove(0);
		}

		//
		if (opens.size() < period1 + 1) {
			log.warn("Not enough OHLC datasets, yet: " + opens.size()
					+ " but would need " + (period1 + 1));
			return;
		}

		// processing high and low.
		double p1 = emaAcc.getMeanValue();
		double lastOpen = opens.get(closes.size() - 1);
		double lastClose = closes.get(closes.size() - 1);
		double lastLow = lows.get(closes.size() - 1);
		double lastHigh = highs.get(closes.size() - 1);
		log.info("Calc output: " + p1 + " <> L:" + lastLow + " <> H:"
				+ lastHigh);

		// dump.
		if (lastLow < p1 && p1 < lastHigh) {
			silentSend(lastLow + " < *" + p1 + "* < " + lastHigh);
		} else if (p1 < lastLow) {
			silentSend(" *" + p1 + "* < " + lastLow + " < " + lastHigh);
		} else if (p1 > lastHigh) {
			silentSend(lastLow + " < " + lastHigh + " < *" + p1 + "*");
		}

		// logging the current position's PNL.
		if (currentPosition != 0.0) {
			String text = "[Position PNL] " + currentPositionPnl;
			log.info(text);
			silentSend(text);
		}

		// check if we should trade.
		if (!tradeFlag)
			return;

		//
		if (p1 > 0.0) {
			if (lastLow >  p1 && lastOpen < lastClose && formerDirection != 1) {
				tradeId++;
				ts = new TradeStatistics(System.currentTimeMillis(),"LONG");
				ts.setTradeId(tradeId);
				ts.setSessionId(sessionId);
				log.info("Detecting a long at " + quote.toString());
				formerDirection = 1;
				silentSend("System 5 says long at " + quote.toString());
				silentSend("Former max Pnl: " + maxPnl + "\nFormer min Pnl: "
						+ minPnl);
				setTargetPosition(quote.getTimeStamp(),
						quote.getInstrumentSpecification(), 1,
						quote.getAskPrice());
				partialReset();
			} else if (lastHigh < p1 && lastClose < lastOpen && formerDirection != -1) {
				tradeId++;
				ts = new TradeStatistics(System.currentTimeMillis(),"SHORT");
				ts.setTradeId(tradeId);
				ts.setSessionId(sessionId);
				log.info("Detecting a short at " + quote.toString());
				formerDirection = -1;
				silentSend("System 5 says short at " + quote.toString());
				silentSend("Former max Pnl: " + maxPnl + "\nFormer min Pnl: "
						+ minPnl);
				setTargetPosition(quote.getTimeStamp(),
						quote.getInstrumentSpecification(), -1,
						quote.getBidPrice());
				partialReset();
			}
		}
	}

	public void partialReset() {
		minPnl = 0;
		maxPnl = 0;
	}

	public void populateReport(SimpleReport report) {
	}

	@Override
	public void forcedTradingStop() {

		// // log.info("Forced liquidation");
		quoteList.clear();
		if (formerQuote != null)
			setTargetPosition(formerQuote.getTimeStamp(),
					formerQuote.getInstrumentSpecification(), 0, 0.0);
		formerQuote = null;

		lows.clear();
		opens.clear();
		highs.clear();
		closes.clear();

	}

	private void positionChecks(Quote quote) {
		currentPosition = 0.000;
		currentPositionPnl = 0.0;
		if (getAlgoEnv().getBrokerAccount().getPortfolio()
				.hasPosition(quote.getInstrumentSpecification())) {
			Position pos = getAlgoEnv().getBrokerAccount().getPortfolio()
					.getPosition(quote.getInstrumentSpecification());
			currentPosition = getAlgoEnv().getBrokerAccount().getPortfolio()
					.getPosition(quote.getInstrumentSpecification())
					.getQuantity();
			// get the price difference
			double priceDiff = pos.getPriceDifference(quote);

			// compute the current pnl
			currentPositionPnl = Math.abs(currentPosition) * priceDiff
					/ quote.getInstrumentSpecification().getTickSize()
					* quote.getInstrumentSpecification().getTickValue();
			ts.setLastPnl(currentPositionPnl);
			ts.setLastQuoteTimeStampMs(System.currentTimeMillis());
			log.info("Current position pnl: " + currentPositionPnl);
			if (currentPositionPnl > maxPnl) {
				maxPnl = currentPositionPnl;
				ts.setMaxPnl(maxPnl);
			}
			if (currentPositionPnl < minPnl) {
				minPnl = currentPositionPnl;
				ts.setMinPnl(minPnl);
			}
			tradeStatisticsDao.persist(ts);
			// check if the current pnl is lower than our stop loss pnl
			if (currentPositionPnl < stopLossPnl) {
				double stopLimitPrice = quote.getBidPrice();
				if (currentPosition < 0)
					stopLimitPrice = quote.getAskPrice();
				// liquidate
				silentSend("Stop loss reached: " + currentPositionPnl + " / "
						+ stopLossPnl);
				setTargetPosition(quote.getTimeStamp(),
						quote.getInstrumentSpecification(), 0, stopLimitPrice);
			}
			// log the quote.
			if (getAlgoEnv().getRunMode().equals(RunMode.PRODUCTION))
				pnlLogger3.log(quote);
		}
	}

	private boolean okChecks(Quote quote) {
		// check if it is the first quote we receive.
		if (open == 0.0)
			open = quote.getMidpoint();

		// only 100% sane quotes ...
		if (quote.getBidPrice() == Quote.NOT_SET
				|| quote.getAskPrice() == Quote.NOT_SET)
			return false;

		if (formerQuote != null) {
			if ((quote.getBidPrice() == formerQuote.getBidPrice() && quote
					.getAskPrice() == formerQuote.getAskPrice())
					|| (quote.getBidPrice() == Quote.NOT_SET || quote
							.getAskPrice() == Quote.NOT_SET))
				return false;
		}

		// all fine.
		return true;
	}

}
