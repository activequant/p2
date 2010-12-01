package org.activequant.util.tempjms;

import java.text.SimpleDateFormat;
import java.util.concurrent.LinkedBlockingQueue;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.data.Quote;
import org.activequant.core.domainmodel.data.TradeIndication;

import org.apache.log4j.Logger; 

public class MessageHandler implements MessageListener {
	
	private InternalQuoteSubscriptionSource source1; 
	private InternalTradeIndicationSubscriptionSource source2;
	private InstrumentSpecification spec; 
	private static Logger  logger = Logger.getLogger(MessageHandler.class);

	
	class LocalMessageHandler implements Runnable 
	{
		public void run()
		{
			while(true)
			{
				Message message;
				try {
					if(queueingMode)
						theWaitQueue.take();
					message = (Message)theWorkQueue.take();
					handleMessage(message);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		private void handleMessage(Message message)
		{
			try {
				if (message instanceof TextMessage) {
					TextMessage textMessage = (TextMessage) message;
					String text = textMessage.getText();
					parse(text);				
				}				
			} catch (Exception jmse) {			
				jmse.printStackTrace();
			}
		}
	}
	
	public MessageHandler(InternalQuoteSubscriptionSource source1, InternalTradeIndicationSubscriptionSource source2, InstrumentSpecification spec)
	{
		this.source1 = source1; 
		this.source2 = source2; 
		this.spec = spec; 
		Thread myT = new Thread(new LocalMessageHandler());
		myT.start(); 
	}
	
	
	public void parse(String[] aDataLines)
	{
		for(String myS : aDataLines)
			parse(myS);
	}
	
	/**
	 * this method is called from somewhere as soon as a data line is to be
	 * parsed. Typically called from local delegator.
	 * 
	 * @param aCsvDataLine
	 */
	public void parse(String aCsvDataLine) {

		if(aCsvDataLine.indexOf("BID")!=-1)
		{

			handleQuoteLine(aCsvDataLine);
		}
		else if(aCsvDataLine.indexOf("T=OE")!=-1){
		    // have to handle an order event.
		}
		else if(aCsvDataLine.indexOf("PRICE")!=-1)
		{
			handleTickLine(aCsvDataLine);
		}
	}
	private void handleTickLine(String aCsvDataLine)
	{
	}
	
	private void handleQuoteLine(String aCsvDataLine)
	{
		Quote myQuote = new Quote();
		// setting the instrument specification
		myQuote.setInstrumentSpecification(spec);
		if(logger.isDebugEnabled()){
			logger.debug("Parsing CSV line: "+aCsvDataLine);
		}
		Long myTime = null;
		String[] myDataEntries = aCsvDataLine.split(",");
		for (String myDataEntry : myDataEntries) {
			String[] myData = myDataEntry.split("=");
			if (myData.length == 2 && !myData[1].equals("")) {
				String myKey = myData[0];
				// System.out.println(myKey);
				if (myKey.equals("TIME")) {
					// time parsing. 
					try {
						myTime = Long.parseLong(myData[1]);
					} catch (Exception anEx) {
					}
					try {
						SimpleDateFormat mySdf = new SimpleDateFormat("yyyy-MM-dd");
						myTime = mySdf.parse(myData[1]).getTime();
						System.out.println("Parsed: " + myTime);
					} catch (Exception anEx) {
					}
					try {
						SimpleDateFormat mySdf = new SimpleDateFormat("MM/dd/yyyy hh:mm");
						myTime = mySdf.parse(myData[1]).getTime();
						System.out.println("Parsed: " + myTime);
					} catch (Exception anEx) {
					}

					long myLocalTime = System.currentTimeMillis();
				
				} else {
					Double myValue = Double.parseDouble(myData[1]);
					if(myKey.endsWith("BID")) myQuote.setBidPrice(myValue);
					else if(myKey.endsWith("ASK")) myQuote.setAskPrice(myValue);
					else if(myKey.endsWith("BIDVOL")) myQuote.setBidQuantity(Math.abs(myValue));
					else if(myKey.endsWith("ASKVOL")) myQuote.setAskQuantity(Math.abs(myValue));
				}
			}
		}
		if(myQuote.getBidPrice()!=Quote.NOT_SET)
		{
			source1.distributeQuote(myQuote);
		}

	}

	
	/**
	 * the on message function, this message is called from jms once a message
	 * arrives.
	 */
	public void onMessage(Message message) {
		theWorkQueue.add(message);
	}
	
	public void startQueueing()
	{
		queueingMode = true; 
	}
	
	public void stopQueueing()
	{
		queueingMode = false;
		try {
			theWaitQueue.put(new Object());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private LinkedBlockingQueue<Message> theWorkQueue = new LinkedBlockingQueue<Message>();
	private LinkedBlockingQueue<Object> theWaitQueue = new LinkedBlockingQueue<Object>();
	private boolean queueingMode = false; 
	
}
