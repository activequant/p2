package org.activequant.util.tempjms;

import java.util.HashMap;

import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.activequant.core.domainmodel.InstrumentSpecification;

public class JMS {

	public JMS(String host, int port)
	{
		jmsHost = host; 
		jmsPort = port; 
	}	

	public String getTopicName(InstrumentSpecification aSpec) {
		String myTemp = "AQID"+aSpec.getId();		
		return myTemp;
	}
	
	public TopicConnection getConnection() throws Exception {
		synchronized (theConnectionLock) {
			if (theConnection == null) {
				System.out.println("Connecting to " + jmsHost + ":"+jmsPort);
				com.sun.messaging.TopicConnectionFactory conFactory = new com.sun.messaging.TopicConnectionFactory();
				conFactory.setProperty(
					com.sun.messaging.ConnectionConfiguration.imqBrokerHostName,jmsHost);
				conFactory.setProperty(
					com.sun.messaging.ConnectionConfiguration.imqBrokerHostPort,""+jmsPort);

				// enabling reconnects. 
				conFactory.setProperty(com.sun.messaging.ConnectionConfiguration.imqReconnectEnabled, "true");
				conFactory.setProperty(com.sun.messaging.ConnectionConfiguration.imqReconnectAttempts, "-1");			
				
				// Create a JMS connection
				theConnection = conFactory.createTopicConnection();
				theConnection.start();
				System.out.println("Connected ... ");
			}
		}
		return theConnection;

	}

	public TopicSession getMessageProducer()
			throws Exception {

		
		synchronized (theLock) {
			if (subSession == null)
				// Create two JMS session objects
				subSession = getConnection().createTopicSession(false,
						Session.AUTO_ACKNOWLEDGE);
		}

		return subSession;

	}


	/**
	 * needs to be converted to a facade, as of now, every window generates a
	 * new subscriber, which is saved into a local hashmap based on the hashcode of the messagelistener ... 
	 * 
	 * @param aChannel
	 * @param aListener
	 * @throws Exception
	 */
	public synchronized void subscribeMessageHandler(String aChannel,
			MessageListener aListener) throws Exception {
		String key = aListener.hashCode() + aChannel; 
		if(theTopicSubscribers.containsKey(key))
			return;
		
		// Create two JMS session objects
		subSession = getConnection().createTopicSession(false,
				Session.AUTO_ACKNOWLEDGE);
		
		// Look up a JMS topic
		Topic assetTopic = subSession.createTopic(aChannel);

		// Create a JMS publisher and subscriber
		TopicSubscriber subscriber = subSession.createSubscriber(assetTopic);		
		theTopicSubscribers.put(key, subscriber);
		
		// Set a JMS message listener
		subscriber.setMessageListener(aListener);

		// Start the JMS connection; allows messages to be delivered
		getConnection().start();

	}
	
	/**
	 * unsubscribes a message handler from a channel. Defacto, closes the topic subscriber / aListener combination
	 * 
	 * @param aChannel
	 * @param aListener
	 * @throws Exception
	 */
	public synchronized void unsubscribeMessageHandler(String aChannel, MessageListener aListener) 
		throws Exception 
	{
		if(theTopicSubscribers.containsKey(aListener.hashCode()+aChannel))
		{
			theTopicSubscribers.get(aListener.hashCode() + aChannel).close(); 
			theTopicSubscribers.remove(aListener.hashCode() + aChannel); 
		}
	}
	
	private TopicSession subSession;
	private TopicConnection theConnection;
	private Object theLock = new Object();
	private Object theConnectionLock = new Object();
	private HashMap<String, TopicSubscriber> theTopicSubscribers = new HashMap<String, TopicSubscriber>();
	public String jmsHost = "localhost"; // "83.169.9.78"; 
	public int jmsPort = 7676; 
}
