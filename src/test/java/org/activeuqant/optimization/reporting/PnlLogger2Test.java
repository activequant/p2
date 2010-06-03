package org.activeuqant.optimization.reporting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.account.Order;
import org.activequant.core.domainmodel.data.Quote;
import org.activequant.core.domainmodel.events.OrderExecutionEvent;
import org.activequant.core.types.OrderSide;
import org.activequant.reporting.PnlLogger2;
import org.junit.Test;



public class PnlLogger2Test {
	
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
	
	@Test 
	public void orderLog() throws IOException {
		PnlLogger2 l = new PnlLogger2(false, "");
		// buy one at 100
		l.log(getOrder(spec(), OrderSide.BUY), getExec(100.0,1.0));
		assertEquals(1, l.getPositionValueSeries(spec()).size());
		assertEquals(1.0, l.getPositionValueSeries(spec()).get(0).getValue(), 0.0001);
		
		// buy one at 101
		l.log(getOrder(spec(), OrderSide.BUY), getExec(101.0,1.0));
		assertEquals(2, l.getPositionValueSeries(spec()).size());
		assertEquals(2.0, l.getPositionValueSeries(spec()).get(1).getValue(), 0.0001);
		
		// check the pnl. 
		assertEquals(2, l.getPnlValueSeries().size());
		assertEquals(1.0, l.getPnlValueSeries().get(1).getValue(), 0.001);
		
		// sell two at 102 
		l.log(getOrder(spec(), OrderSide.SELL), getExec(102.0,2.0));
		assertEquals(3, l.getPnlValueSeries().size());
		assertEquals(3.0, l.getPnlValueSeries().get(2).getValue(), 0.001);
		assertEquals(3, l.getPositionValueSeries(spec()).size());
		assertEquals(0.0, l.getPositionValueSeries(spec()).get(2).getValue(), 0.0001);
		
		
		// sell two more at 103
		l.log(getOrder(spec(), OrderSide.SELL), getExec(103.0,2.0));
		assertEquals(3, l.getPnlValueSeries().size());
		assertEquals(3.0, l.getPnlValueSeries().get(2).getValue(), 0.001);
		assertEquals(4, l.getPositionValueSeries(spec()).size());
		assertEquals(-2.0, l.getPositionValueSeries(spec()).get(3).getValue(), 0.0001);
		
		// buy them back at 100
		l.log(getOrder(spec(), OrderSide.BUY), getExec(100.0,2.0));
		assertEquals(4, l.getPnlValueSeries().size());
		assertEquals(9.0, l.getPnlValueSeries().get(3).getValue(), 0.001);
		assertEquals(5, l.getPositionValueSeries(spec()).size());
		assertEquals(0.0, l.getPositionValueSeries(spec()).get(4).getValue(), 0.0001);
		
		
	}
	


	
	@Test 
	public void quoteLog() throws IOException {
		PnlLogger2 l = new PnlLogger2(false, "");
		// buy one at 100
		l.log(getOrder(spec(), OrderSide.BUY), getExec(100.0,1.0));
		assertEquals(1, l.getPositionValueSeries(spec()).size());
		assertEquals(1.0, l.getPositionValueSeries(spec()).get(0).getValue(), 0.0001);
		
		// track a quote. 
		l.log(getQuote(101, 102));
		assertEquals(2, l.getPnlValueSeries().size());
		assertEquals(1.0, l.getPnlValueSeries().get(1).getValue(), 0.001);
		
		
		// buy one at 101
		l.log(getOrder(spec(), OrderSide.BUY), getExec(101.0,1.0));
		assertEquals(2, l.getPositionValueSeries(spec()).size());
		assertEquals(2.0, l.getPositionValueSeries(spec()).get(1).getValue(), 0.0001);
		
		// check the pnl and length (must be the same, as duplicate values are dropped) 
		assertEquals(2, l.getPnlValueSeries().size());
		assertEquals(1.0, l.getPnlValueSeries().get(1).getValue(), 0.001);
		
		// sell two at 102 
		l.log(getOrder(spec(), OrderSide.SELL), getExec(102.0,2.0));
		assertEquals(3, l.getPnlValueSeries().size());
		assertEquals(3.0, l.getPnlValueSeries().get(2).getValue(), 0.001);
		assertEquals(3, l.getPositionValueSeries(spec()).size());
		assertEquals(0.0, l.getPositionValueSeries(spec()).get(2).getValue(), 0.0001);
		
		
		// sell two more at 103
		l.log(getOrder(spec(), OrderSide.SELL), getExec(103.0,2.0));
		assertEquals(3, l.getPnlValueSeries().size());
		assertEquals(3.0, l.getPnlValueSeries().get(2).getValue(), 0.001);
		assertEquals(4, l.getPositionValueSeries(spec()).size());
		assertEquals(-2.0, l.getPositionValueSeries(spec()).get(3).getValue(), 0.0001);
		
		// buy them back at 100
		l.log(getOrder(spec(), OrderSide.BUY), getExec(100.0,2.0));
		assertEquals(4, l.getPnlValueSeries().size());
		assertEquals(9.0, l.getPnlValueSeries().get(3).getValue(), 0.001);
		assertEquals(5, l.getPositionValueSeries(spec()).size());
		assertEquals(0.0, l.getPositionValueSeries(spec()).get(4).getValue(), 0.0001);
		
		
	}
	

	
}
