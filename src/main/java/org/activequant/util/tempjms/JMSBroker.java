package org.activequant.util.tempjms;

import java.util.HashMap;

import org.activequant.broker.BrokerBase;
import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.account.Order;
import org.activequant.core.domainmodel.events.OrderExecutionEvent;
import org.activequant.core.domainmodel.events.OrderSubmitEvent;
import org.activequant.core.domainmodel.events.OrderUpdateEvent;
import org.activequant.core.types.OrderSide;
import org.activequant.core.types.OrderType;
import org.activequant.core.types.TimeStamp;
import org.activequant.data.retrieval.IQuoteSubscriptionSource;
import org.activequant.data.retrieval.ISubscription;
import org.activequant.util.pattern.events.IEventListener;
import org.activequant.util.tools.UniqueDateGenerator;
import org.activequant.util.SpecResolver;
import org.activequant.core.domainmodel.InstrumentSpecification; 
import org.activequant.broker.BrokerBase; 
import org.activequant.core.types.SecurityType;
import org.activequant.core.types.Currency;
import org.activequant.util.pattern.events.Event;
import java.util.HashMap;
/**
 * a jms broker implementation 
 */
class JMSBroker extends BrokerBase {

	private SpecResolver specResolver = new SpecResolver();
	private HashMap<String, OrderTracker> orderTrackers = new HashMap<String, OrderTracker>();
	private Event<OrderExecutionEvent> unknownExecutions = new Event<OrderExecutionEvent>();

	
	class OrderTracker extends OrderTrackerBase {
		private final long orderId=0L; 
		private Order localOrder; 
		private double openQuantity; 

		public OrderTracker(Order order) {
			super(order);
			localOrder = new Order(order);
			//orderId = getNextOrderId();
			openQuantity = order.getQuantity();
		}
		protected String handleSubmit() {return "";}

		public void handleUpdate(Order newOrder) {}

		public void handleCancel() {}

		private void fireExecution(double quantity, double price) {
			
		}


	}


	private void handleLine(String line)
	{
		
		String[] lineParts = line.split(";");
		String type=lineParts[0];
		if(type.equals("OE"))
		{
			// order fill
			String[] instrumentParts = lineParts[1].split(",");
			InstrumentSpecification spec = specResolver.getSpec(instrumentParts[0], instrumentParts[1], instrumentParts[2], instrumentParts[3]);
			// extract the native order id
			String orderId = lineParts[2];
			String fillTicks = lineParts[3];
			String fillAmount = lineParts[4];
			// find the corresponding AQ order id and the corresponding order tracker ... 
			OrderTracker ot = orderTrackers.get(orderId);
			if(ot!=null)
			{
				ot.fireExecution(Double.parseDouble(fillTicks), Double.parseDouble(fillAmount));
			}
			else fireUnknownExecution(spec, Double.parseDouble(fillTicks), Double.parseDouble(fillAmount));	
		}
		else if(type.equals("OU"))
		{
			// order update
		}
		else if(type.equals("OC"))
		{
			// order cancellation 
		}
	
	}

	private void fireUnknownExecution(InstrumentSpecification spec, double quantity, double price)
	{
		//
		OrderExecutionEvent oe = new OrderExecutionEvent();
		oe.setPrice(price);
		oe.setQuantity(quantity);
		unknownExecutions.fire(oe);
	}

	@Override
	protected OrderTracker createOrderTracker(Order order) {
		return new OrderTracker(order);
	}


}

