package org.activequant.tradesystems;

import java.util.HashMap;

import org.activequant.broker.IOrderTracker;
import org.activequant.container.report.SimpleReport;
import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.account.Order;
import org.activequant.core.domainmodel.account.OrderHistory;
import org.activequant.core.domainmodel.data.Quote;
import org.activequant.core.domainmodel.events.OrderEvent;
import org.activequant.core.domainmodel.events.OrderExecutionEvent;
import org.activequant.core.types.OrderSide;
import org.activequant.core.types.OrderType;
import org.activequant.core.types.TimeStamp;
import org.activequant.optimization.domainmodel.AlgoConfig;
import org.activequant.util.pattern.events.IEventListener;

/**
 * 
 * @author Ghost Rider
 *
 */
public class BasicTradeSystem implements IBatchTradeSystem {

	private HashMap<Order, IOrderTracker> orderTrackers = new HashMap<Order, IOrderTracker>();
	private AlgoConfig algoConfig; 
	private AlgoEnvironment algoEnv; 

	
	public boolean initialize(AlgoEnvironment algoEnv, AlgoConfig algoConfig) {
		this.algoConfig = algoConfig; 
		this.algoEnv = algoEnv;
		return true; 
	}
	
	
	@Override
	public void forcedTradingStop() {
		// doing nothing in this implementation ... 
	}


	@Override
	public void start() {
		// doing nothing in this implementation ... 
	}


	@Override
	public void stop() {
		// doing nothing in this implementation ... 
	}

	
	@Override
	public void onQuote(Quote quote)
	{
		// doing nothing in this implementation ... 
	}
	
	/**
	 * empty implementation.
	 */
	@Override
	public void populateReport(SimpleReport reportObject) {
		// doing nothing implementation
	}


	/**
	 * ======================================= 
	 * 		Helper functions following. 
	 * =======================================
	 */
	

	/**
	 * Helper function to generate a short limit order object. 
	 * @param spec
	 * @param limitPrice
	 * @param quantity
	 * @return
	 */
	public Order shortLimitOrder(InstrumentSpecification spec, double limitPrice, double quantity) {
		Order o = longLimitOrder(spec, limitPrice, quantity);
		o.setOrderSide(OrderSide.SELL);
		return o;
	}

	/**
	 * helper function to generate a long limit order. 
	 * @param spec
	 * @param limitPrice
	 * @param quantity
	 * @return
	 */
	public Order longLimitOrder(InstrumentSpecification spec, double limitPrice, double quantity) {
		Order o = new Order();
		o.setInstrumentSpecification(spec);
		o.setOrderType(OrderType.LIMIT);
		o.setOrderSide(OrderSide.BUY);
		o.setLimitPrice(limitPrice);
		o.setQuantity(quantity);
		return o;
	}
	
	
	/**
	 * Logic description:
	 * 
	 * @param spec
	 * @param tgtPosition
	 * @param limit
	 */
	public void setTargetPosition(TimeStamp timeStamp, InstrumentSpecification spec, int tgtPosition, double limit) {
		
		// send the reporting ..
		getAlgoEnv().getValueReporter().report(timeStamp, "TGTPOS", new Double(tgtPosition));
		
		// 
		double currentPosition = 0.0;
		if (algoEnv.getBrokerAccount().getPortfolio().hasPosition(spec)) {
			currentPosition = algoEnv.getBrokerAccount().getPortfolio().getPosition(spec).getQuantity();
		}

		if (tgtPosition != currentPosition) {
			if (tgtPosition > currentPosition) {
				// have to go further long.
				// check if there is a long order to reach this position active
				// ...
				double positionDifference = tgtPosition - currentPosition;
				for (OrderHistory h : algoEnv.getBrokerAccount().getOrderBook().getOpenHistories()) {
					if (h.getOrder().getOrderSide().equals(OrderSide.BUY)) {
						if (h.getOrder().getQuantity() <= positionDifference) {
							positionDifference = positionDifference - h.getOrder().getQuantity();
							
							// update the order. 
							h.getOrder().setLimitPrice(limit);
							orderTrackers.get(h.getOrder()).update(h.getOrder());
							
						} else {
							orderTrackers.get(h.getOrder()).cancel();
						}
					} else {
						// cancel any sell order.
						orderTrackers.get(h.getOrder()).cancel();
					}

				}
				if (positionDifference > 0) {
					if (limit == 0.0)
						limit = Double.MAX_VALUE;
					Order o = longLimitOrder(spec, limit, positionDifference);
					// // log.info("Long limit order at " + limit);
					IOrderTracker t = algoEnv.getBroker().prepareOrder(o);
					t.getOrderEventSource().addEventListener(new IEventListener<OrderEvent>() {
						@Override
						public void eventFired(OrderEvent event) {
							if (event instanceof OrderExecutionEvent) {
								// log.info("Execution at " + ((OrderExecutionEvent) event).getPrice());
							}
						}
					});
					orderTrackers.put(o, t);
					t.submit();
				}
				

			} else if (tgtPosition < currentPosition) {

				// have to go further long.
				// check if there is a long order to reach this position active
				// ...
				double positionDifference = Math.abs(tgtPosition - currentPosition);
				for (OrderHistory h : algoEnv.getBrokerAccount().getOrderBook().getOpenHistories()) {
					if (h.getOrder().getOrderSide().equals(OrderSide.SELL)) {
						if (h.getOrder().getQuantity() <= positionDifference) {
							positionDifference = positionDifference - h.getOrder().getQuantity();
							// update the order. 
							h.getOrder().setLimitPrice(limit);
							orderTrackers.get(h.getOrder()).update(h.getOrder());
							
						} else {
							orderTrackers.get(h.getOrder()).cancel();
						}
					} else {
						// cancel any sell order.
						orderTrackers.get(h.getOrder()).cancel();
					}

				}
				if (positionDifference > 0) {
					// have to go further short.
					if (limit == 0.0)
						limit = Double.MIN_VALUE;
					Order o = shortLimitOrder(spec, limit, Math.abs(positionDifference));
					// log.info("Short limit order at " + limit);
					IOrderTracker t = algoEnv.getBroker().prepareOrder(o);
					t.getOrderEventSource().addEventListener(new IEventListener<OrderEvent>() {
						@Override
						public void eventFired(OrderEvent event) {
							if (event instanceof OrderExecutionEvent) {
								// log.info("Execution at " + ((OrderExecutionEvent) event).getPrice());
							}
						}
					});
					orderTrackers.put(o, t);
					t.submit();
				}
			}
		}
	}


	public HashMap<Order, IOrderTracker> getOrderTrackers() {
		return orderTrackers;
	}


	public void setOrderTrackers(HashMap<Order, IOrderTracker> orderTrackers) {
		this.orderTrackers = orderTrackers;
	}


	public AlgoConfig getAlgoConfig() {
		return algoConfig;
	}


	public void setAlgoConfig(AlgoConfig algoConfig) {
		this.algoConfig = algoConfig;
	}


	public AlgoEnvironment getAlgoEnv() {
		return algoEnv;
	}


	public void setAlgoEnv(AlgoEnvironment algoEnv) {
		this.algoEnv = algoEnv;
	}


	
}
