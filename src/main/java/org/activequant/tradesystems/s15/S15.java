package org.activequant.tradesystems.s15;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;

import org.activequant.container.report.SimpleReport;
import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.data.Quote;
import org.activequant.core.types.TimeStamp;
import org.activequant.optimization.domainmodel.AlgoConfig;
import org.activequant.production.InMemoryAlgoEnvConfigRunner;
import org.activequant.tradesystems.AlgoEnvironment;
import org.activequant.tradesystems.BasicTradeSystem;
import org.activequant.util.FinancialLibrary2;
import org.activequant.util.LimitedQueue;
import org.activequant.util.spring.ServiceLocator;
import org.apache.log4j.Logger;

public class S15 extends BasicTradeSystem {

	private static Logger log = Logger.getLogger(S15.class);
	private SystemState ss = new SystemState();
	private InstrumentSpecification spec1, spec2;

	private int direction = 0;
	private DecimalFormat df = new DecimalFormat("##.#####");

	// private int factor1 = 100, factor2 = 1000;
	private int priceFactor1 = 1, priceFactor2 = 1;
	private double slippage = 0.03;
	private boolean firstWrite = true;

	private double currentMajorTrend = 0;
	private boolean majorUpTrend = false, majorDownTrend = false;
	private double position1 = 0.0, position2 = 0.0;

	// /// ----- parameter start
	private int period1 = 200, period2 = 3, period3 = 50, period4 = 50, period5 = 100;
	private double sdFactor = 1.5;
	private int maxPos = 3;
	private int recalcEveryXQuotes = 100; 
	// /// ----- parameter end

	private LimitedQueue<Double> slowQueue = new LimitedQueue<Double>(2);
	private LimitedQueue<Double> fastQueue = new LimitedQueue<Double>(2);
	private LimitedQueue<Double> bollingerQueue = new LimitedQueue<Double>(period4);

	private LimitedQueue<Double> slowMajorTrendQueue = new LimitedQueue<Double>(period1);
	private LimitedQueue<Double> fastMajorTrendQueue = new LimitedQueue<Double>(period5);

	private EmaAcc slowMajorTrend = new EmaAcc(period1);
	private EmaAcc fastMajorTrend = new EmaAcc(period5);
	private EmaAcc emaAcc1 = new EmaAcc(period2), emaAcc2 = new EmaAcc(period3);

	private void appendToValueLog(String text) {
		try {
			FileWriter fstream;
			if (firstWrite) {
				fstream = new FileWriter(new File("/home/knoppix/value.txt"));
			} else
				fstream = new FileWriter("/home/knoppix/value.txt", true);
			firstWrite = false;
			BufferedWriter out = new BufferedWriter(fstream);

			out.write(text.replaceAll(",", "."));
			out.newLine();
			out.flush();
			// Close the output stream
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public boolean initialize(AlgoEnvironment algoEnv, AlgoConfig algoConfig) {
		super.initialize(algoEnv, algoConfig);
		// load the system state.
		// loadState();
		ss.setPeriod1((Integer) algoConfig.get("period1"));
		ss.setPeriod2((Integer) algoConfig.get("period2"));

		if (ss.getFastRatioEmaQueue() == null) {
			ss.setFastRatioEmaQueue(new LimitedQueue<Double>(period3));
			ss.setSlowRatioEmaQueue(new LimitedQueue<Double>(period3));
		}

		spec1 = getAlgoEnv().getInstrumentSpecs().get(0);
		spec2 = getAlgoEnv().getInstrumentSpecs().get(1);
		System.out.printf("Initialized with %d, %d\n", ss.getPeriod1(), ss.getPeriod2());
		return true;
	}

	private void loadState() {
		try {
			ObjectInputStream oin = new ObjectInputStream(new FileInputStream("s15.state"));
			ss = (SystemState) oin.readObject();
			oin = new ObjectInputStream(new FileInputStream("s15.ema.state"));
			emaAcc1 = (EmaAcc) oin.readObject();
			log.info("Former system states loaded successfully.");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void copy(String f, String t) throws Exception {
		FileInputStream from = null;
		FileOutputStream to = null;
		try {
			from = new FileInputStream(f);
			to = new FileOutputStream(t);
			byte[] buffer = new byte[4096];
			int bytesRead;

			while ((bytesRead = from.read(buffer)) != -1)
				to.write(buffer, 0, bytesRead); // write
		} finally {
			if (from != null)
				try {
					from.close();
				} catch (Exception e) {
					;
				}
			if (to != null)
				try {
					to.close();
				} catch (Exception e) {
					;
				}

		}
	}

	private void saveState() {
		/*
		 * try{ // copy the former state to a backup state. copy("s15.state",
		 * "s15.state.0"); copy("s15.ema.state", "s15.ema.state.0");
		 * ObjectOutputStream oout = new ObjectOutputStream(new
		 * FileOutputStream("s15.state")); oout.writeObject(ss); oout = new
		 * ObjectOutputStream(new FileOutputStream("s15.ema.state"));
		 * oout.writeObject(emaAcc); } catch(Exception ex){
		 * log.warn("Error loading data. ", ex); }
		 */
	}

	
	
	private String processSignals(boolean longEntry, boolean longExit, boolean shortEntry, boolean shortExit, TimeStamp timeStamp)
	{
		String text = ""; 
		if (longExit) {
			setTargetPosition(timeStamp, spec1, 0, ss.getBid1() * priceFactor1 - slippage * priceFactor1);
			setTargetPosition(timeStamp, spec2, 0, ss.getAsk2() * priceFactor2 + slippage * priceFactor2);

			ss.setEntryPrice1(ss.getBid1());
			ss.setEntryPrice2(ss.getAsk2());
			direction = 0;
			position1 = 0;
			position2 = 0;
			ss.setPnl(0);
			// if (!shortEntry && !longEntry)
			// text += "0;";

		} else if (shortExit) {
			setTargetPosition(timeStamp, spec1, 0, ss.getAsk1() * priceFactor1 + slippage * priceFactor1);
			setTargetPosition(timeStamp, spec2, 0, ss.getBid2() * priceFactor2 - slippage * priceFactor2);
			ss.setEntryPrice1(ss.getAsk1());
			ss.setEntryPrice2(ss.getBid2());
			position1 = 0;
			position2 = 0;
			direction = 0;
			ss.setPnl(0);
			// if (!shortEntry && !longEntry)
			// text += "0;";
		}
		//
		//
		//
		if (longEntry & position1 < maxPos & position2 > -maxPos) {
			setTargetPosition(timeStamp, spec1, (int) position1 + 1, ss.getAsk1() * priceFactor1 + slippage * priceFactor1);
			setTargetPosition(timeStamp, spec2, (int) position2 - 1, ss.getBid2() * priceFactor2 - slippage * priceFactor2);
			ss.setEntryPrice1(ss.getAsk1());
			ss.setEntryPrice2(ss.getBid2());
			direction = 1;
			ss.setPnl(0);
			text += "1;";
		} else if (shortEntry & position1 > -maxPos & position2 < maxPos) {

			setTargetPosition(timeStamp, spec1, (int) position1 - 1, ss.getBid1() * priceFactor1 - slippage * priceFactor1);
			setTargetPosition(timeStamp, spec2, (int) position2 + 1, ss.getAsk2() * priceFactor2 + slippage * priceFactor2);

			ss.setEntryPrice1(ss.getBid1());
			ss.setEntryPrice2(ss.getAsk2());
			direction = -1;
			ss.setPnl(0);
			text += "-1;";

		} else
			text += "0;";

		return text; 
	}
	
	
	int i = 0;

	@Override
	public void onQuote(Quote quote) {
		boolean shortEntry = false, shortExit = false, longEntry = false, longExit = false;

		pnlLogger.log(quote);
		// System.out.println(quote.toString());
		long id = quote.getInstrumentSpecification().getId();
		long id1 = spec1.getId();
		long id2 = spec2.getId();
		double mp1 = ss.getMp1();
		double mp2 = ss.getMp2();
		if (id == id1) {
			ss.setBid1(quote.getBidPrice());
			ss.setAsk1(quote.getAskPrice());
			if (quote.getMidpoint() == ss.getMp1())
				return;
			ss.setMp1(quote.getMidpoint());
		}
		if (id == id2) {
			ss.setBid2(quote.getBidPrice());
			ss.setAsk2(quote.getAskPrice());
			if (quote.getMidpoint() == ss.getMp2())
				return;
			ss.setMp2(quote.getMidpoint());
		}


		//
		position1 = 0;
		position2 = 0;
		// dump position
		if (getAlgoEnv().getBrokerAccount().getPortfolio().hasPosition(spec1))
			position1 = getAlgoEnv().getBrokerAccount().getPortfolio().getPosition(spec1).getQuantity();
		if (getAlgoEnv().getBrokerAccount().getPortfolio().hasPosition(spec2))
			position2 = getAlgoEnv().getBrokerAccount().getPortfolio().getPosition(spec2).getQuantity();

		updateCurrentPnl();

		// check stop loss
		if (position1 < 0 && (ss.getPnl() < -0.2 || ss.getPnl() > 1.0)) {
			shortExit = true;
			shortEntry = longExit = longEntry = false;
		}

		if (position1 > 0 && (ss.getPnl() < -0.2 || ss.getPnl() > 1.0)) {
			longExit = true;
			shortEntry = shortExit = longEntry = false;
		}
		// terminate.
		processSignals(longEntry, longExit, shortEntry, shortExit, quote.getTimeStamp());
		
		// reset all the liquidation flags. 
		shortExit = longExit = false; 

		// check if we should continue.
		ss.incQuoteCount();
		log.debug("Current quote count: " + ss.getQuoteCount());
		if ((ss.getQuoteCount()) % recalcEveryXQuotes != 0) {
			saveState();
			return;
		}
		double ratio = ss.getMp1() - ss.getMp2();
		processRatio(ratio);

		String text = "VALUELOG;" + quote.getTimeStamp() + ";" + df.format(ratio);

		// System.out.println(quote.getTimeStamp().getDate());
		if (ss.getSlowRatioEmaQueue().isFull()) {
			// full queues means full ratio!
			double fastEma = emaAcc1.getValue();
			double slowEma = emaAcc2.getValue();
			fastQueue.add(fastEma);
			slowQueue.add(slowEma);
			bollingerQueue.add(fastEma - slowEma);

			if (ss.getSmoothedRatio() != null && fastQueue.isFull() && bollingerQueue.isFull() && slowMajorTrendQueue.isFull()) {

				double formerMajorTrend = currentMajorTrend;
				currentMajorTrend = slowMajorTrend.getValue();
				text += ";" + df.format(ss.getSmoothedRatio().doubleValue()) + ";" + df.format(fastEma) + ";" + df.format(currentMajorTrend) + ";"
						+ df.format(fastMajorTrend.getValue()) + ";";

				majorUpTrend = false;
				majorDownTrend = false;

				boolean intermediateUpTrend = false;
				boolean intermediateDownTrend = false;

				if (currentMajorTrend > formerMajorTrend && fastMajorTrend.getValue() > currentMajorTrend)
					majorUpTrend = true;
				if (currentMajorTrend < formerMajorTrend && fastMajorTrend.getValue() < currentMajorTrend)
					majorDownTrend = true;

				if (fastMajorTrend.getValue() > currentMajorTrend)
					intermediateUpTrend = true;
				if (fastMajorTrend.getValue() < currentMajorTrend)
					intermediateDownTrend = true;

				double formerDiff = fastQueue.get(0) - slowQueue.get(0);
				double currentDiff = fastQueue.get(1) - slowQueue.get(1);

				Double[] ratios = bollingerQueue.toArray(new Double[] {});
				double sd = FinancialLibrary2.deviation(ratios);
				double mean = FinancialLibrary2.mean(ratios);

				// check the conditions.

				//

				if (majorUpTrend) {
					if (position1 < 0 && (ss.getPnl() < -0.2 || ss.getPnl() > 10.5)) {
						shortExit = true;
						shortEntry = longExit = longEntry = false;
					}

					if (formerDiff <= (mean - sdFactor * sd) && currentDiff > (mean - sdFactor * sd) & majorUpTrend) { // &&
						longEntry = true;
						if (position1 < 0)
							shortExit = true;
					}

				} else if (majorDownTrend) {
					if (position1 > 0 && (ss.getPnl() < -0.2 || ss.getPnl() > 10.5)) {
						longExit = true;
						shortEntry = shortExit = longEntry = false;
					}
					if (formerDiff >= (mean + sdFactor * sd) && currentDiff < (mean + sdFactor * sd) & majorDownTrend) {
						shortEntry = true;
						if (position1 > 0)
							longExit = true;
					}
				}

				text += processSignals(longEntry, longExit, shortEntry, shortExit, quote.getTimeStamp());
				
				
			} else
				text += "0;0;0;0;0;0;0;0;0;0;";
			ss.setSmoothedRatio(slowEma);

		} else {
			text += "0;0;0;0;0;0;0;0;0;0;";
		}

		text += "" + df.format(ss.getPnl()) + ";" + df.format(ss.getTotalPnl()) + ";";

		// dump position
		text += "" + df.format(position1) + ";";
		text += "" + df.format(position2) + ";";
		text += df.format(pnlLogger.getPnl()) + ";";

		appendToValueLog(text);
		// log.info(text);
		saveState();
	}

	private void processRatio(double ratio) {
		ss.getFastRatioEmaQueue().add(ratio);
		ss.getSlowRatioEmaQueue().add(ratio);
		emaAcc1.eacc2(ratio);
		emaAcc2.eacc2(ratio);
		slowMajorTrend.eacc2(ratio);
		fastMajorTrend.eacc2(ratio);
		slowMajorTrendQueue.add(ratio);
		fastMajorTrendQueue.add(ratio);
	}

	private void updateCurrentPnl() {
		// calculate the latest pnl.
		if (ss.getEntryPrice1() != null) {
			if (direction > 0) {
				ss.setPnl(position1 * (ss.getBid1() - ss.getEntryPrice1()) + -position2 * (ss.getEntryPrice2() - ss.getAsk2()));
			} else if (direction < 0) {
				ss.setPnl(-position1 * (ss.getEntryPrice1() - ss.getAsk1()) + position2 * (ss.getBid2() - ss.getEntryPrice2()));
			}		
		}
	}

	public void populateReport(SimpleReport report) {
	}

	@Override
	public void forcedTradingStop() {

	}

	@Override
	public void stop() {
		System.out.println("Stoppping S15.");
		super.stop();
		saveState();
	}

	public static void main(String[] args) throws Exception {
		InMemoryAlgoEnvConfigRunner runner = (InMemoryAlgoEnvConfigRunner) ServiceLocator.instance("data/inmemoryalgoenvrunner.xml").getContext()
				.getBean("runner");
		runner.init("org.activequant.tradesystems.s15.AlgoEnvConfigS15");

	}

}
