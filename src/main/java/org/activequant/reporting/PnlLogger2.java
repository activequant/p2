package org.activequant.reporting;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.account.Order;
import org.activequant.core.domainmodel.data.Quote;
import org.activequant.core.domainmodel.data.TimedValue;
import org.activequant.core.domainmodel.data.ValueSeries;
import org.activequant.core.domainmodel.events.OrderEvent;
import org.activequant.core.domainmodel.events.OrderExecutionEvent;
import org.activequant.core.types.OrderSide;
import org.activequant.core.types.TimeStamp;
import org.activequant.util.log.LoggerBase;

/**
 * 
 * @author Ulrich Staudinger
 * 
 */
public class PnlLogger2 extends LoggerBase {

	private int quoteCount = 0;
	private double pnl = 0.0;
	private ValueSeries pnlValueSeries = new ValueSeries();
	private HashMap<InstrumentSpecification, ValueSeries> instrumentPnlValueSeries = new HashMap<InstrumentSpecification, ValueSeries>();
	private HashMap<InstrumentSpecification, ValueSeries> positionValueSeries = new HashMap<InstrumentSpecification, ValueSeries>();
	private BufferedWriter bw = null;

	public PnlLogger2(boolean createFullLog, String fileName) throws IOException {
		if (createFullLog)
			bw = new BufferedWriter(new FileWriter(fileName));
	}

	public void write(String s) {
		if (bw != null)
			try {
				bw.write(s);
				bw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

	}

	private void addPositionValue(InstrumentSpecification spec, TimeStamp timestamp, double position) {
		if (!positionValueSeries.containsKey(spec)) {
			positionValueSeries.put(spec, new ValueSeries());
		}
		positionValueSeries.get(spec).add(new TimedValue(timestamp, position));
	}

	private void addInstrumentPnlValue(InstrumentSpecification spec, TimeStamp timestamp, double position) {

	}

	public void log(Order order, OrderEvent event) {
		write(order.toString() + "///" + event.toString());
		if (event instanceof OrderExecutionEvent) {
			OrderExecutionEvent myEvent = (OrderExecutionEvent) event;
			long iid = order.getInstrumentSpecification().getId();
			// as it is an order execution, we also update the running positions
			// and
			// position values.
			if (!thePositions.containsKey(iid)) {
				thePositions.put(iid, 0.0);
			}
			// update the position count.
			double myFormerPosition = thePositions.get(iid);
			if (myFormerPosition != 0.0) {
				// track the change.
				double myFormerValuationPrice = theInstrumentValuationPrices.get(iid);
				double myChange = (myEvent.getPrice() - myFormerValuationPrice) * myFormerPosition;
				double myCashChange = (myChange * order.getInstrumentSpecification().getTickSize())
						* order.getInstrumentSpecification().getTickValue();
				addPnlValue(myEvent.getEventTimeStamp(), pnl + myCashChange);
			} else {
				addPnlValue(myEvent.getEventTimeStamp(), pnl);
			}
			double myNewPosition = myFormerPosition;
			// update the last price as well as the current position
			if (order.getOrderSide().equals(OrderSide.BUY)) {
				myNewPosition = myFormerPosition + myEvent.getQuantity();

			} else {
				myNewPosition = myFormerPosition - Math.abs(myEvent.getQuantity());
			}
			thePositions.put(iid, myNewPosition);
			addPositionValue(order.getInstrumentSpecification(), event.getEventTimeStamp(), myNewPosition);
			theInstrumentValuationPrices.put(iid, myEvent.getPrice());
			write(";currentPosition=" + myNewPosition);
		}
		write("\n");
	}

	public void log(Quote quote) {
		write(quote.toString());

		long iid = 0;
		if(quote.getInstrumentSpecification()!=null)
		{
			if(quote.getInstrumentSpecification().getId()!=null)
				iid = quote.getInstrumentSpecification().getId();
		}
		

		if (!thePositions.containsKey(iid))
			thePositions.put(iid, 0.0);

		// update the position count.
		double myCurrentPosition = thePositions.get(iid);
		write(";currentPosition=" + myCurrentPosition);
		if (myCurrentPosition != 0.0) {
			double myLastPrice = theInstrumentValuationPrices.get(iid);
			double myRelevantQuotePrice = myCurrentPosition > 0 ? quote.getBidPrice() : quote.getAskPrice();
			double myChangeInPrice = myCurrentPosition > 0 ? (quote.getBidPrice() - myLastPrice) : (myLastPrice - quote.getAskPrice());
			//
			double myChangeInPnl = myChangeInPrice * Math.abs(myCurrentPosition);
			pnl += (myChangeInPnl / quote.getInstrumentSpecification().getTickSize()) * quote.getInstrumentSpecification().getTickValue();
			// track the current valuation price.
			theInstrumentValuationPrices.put(iid, myRelevantQuotePrice);
			// 			
			quoteCount++;
		} else {
			quoteCount = 0;
		}
		addPnlValue(quote.getTimeStamp(), pnl);
		write(";pnlValue=" + pnl);
		write("\n");
	}

	private void addPnlValue(TimeStamp ts, double val) {
		pnl = val;
		TimedValue refVal = null;
		if (pnlValueSeries.size() > 0) {
			refVal = pnlValueSeries.lastElement();
		}
		TimedValue myValue = new TimedValue(ts, val);

		if (refVal == null || refVal.getValue() != val)
			pnlValueSeries.add(myValue);
	}

	/**
	 * the getter for the pnl value series.
	 * 
	 * @return
	 */
	public ValueSeries getPnlValueSeries() {
		return pnlValueSeries;
	}

	public ValueSeries getPositionValueSeries(InstrumentSpecification spec) {
		ValueSeries vs = positionValueSeries.get(spec);
		// sanity. 
		if(vs==null)
			vs = new ValueSeries();
		return vs; 
	}

}
