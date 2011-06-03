package org.activequant.reporting;

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
 * @author GhostRider
 * 
 */
public class PnlLogger3 extends LoggerBase {

	private int quoteCount = 0;
	private double pnl = 0.0;
	private ValueSeries pnlValueSeries = new ValueSeries();
	private HashMap<InstrumentSpecification, ValueSeries> instrumentPnlValueSeries = new HashMap<InstrumentSpecification, ValueSeries>();
	private HashMap<InstrumentSpecification, ValueSeries> positionValueSeries = new HashMap<InstrumentSpecification, ValueSeries>();
	private IValueReporter valueReporter; 

	/**
	 * PNL Logger constructor that can take a value reporter where to report the values over.  
	 * @param valueReporter
	 */
	public PnlLogger3(IValueReporter valueReporter) {
		this.valueReporter = valueReporter; 
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
	
	public void log(Order order, OrderEvent event) {
		
		if (event instanceof OrderExecutionEvent) {
			OrderExecutionEvent executionEvent = (OrderExecutionEvent) event;
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
				double formerValuationPrice = theInstrumentValuationPrices.get(iid);
				double change = (executionEvent.getPrice() - formerValuationPrice) * myFormerPosition;
				double cashChange = (change * order.getInstrumentSpecification().getTickSize())
						* order.getInstrumentSpecification().getTickValue();
				addPnlValue(executionEvent.getEventTimeStamp(), pnl + cashChange);
			} else {
				addPnlValue(executionEvent.getEventTimeStamp(), pnl);
			}
			double newPosition = myFormerPosition;
			// update the last price as well as the current position
			if (order.getOrderSide().equals(OrderSide.BUY)) {
				newPosition = myFormerPosition + executionEvent.getQuantity();

			} else {
				newPosition = myFormerPosition - Math.abs(executionEvent.getQuantity());
			}
			thePositions.put(iid, newPosition);
			addPositionValue(order.getInstrumentSpecification(), event.getEventTimeStamp(), newPosition);
			theInstrumentValuationPrices.put(iid, executionEvent.getPrice());
			valueReporter.report(event.getEventTimeStamp(), "POSITION",newPosition);
		}				
	}

	public void log(Quote quote) {
		long iid = 0;
		if(quote.getInstrumentSpecification()!=null)
		{
			if(quote.getInstrumentSpecification().getId()!=null)
				iid = quote.getInstrumentSpecification().getId();
		}
		
		if (!thePositions.containsKey(iid))
			thePositions.put(iid, 0.0);

		// update the position count.
		double currentPosition = thePositions.get(iid);
		valueReporter.report(quote.getTimeStamp(), "POSITION",currentPosition);
		if (currentPosition != 0.0) {
			double myLastPrice = theInstrumentValuationPrices.get(iid);
			double myRelevantQuotePrice = currentPosition > 0 ? quote.getBidPrice() : quote.getAskPrice();
			double myChangeInPrice = myRelevantQuotePrice - myLastPrice;
			
			//
			double myChangeInPnl = myChangeInPrice * currentPosition;
			
			// compute the number of ticks ... 
			pnl += (myChangeInPnl / quote.getInstrumentSpecification().getTickSize()) 
					* quote.getInstrumentSpecification().getTickValue();
			
			// track the current valuation price.
			theInstrumentValuationPrices.put(iid, myRelevantQuotePrice);
			
			// 			
			quoteCount++;
		} else {
			quoteCount = 0;
		}
		addPnlValue(quote.getTimeStamp(), pnl);	
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
		
		valueReporter.report(ts, "PNL", val);
	}


	private void addPositionValue(InstrumentSpecification spec, TimeStamp timestamp, double position) {
		if (!positionValueSeries.containsKey(spec)) {
			positionValueSeries.put(spec, new ValueSeries());
		}
		positionValueSeries.get(spec).add(new TimedValue(timestamp, position));

	}

	private void addInstrumentPnlValue(InstrumentSpecification spec, TimeStamp timestamp, double position) {

	}
	

}
