package org.activequant.tradesystems.s15;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;

import org.activequant.container.report.SimpleReport;
import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.data.Quote;
import org.activequant.optimization.domainmodel.AlgoConfig;
import org.activequant.production.InMemoryAlgoEnvConfigRunner;
import org.activequant.tradesystems.AlgoEnvironment;
import org.activequant.tradesystems.BasicTradeSystem;
import org.activequant.util.LimitedQueue;
import org.activequant.util.spring.ServiceLocator;
import org.apache.log4j.Logger;

public class S15 extends BasicTradeSystem {

	private static Logger pnllog = Logger.getLogger("pnl");
	private static Logger log = Logger.getLogger(S15.class);
	private SystemState ss = new SystemState();
	private InstrumentSpecification spec1, spec2;
	private EmaAcc emaAcc1, emaAcc2;
	private int direction = 0;
	private DecimalFormat df = new DecimalFormat("##.#####");
	private int directionCount = 0;
	private int factor1 = 100, factor2 = 1000; 
	//private int factor1 = 1, factor2 = 1;

	@Override
	public boolean initialize(AlgoEnvironment algoEnv, AlgoConfig algoConfig) {
		super.initialize(algoEnv, algoConfig);
		// load the system state.
		// loadState();
		ss.setPeriod1((Integer) algoConfig.get("period1"));
		ss.setPeriod2((Integer) algoConfig.get("period2"));
		if (emaAcc1 == null)
			emaAcc1 = new EmaAcc(ss.getPeriod1());
		if (emaAcc2 == null)
			emaAcc2 = new EmaAcc(ss.getPeriod2());

		if (ss.getShortRatioQueue() == null) {
			ss.setShortRatioQueue(new LimitedQueue<Double>(ss.getPeriod1()));
			ss.setLongRatioQueue(new LimitedQueue<Double>(ss.getPeriod2()));
		}

		spec1 = getAlgoEnv().getInstrumentSpecs().get(0);
		spec2 = getAlgoEnv().getInstrumentSpecs().get(1);
		System.out.printf("Initialized with %d, %d\n", ss.getPeriod1(),
				ss.getPeriod2());
		return true;
	}

	private void loadState() {
		try {
			ObjectInputStream oin = new ObjectInputStream(new FileInputStream(
					"s15.state"));
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

	int i = 0;

	@Override
	public void onQuote(Quote quote) {
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
		ss.incQuoteCount();
		log.debug("Current quote count: " + ss.getQuoteCount());
		if ((ss.getQuoteCount()) % 25 != 0) {
			saveState();
			return;
		}

		double ratio = ss.getMp1() - ss.getMp2();

		ss.getShortRatioQueue().add(ratio);
		ss.getLongRatioQueue().add(ratio);
		emaAcc1.eacc2(ratio);
		emaAcc2.eacc2(ratio);

		String text = "VALUELOG:" + quote.getTimeStamp() + ";"
				+ df.format(ratio) + ";" + ss.getShortRatioQueue().size() + ";"
				+ ss.getLongRatioQueue().size();

		if (ss.getLongRatioQueue().isFull()) {
			// full queues means full ratio!
			double fastEma = emaAcc1.getValue();
			double slowEma = emaAcc2.getValue();
			if (ss.getSmoothedRatio() != null) {
				text += ";" + df.format(ss.getSmoothedRatio().doubleValue())
						+ ";" + df.format(fastEma) + ";";

				if ((slowEma - fastEma) > 0.0) {

					if (directionCount > 0)
						directionCount = 0;
					directionCount--;

					// short.
					// log.info("Short Spec1/Long Spec2");
					text += "-1;";

					setTargetPosition(quote.getTimeStamp(), spec1, -1,
							ss.getBid1() * factor1 - 0.02 * factor1);
					setTargetPosition(quote.getTimeStamp(), spec2, 1,
							ss.getAsk2() * factor2 + 0.02 * factor2);
					if (direction != -1 && directionCount < 0) {
						ss.setEntryPrice1(ss.getBid1());
						ss.setEntryPrice2(ss.getAsk2());
						direction = -1;
						ss.setPnl(0);
					}

				} else if ((fastEma - slowEma) > 0.0) {

					if (directionCount < 0)
						directionCount = 0;
					directionCount++;
					// long
					// log.info("Long Spec1/Short Spec2");
					text += "1;";
					setTargetPosition(quote.getTimeStamp(), spec1, 1,
							ss.getAsk1() * factor1 + 0.02 * factor1);
					setTargetPosition(quote.getTimeStamp(), spec2, -1,
							ss.getBid2() * factor2 - 0.02 * factor2);
					if (direction != 1 && directionCount > 0) {
						ss.setEntryPrice1(ss.getAsk1());
						ss.setEntryPrice2(ss.getBid2());
						direction = 1;
						ss.setPnl(0);
					}
				} else {
					text += "0;";
				}
			}
			ss.setSmoothedRatio(fastEma);
		}
		if (ss.getEntryPrice1() != null) {
			if (direction > 0) {

				ss.setPnl(ss.getBid1() - ss.getEntryPrice1()
						+ ss.getEntryPrice2() - ss.getAsk2());

			} else if (direction < 0) {

				ss.setPnl(ss.getEntryPrice1() - ss.getAsk1() + ss.getBid2()
						- ss.getEntryPrice2());

			}
			text += "" + df.format(ss.getPnl()) + ";"
					+ df.format(ss.getTotalPnl());
		}
		log.info(text);
		saveState();
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
		InMemoryAlgoEnvConfigRunner runner = (InMemoryAlgoEnvConfigRunner) ServiceLocator
				.instance("data/inmemoryalgoenvrunner.xml").getContext()
				.getBean("runner");
		runner.init("org.activequant.tradesystems.s15.AlgoEnvConfigS15");

	}

}
