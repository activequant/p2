package org.activeuqant.optimization.reporting;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.account.Order;
import org.activequant.core.domainmodel.data.Quote;
import org.activequant.core.domainmodel.events.OrderExecutionEvent;
import org.activequant.core.types.OrderSide;
import org.activequant.core.types.TimeStamp;
import org.activequant.reporting.PnlLogger3;
import org.junit.Test;
import org.activequant.reporting.IValueReporter;


public class PnlLogger3Test {
	
	private static final double delta = 0.0001;
	private InstrumentSpecification spec = new InstrumentSpecification();
	
	private InstrumentSpecification spec()
	{		
		spec.setId(1L);
		return spec; 
	}
	
	
	private OrderExecutionEvent getExec(double price, double quantity)
	{
		OrderExecutionEvent exec = new OrderExecutionEvent();
		exec.setPrice(price);
		exec.setQuantity(quantity);
		return exec;
	}
	
	private Order getOrder(InstrumentSpecification spec, OrderSide side)
	{
		Order order = new Order();
		order.setOrderSide(side);
		order.setInstrumentSpecification(spec);
		return order; 
	}

	private Quote getQuote(double bid, double ask)
	{
		Quote q = new Quote();
		q.setInstrumentSpecification(spec());
		q.setBidPrice(bid);
		q.setAskPrice(ask);
		return q; 
	}
	
	/**	
	 * @author GhostRider
	 */
	private class TestValueReporter implements IValueReporter {
		TimeStamp lastTimeStamp; 
		String lastKey; 
		Double lastValue; 		
		@Override
		public void report(TimeStamp timeStamp, String valueKey, Double value) {
			lastTimeStamp = timeStamp;
			lastKey = valueKey; 
			lastValue = value; 
		}
		@Override
		public void flush() {}		
	}
	
	
	@Test 
	public void orderLog() throws IOException {
		PnlLogger3 l = new PnlLogger3(new TestValueReporter());
		
		// buy one at 100
		l.log(getOrder(spec(), OrderSide.BUY), getExec(100.0,1.0));
		assertEquals(1, l.getPositionValueSeries(spec()).size());
		assertEquals(1.0, l.getPositionValueSeries(spec()).get(0).getValue(), delta);
		
		// buy one at 101
		l.log(getOrder(spec(), OrderSide.BUY), getExec(101.0,1.0));
		assertEquals(2, l.getPositionValueSeries(spec()).size());
		assertEquals(2.0, l.getPositionValueSeries(spec()).get(1).getValue(), delta);
		
		// check the pnl. 
		assertEquals(2, l.getPnlValueSeries().size());
		assertEquals(1.0, l.getPnlValueSeries().get(1).getValue(), delta);
		
		// sell two at 102 
		l.log(getOrder(spec(), OrderSide.SELL), getExec(102.0,2.0));
		assertEquals(3, l.getPnlValueSeries().size());
		assertEquals(3.0, l.getPnlValueSeries().get(2).getValue(), delta);
		assertEquals(3, l.getPositionValueSeries(spec()).size());
		assertEquals(0.0, l.getPositionValueSeries(spec()).get(2).getValue(), delta);
		
		
		// sell two more at 103
		l.log(getOrder(spec(), OrderSide.SELL), getExec(103.0,2.0));
		assertEquals(3, l.getPnlValueSeries().size());
		assertEquals(3.0, l.getPnlValueSeries().get(2).getValue(), delta);
		assertEquals(4, l.getPositionValueSeries(spec()).size());
		assertEquals(-2.0, l.getPositionValueSeries(spec()).get(3).getValue(), delta);
		
		// buy them back at 100
		l.log(getOrder(spec(), OrderSide.BUY), getExec(100.0,2.0));
		assertEquals(4, l.getPnlValueSeries().size());
		assertEquals(9.0, l.getPnlValueSeries().get(3).getValue(), delta);
		assertEquals(5, l.getPositionValueSeries(spec()).size());
		assertEquals(0.0, l.getPositionValueSeries(spec()).get(4).getValue(), delta);
		
		
	}
	

	@Test
	public void logLongTrades() {
		PnlLogger3 l = new PnlLogger3(new TestValueReporter());
		
		l.log(getQuote(100.0, 100.0));
		l.log(getOrder(spec(), OrderSide.BUY), getExec(100.0,1.0));
		l.log(getQuote(100.0, 100.0));
		
		int i = 0; 
		assertEquals(0.0, l.getPnlValueSeries().get(i).getValue(), delta);
	
		i++;
		l.log(getQuote(101.0, 102.0));
		assertEquals(1.0, l.getPnlValueSeries().get(i).getValue(), delta);
		
		i++;
		l.log(getQuote(99.0, 100.0));
		assertEquals(-1.0, l.getPnlValueSeries().get(i).getValue(), delta);
		
		i++;
		l.log(getQuote(90.0, 100.0));
		assertEquals(-10.0, l.getPnlValueSeries().get(i).getValue(), delta);
		
		
	}
	

	@Test
	public void logShortTrades() {
		PnlLogger3 l = new PnlLogger3(new TestValueReporter());
		
		l.log(getQuote(100.0, 100.0));
		l.log(getOrder(spec(), OrderSide.SELL), getExec(100.0,1.0));
		l.log(getQuote(100.0, 100.0));
		
		int i = 0; 
		assertEquals(0.0, l.getPnlValueSeries().get(i).getValue(), delta);
	
		i++;
		l.log(getQuote(101.0, 102.0));
		assertEquals(-2.0, l.getPnlValueSeries().get(i).getValue(), delta);
		
		i++;
		l.log(getQuote(99.0, 100.0));
		assertEquals(0.0, l.getPnlValueSeries().get(i).getValue(), delta);
		
		i++;
		l.log(getQuote(90.0, 90.0));
		assertEquals(10.0, l.getPnlValueSeries().get(i).getValue(), delta);
		
		
	}
	
	
	
	@Test 
	public void quoteLog() throws IOException {
		PnlLogger3 l = new PnlLogger3(new TestValueReporter());
		
		// buy one at 100
		l.log(getOrder(spec(), OrderSide.BUY), getExec(100.0,1.0));
		assertEquals(1, l.getPositionValueSeries(spec()).size());
		assertEquals(1.0, l.getPositionValueSeries(spec()).get(0).getValue(), delta);
		
		// track a quote. 
		l.log(getQuote(101, 102));
		assertEquals(2, l.getPnlValueSeries().size());
		assertEquals(1.0, l.getPnlValueSeries().get(1).getValue(), delta);
		
		
		// buy one at 101
		l.log(getOrder(spec(), OrderSide.BUY), getExec(101.0,1.0));
		assertEquals(2, l.getPositionValueSeries(spec()).size());
		assertEquals(2.0, l.getPositionValueSeries(spec()).get(1).getValue(), delta);
		
		// check the pnl and length (must be the same, as duplicate values are dropped) 
		assertEquals(2, l.getPnlValueSeries().size());
		assertEquals(1.0, l.getPnlValueSeries().get(1).getValue(), delta);
		
		// sell two at 102 
		l.log(getOrder(spec(), OrderSide.SELL), getExec(102.0,2.0));
		assertEquals(3, l.getPnlValueSeries().size());
		assertEquals(3.0, l.getPnlValueSeries().get(2).getValue(), delta);
		assertEquals(3, l.getPositionValueSeries(spec()).size());
		assertEquals(0.0, l.getPositionValueSeries(spec()).get(2).getValue(), delta);
		
		
		// sell two more at 103
		l.log(getOrder(spec(), OrderSide.SELL), getExec(103.0,2.0));
		assertEquals(3, l.getPnlValueSeries().size());
		assertEquals(3.0, l.getPnlValueSeries().get(2).getValue(), delta);
		assertEquals(4, l.getPositionValueSeries(spec()).size());
		assertEquals(-2.0, l.getPositionValueSeries(spec()).get(3).getValue(), delta);
		
		// buy them back at 100
		l.log(getOrder(spec(), OrderSide.BUY), getExec(100.0,2.0));
		assertEquals(4, l.getPnlValueSeries().size());
		assertEquals(9.0, l.getPnlValueSeries().get(3).getValue(), delta);
		assertEquals(5, l.getPositionValueSeries(spec()).size());
		assertEquals(0.0, l.getPositionValueSeries(spec()).get(4).getValue(), delta);
		
		
	}
	

	
}
