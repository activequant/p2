package org.activequant.util.tempjms;

import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.data.Quote;
import org.activequant.data.retrieval.IQuoteSubscriptionSource;
import org.activequant.data.retrieval.ISubscription;
import org.activequant.util.exceptions.NotImplementedException;

public class JMSQuoteSubscriptionSource implements IQuoteSubscriptionSource {

	private InternalQuoteSubscriptionSource internalQuoteSubscriptionSource = new InternalQuoteSubscriptionSource();
	private JMS jms; 
	
	public JMSQuoteSubscriptionSource(String host, int port)
	{
		jms = new JMS(host, port);
	}
	
	@Override
	public ISubscription<Quote>[] getSubscriptions() {
		throw new NotImplementedException("Not implemented yet.");
	}

	@Override
	public ISubscription<Quote> subscribe(InstrumentSpecification spec) throws Exception {
		MessageHandler handler = new MessageHandler(internalQuoteSubscriptionSource, null, spec);
		jms.subscribeMessageHandler("AQID"+spec.getId(), handler);		
		return internalQuoteSubscriptionSource.subscribe(spec);		
	}

}
