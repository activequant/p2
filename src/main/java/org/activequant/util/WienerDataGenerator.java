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
 * Creates random price quotes, following a Wiener process, similar to Mike's
 * application.
 * 
 * 
 * @author Ulrich Staudinger.
 */
class WienerDataGenerator {

	IFactoryDao factoryDao = new FactoryLocatorDao("activequantdao/config.xml");
	ISpecificationDao specDao = factoryDao.createSpecificationDao();
	protected final static Logger log = Logger
			.getLogger(WienerDataGenerator.class);

	private String theJmsEndPoint = "";
	private int theJmsPort = 7676;
	private String theJmsUserName = "username";
	private String theJmsPassword = "password";
	private JMS jms;
	private boolean quit = false;
	private double price1 = 100.0, price2 = 100.0, price3 = 100.0;

	private Hashtable<String, MessageProducer> theProducers = new Hashtable<String, MessageProducer>();
	private Hashtable<String, TextMessage> theMessages = new Hashtable<String, TextMessage>();

	// it would be possible to overwrite this from within spring ...
	private Hashtable<String, InstrumentSpecification> theSpecCache = new Hashtable<String, InstrumentSpecification>();

	public WienerDataGenerator(String jmsHost, int jmsPort) throws Exception {
		jms = new JMS(jmsHost, jmsPort);
	}

	public void startRelay() throws Exception {
		while (true) {
			Thread.sleep((int)(Math.random() * 1000 + 10));
			generateQuotes();
		}
	}

	private void emitQuote(double price, String name, String exchange,
			String currency, String vendor) throws Exception {
		double bidPrice = price - 0.01;
		double bidVolume = 100.0;
		double askPrice = price + 0.01;
		double askVolume = 100.0;

		Quote myQuote = new Quote();
		myQuote.setBidPrice(bidPrice);
		myQuote.setBidQuantity(bidVolume);
		myQuote.setAskPrice(askPrice);
		myQuote.setAskQuantity(askVolume);
		myQuote.setTimeStamp(new TimeStamp(System.nanoTime()));
		myQuote.setInstrumentSpecification(getSpec(name, exchange, currency,
				vendor));
		// hardcore send out direct.
		String myTopic = getTopicName(myQuote.getInstrumentSpecification());
		String myLine = ("TIME=" + System.currentTimeMillis() + ",MAIN/"
				+ myTopic + "/BID=" + myQuote.getBidPrice() + ",MAIN/"
				+ myTopic + "/ASK=" + myQuote.getAskPrice() + ",MAIN/"
				+ myTopic + "/BIDVOL=" + myQuote.getBidQuantity() + ",MAIN/"
				+ myTopic + "/ASKVOL=" + myQuote.getAskQuantity()

		);
		TextMessage myMessage = getTextMessage(myTopic);
		myMessage.setText(myLine);
		getProducer(myTopic).send(myMessage);

	}

	private void generateQuotes() throws Exception {
		//System.out.print(".");

		double return1 = 1 - (0.5 - Math.random()) / 500.0;
		price1 *= return1;

		double return2 = 1 - (0.5 - Math.random()) / 500.0;
		price2 *= return2;

		double return3 = 1 - (0.5 - Math.random()) / 500.0;
		price3 *= return3;

		emitQuote(price1, "Random1", "Internal", "EUR", "AQ");
		emitQuote(price2, "Random2", "Internal", "EUR", "AQ");
		emitQuote(price3, "Random3", "Internal", "EUR", "AQ");
		
		
	}

	public static void main(String[] args) throws Exception {
		WienerDataGenerator myRelay = new WienerDataGenerator("localhost", 7676);
		myRelay.startRelay();
	}

	public synchronized InstrumentSpecification getSpec(String aSymbol,
			String anExchange, String aCurrency, String aVendor) {

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

	private MessageProducer getProducer(String aTopic) throws Exception {
		if (!theProducers.containsKey(aTopic)) {
			theProducers.put(
					aTopic,
					jms.getMessageProducer().createPublisher(
							jms.getMessageProducer().createTopic(aTopic)));
			System.out.println("Creating new producer for topic " + aTopic);
		}
		return theProducers.get(aTopic);
	}

	private TextMessage getTextMessage(String aTopic) throws Exception {
		if (!theMessages.containsKey(aTopic))
			theMessages.put(aTopic, jms.getMessageProducer()
					.createTextMessage());
		return theMessages.get(aTopic);

	}

	private String getTopicName(InstrumentSpecification aSpec) {
		String myTemp = "AQID" + aSpec.getId();
		return myTemp;
	}

}
