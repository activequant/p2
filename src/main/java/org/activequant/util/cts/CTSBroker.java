package org.activequant.util.cts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.HashMap;

import org.activequant.broker.BrokerBase2;
import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.account.BrokerAccount;
import org.activequant.core.domainmodel.account.Order;
import org.activequant.core.domainmodel.account.Position;
import org.activequant.core.domainmodel.events.OrderCancelEvent;
import org.activequant.core.domainmodel.events.OrderExecutionEvent;
import org.activequant.core.types.TimeStamp;
import org.activequant.util.SpecResolver;
import org.activequant.util.pattern.events.Event;

import org.apache.log4j.Logger;


/**
 * a jms broker implementation
 */
class CTSBroker extends BrokerBase2 {

	private SpecResolver specResolver = new SpecResolver();
	private HashMap<String, OrderTracker> orderTrackers = new HashMap<String, OrderTracker>();
	private Event<OrderExecutionEvent> unknownExecutions = new Event<OrderExecutionEvent>();
	private DecimalFormat decFormat = new DecimalFormat("#.#######");
	private long orderCounter = 0;
	private long orderIdBase = System.currentTimeMillis();
	private BufferedReader br;
	private BufferedWriter bw;
	Thread readThread;
	private BrokerAccount brokerAccount = new BrokerAccount("CTS", "N/A"); 
        private static Logger log = Logger.getLogger(CTSBroker.class);


	public CTSBroker(String ctsGateIP, int ctsGatePort, String identifier)
			throws IOException {
		Socket s = new Socket(ctsGateIP, ctsGatePort);
		br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
		// log on and assume the happy path.
		bw.write(identifier);
		bw.newLine();
		bw.flush();
		readThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String line = br.readLine();
					System.out.println(line);
					while (line != null) {
						try{handleLine(line);}catch(Exception ex){System.out.println(line); ex.printStackTrace();}
						line = br.readLine();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		readThread.setDaemon(false);
		readThread.start();
	}

	public BrokerAccount getBrokerAccount(){
		return brokerAccount;
	}

	private long getNextOrderId() {
		return orderIdBase + orderCounter++;
	}

	private void sendToGate(String text){
		try{
			bw.write(text);
			bw.newLine();
			bw.flush();
			log.info("Sent to gate: " + text);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	class OrderTracker extends OrderTrackerBase {
		private final long orderId;
		private Order localOrder;
		private double openQuantity;

		public OrderTracker(Order order) {
			super(order);
			localOrder = new Order(order);
			orderId = getNextOrderId();
			openQuantity = order.getQuantity();
		}

		protected String handleSubmit() {
			String otxt = "OP;"
					+ orderId
					+ ";PO;"
					+ localOrder.getInstrumentSpecification().getSymbol()
							.toString() + ";"+localOrder.getOrderSide().toString()+";LIMIT;GTC;"
					+ decFormat.format(localOrder.getQuantity()) + ";"
					+ decFormat.format(localOrder.getLimitPrice()) + ";0;";
			sendToGate(otxt);
			return ""+orderId;
		}

		public void handleUpdate(Order newOrder) {
			String otxt = "OP;" + orderId + ";UO;"
					+ decFormat.format(newOrder.getQuantity()) + ";"
					+ decFormat.format(newOrder.getLimitPrice()) + ";";
			sendToGate(otxt);
		}

		public void handleCancel() {
			String otxt = "OP;" + orderId + ";CO;";
			// send it.
			sendToGate(otxt);
			
		}

		public void beenCancelled(){
			fireOrderEvent(new OrderCancelEvent());
		}
		
		private void fireExecution(double quantity, double price) {
			OrderExecutionEvent oe = new OrderExecutionEvent();
			oe.setPrice(price);
			oe.setQuantity(quantity);
			// 
			oe.setEventTimeStamp(new TimeStamp());
			this.fireOrderEvent(oe);	
		}

	}

	private void handleLine(String line) {
		if(line.startsWith("Q;") || line.startsWith("T;"))return;
		if(line.startsWith("Welcome")){
			log.info("Successfully logged in. Interface greeted us warmly. ");
			return;
		}
		System.out.println(line);
		String[] lineParts = line.split(";");
		String type = lineParts[0];
		String ioid = lineParts[1];
		String minorCmd = lineParts[2];
		
		if (minorCmd.equals("OF")) {
			double orderVolume = Double.parseDouble(lineParts[3]);
			double orderTicks = Double.parseDouble(lineParts[4]);			
			OrderTracker ot = orderTrackers.get(ioid);
			if (ot != null) {
				ot.fireExecution(orderVolume,
						orderTicks);
			} 
		} else if (type.equals("OU")) {
			String[] instrumentParts = lineParts[1].split(",");
			InstrumentSpecification spec = specResolver.getSpec(
					instrumentParts[0], instrumentParts[1], instrumentParts[2],
					instrumentParts[3]);
			// extract the native order id
			String orderId = ioid;

			OrderTracker ot = orderTrackers.get(orderId);
			if (ot != null) {
			}
		} else if (type.equals("OC")) {
			String orderId = ioid;

			OrderTracker ot = orderTrackers.get(orderId);
			if (ot != null) {
				
			}

		}
//                         string text2 = "PU;"+position.AccountID+";"+position.MarketID+";"+position.PL+";"+pos+";";

		else if (type.equals("PU")){
			String accountId = lineParts[1];
			String assetId = lineParts[2];
			Double avgPrice = Double.parseDouble(lineParts[3]);
			Double quantity = Double.parseDouble(lineParts[4]);
			// get the instrument specification ...
			String[] instrumentParts = lineParts[2].split(",");
			InstrumentSpecification spec = specResolver.getSpec(
					instrumentParts[0], instrumentParts[1], instrumentParts[2],
					instrumentParts[3]);
			if(spec==null){
				this.log.warn("No specification for "+lineParts[1]+" found in DB. Ignoring position");
				return; 
			}
						
			if(!this.getBrokerAccount().getPortfolio().hasPosition(spec)){
				Position position = new Position(spec, avgPrice, quantity);
				this.getBrokerAccount().getPortfolio().addPosition(position);
			}
			else{
				Position position = this.getBrokerAccount().getPortfolio().getPosition(spec);
				position.setAveragePrice(avgPrice);
				position.setQuantity(quantity);
			}
			
		}
	}

	private void fireUnknownExecution(InstrumentSpecification spec,
			double quantity, double price) {
		//
		OrderExecutionEvent oe = new OrderExecutionEvent();
		oe.setPrice(price);
		oe.setQuantity(quantity);
		unknownExecutions.fire(oe);
	}

	@Override
	protected OrderTracker createOrderTracker(Order order) {
		OrderTracker ot = new OrderTracker(order);
		this.orderTrackers.put(""+ot.orderId, ot);
		return ot; 
	}

}
