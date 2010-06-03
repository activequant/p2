package org.activequant.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Hashtable;
import java.util.concurrent.LinkedBlockingQueue;

import javax.jms.MessageProducer;
import javax.jms.TextMessage;

import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.Symbol;
import org.activequant.core.domainmodel.data.Quote;
import org.activequant.core.domainmodel.data.TradeIndication;
import org.activequant.core.types.Currency;
import org.activequant.core.types.SecurityType;
import org.activequant.core.types.TimeStamp;
import org.activequant.dao.IFactoryDao;
import org.activequant.dao.ISpecificationDao;
import org.activequant.dao.hibernate.FactoryLocatorDao;
import org.activequant.util.tempjms.JMS;
import org.apache.log4j.Logger;

/**
 * this class opens a telnet socket and listens for csv lines that contain
 * either quotes or ticks. These lines are then transformed into AQ domain
 * objects and (that's the purpose of this app) are then sent to a jms endpoint
 * for relaying.
 * 
 * The protocol used is a simple line based csv protocol, where each dataset is
 * transmitted in one line.
 * 
 * Protocol used: for TradeIndicatons: T;NanoSecTimeStamp;instrument
 * spec;Price;Volume;
 * 
 * for Quotes: Q;NanoSecTimeStamp;instrument
 * spec;BidPrice;BidVolume;AskPrice;AskVolume;
 * 
 * The instrument specification must be comma separated and must have the
 * format: InstrumentName,Exchange,Currency
 * 
 * @author Ghost Rider.
 */
class DataRelay {

	IFactoryDao factoryDao = new FactoryLocatorDao("activequantdao/config.xml");
	ISpecificationDao specDao = factoryDao.createSpecificationDao();
	protected final static Logger log = Logger.getLogger(DataRelay.class);
	private int theQuoteCount;
	private int theTickCount;

	private int theListenerPort = 22223;
	private String theJmsEndPoint = "";
	private int theJmsPort = 7676;
	private String theJmsUserName = "username";
	private String theJmsPassword = "password";
	private JMS jms;
	private boolean quit = false;

	// it would be possible to overwrite this from within spring ...
	private Hashtable<String, InstrumentSpecification> theSpecCache = new Hashtable<String, InstrumentSpecification>();

	public DataRelay(String jmsHost, int jmsPort) throws Exception {
		jms = new JMS(jmsHost, jmsPort);
	}

	public void startRelay() throws Exception {
		startListenerSocket();

	}

	public JMS getJms() {
		return jms;
	}

	private void startListenerSocket() throws Exception {
		System.out.println("Settings: ");
		System.out.println("ListnerPort " + theListenerPort);
		ServerSocket listenerSocket = new ServerSocket(theListenerPort);
		Socket mySocket;
		while (!quit) {
			mySocket = listenerSocket.accept();
			new Thread(new WorkerThread(this, mySocket)).start();
		}

	}

	public static void main(String[] args) throws Exception {
		DataRelay myRelay = new DataRelay("localhost", 7676);
		myRelay.startRelay();
	}

	public void increaseQuoteCount() {
		theQuoteCount++;
	}

	public void increaseTickCount() {
		theTickCount++;
	}

	public int getTheListenerPort() {
		return theListenerPort;
	}

	public void setListenerPort(int theListenerPort) {
		this.theListenerPort = theListenerPort;
	}

	public synchronized InstrumentSpecification getSpec(String aSymbol, String anExchange, String aCurrency, String aVendor) {

		String myKey = aSymbol + anExchange + aCurrency;
		if (!theSpecCache.containsKey(myKey)) {
			System.out.println("Resolving " + aSymbol + "/" + anExchange);
			// fetch the key ..
			InstrumentSpecification myExampleSpec = new InstrumentSpecification();
			myExampleSpec.setSymbol(new Symbol(aSymbol));
			myExampleSpec.setCurrency(Currency.valueOf(aCurrency));
			myExampleSpec.setExchange(anExchange);
			myExampleSpec.setVendor(aVendor);

			InstrumentSpecification spec = specDao.findByExample(myExampleSpec);
			if (spec == null) {
				myExampleSpec.setLotSize(1);
				myExampleSpec.setTickSize(1);
				myExampleSpec.setTickValue(1);
				myExampleSpec.setSecurityType(SecurityType.FUTURE);
				spec = specDao.update(myExampleSpec);
			}

			theSpecCache.put(myKey, spec);
			System.out.println("Resolved instrument to id " + spec.getId());
		}
		return theSpecCache.get(myKey);

	}

}

class WorkerThread implements Runnable {
	WorkerThread(DataRelay aRelay, Socket aSocket) throws Exception {
		theRelay = aRelay;
		theSocket = aSocket;
	}

	private MessageProducer getProducer(String aTopic) throws Exception {
		if (!theProducers.containsKey(aTopic)) {
			theProducers.put(aTopic, theRelay.getJms().getMessageProducer().createPublisher(
					theRelay.getJms().getMessageProducer().createTopic(aTopic)));
			System.out.println("Creating new producer for topic "+aTopic);
		}
		return theProducers.get(aTopic);
	}

	private TextMessage getTextMessage(String aTopic) throws Exception {
		if (!theMessages.containsKey(aTopic))
			theMessages.put(aTopic, theRelay.getJms().getMessageProducer().createTextMessage());
		return theMessages.get(aTopic);

	}

	private String getTopicName(InstrumentSpecification aSpec) {
		String myTemp = "AQID" + aSpec.getId();
		return myTemp;
	}

	private Hashtable<String, MessageProducer> theProducers = new Hashtable<String, MessageProducer>();
	private Hashtable<String, TextMessage> theMessages = new Hashtable<String, TextMessage>();

	private void handleLine(String aLine) throws Exception {
	//	System.out.println(aLine);
		String[] parts = aLine.split(";");
		String type = parts[0];

		if (type.equals("T")) {
			handleTick(parts);
		} else if (type.equals("Q")) {
			handleQuote(parts);
		}
	}

	private void handleTick(String[] parts) throws Exception {
		long nanoseconds = Long.parseLong(parts[1]);
		String[] myInstrumentParts = parts[2].split(",");

		double tradedPrice = Double.parseDouble(parts[3]);
		double tradedVolume = Double.parseDouble(parts[4]);

		TradeIndication myTick = new TradeIndication();
		myTick.setTimeStamp(new TimeStamp(nanoseconds));
		myTick.setPrice(tradedPrice);
		myTick.setQuantity(tradedVolume);
		myTick.setInstrumentSpecification(theRelay.getSpec(myInstrumentParts[0], myInstrumentParts[1], myInstrumentParts[2],
				myInstrumentParts[3]));
		// hardcore avoid for now.
		// theTickPublisher.publish(myTick);
		theRelay.increaseTickCount();
	}

	private void handleQuote(String[] parts) throws Exception {
		System.out.print(".");
		try {
			long nanoseconds = Long.parseLong(parts[1]);
			String[] myInstrumentParts = parts[2].split(",");
			double bidPrice = Double.parseDouble(parts[3]);
			double bidVolume = Double.parseDouble(parts[4]);
			double askPrice = Double.parseDouble(parts[5]);
			double askVolume = Double.parseDouble(parts[6]);

			Quote myQuote = new Quote();
			myQuote.setBidPrice(bidPrice);
			myQuote.setBidQuantity(bidVolume);
			myQuote.setAskPrice(askPrice);
			myQuote.setAskQuantity(askVolume);
			myQuote.setTimeStamp(new TimeStamp(nanoseconds));
			myQuote.setInstrumentSpecification(theRelay.getSpec(myInstrumentParts[0], myInstrumentParts[1], myInstrumentParts[2],
					myInstrumentParts[3]));
			theRelay.increaseQuoteCount();
			// hardcore send out direct.
			String myTopic = getTopicName(myQuote.getInstrumentSpecification());
			String myLine = ("TIME=" + System.currentTimeMillis() + ",MAIN/" + myTopic + "/BID=" + myQuote.getBidPrice() + ",MAIN/"
					+ myTopic + "/ASK=" + myQuote.getAskPrice() + ",MAIN/" + myTopic + "/BIDVOL=" + myQuote.getBidQuantity() + ",MAIN/"
					+ myTopic + "/ASKVOL=" + myQuote.getAskQuantity()

			);
			TextMessage myMessage = getTextMessage(myTopic);
			myMessage.setText(myLine);
			getProducer(myTopic).send(myMessage);

			// theQuotePublisher.publish(myQuote);
		} catch (Exception anEx) {
			System.out.println(getString(parts));
			anEx.printStackTrace();
		}
	}

	private String getString(String[] aString) {
		StringBuffer mySb = new StringBuffer();
		for (String myS : aString) {
			mySb.append(myS + "//");
		}
		return mySb.toString();
	}

	public void run() {
		try {
			ReadThread myT = new ReadThread();
			myT.theWorkerThread = this;
			myT.myBr = new BufferedReader(new InputStreamReader(theSocket.getInputStream()));
			Thread myTh = new Thread(myT);
			myTh.start();
			while (true) {
			
				String myL = theQueue.take();
				try { 
					handleLine(myL);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
				// System.out.println("[" + new Date() + "] Queue length: " + theQueue.size());
			}
		} catch (Exception anEx) {
			anEx.printStackTrace();
		}
	}

	private DataRelay theRelay;
	private Socket theSocket;
	LinkedBlockingQueue<String> theQueue = new LinkedBlockingQueue<String>();

}

/**
 * reads from socket and puts it back to the worker ...
 * 
 * @author ulst
 * 
 */
class ReadThread implements Runnable {
	WorkerThread theWorkerThread;
	BufferedReader myBr;

	public void run() {
		try {
			while (true) {
				String l = "";
				l = myBr.readLine();
				while (l != null) {
					theWorkerThread.theQueue.put(l);
					l = myBr.readLine();
				}
			}
		} catch (Exception anEx) {
			anEx.printStackTrace();
		}

	}
}
