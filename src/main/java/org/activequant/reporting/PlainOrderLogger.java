package org.activequant.reporting;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.activequant.core.domainmodel.account.Order;
import org.activequant.core.domainmodel.data.Quote;
import org.activequant.core.domainmodel.events.OrderAcceptEvent;
import org.activequant.core.domainmodel.events.OrderCancelEvent;
import org.activequant.core.domainmodel.events.OrderEvent;
import org.activequant.core.domainmodel.events.OrderExecutionEvent;
import org.activequant.core.domainmodel.events.OrderRejectEvent;
import org.activequant.util.log.LoggerBase;

/**
 * This logger logs only orders and order events.
 * 
 * @author Ulrich Staudinger
 * 
 */
public class PlainOrderLogger extends LoggerBase {

	private BufferedWriter bw = null;

	public PlainOrderLogger(String fileName) throws IOException {
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

	public void log(Order order, OrderEvent event) {
		write(event.getEventTimeStamp().getNanoseconds()+";");
		write(order.getInstrumentSpecification().getId()+";");
		write(order.getOrderSide()+";");
		write(order.getOrderType()+";");
		write(order.getQuantity()+";");
		write(order.getLimitPrice()+";");
		
		if (event instanceof OrderExecutionEvent) {
			OrderExecutionEvent myEvent = (OrderExecutionEvent) event;
			write("EXECUTION;");
			write(myEvent.getPrice()+";");
			write(myEvent.getQuantity()+";");
			write(myEvent.getCommission()+";");
			write(myEvent.getMessage()+";");
		}
		else if (event instanceof OrderAcceptEvent) {
			OrderAcceptEvent myEvent = (OrderAcceptEvent) event;
			write("ACCEPT;");			
			write(event.getMessage()+";");
		}
		else if (event instanceof OrderRejectEvent) {
			OrderAcceptEvent myEvent = (OrderAcceptEvent) event;
			write("REJECT;");			
			write(event.getMessage()+";");
		}
		else if (event instanceof OrderCancelEvent) {
			OrderCancelEvent myEvent = (OrderCancelEvent) event;
			write("CANCEL;");
			write(event.getMessage()+";");
			
		}
		write("\n");
	}

	@Override
	public void log(Quote arg0) {
		// TODO Auto-generated method stub
		
	}


}
