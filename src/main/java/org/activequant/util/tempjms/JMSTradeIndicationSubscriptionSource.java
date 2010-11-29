package org.activequant.util.tempjms;

import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.data.TradeIndication;
import org.activequant.data.retrieval.ITradeIndicationSubscriptionSource;
import org.activequant.data.retrieval.ISubscription;
import org.activequant.util.exceptions.NotImplementedException;

public class JMSTradeIndicationSubscriptionSource implements ITradeIndicationSubscriptionSource {

	private InternalTradeIndicationSubscriptionSource internalQuoteSubscriptionSource = new InternalTradeIndicationSubscriptionSource();
	private JMS jms; 	

	public JMSTradeIndicationSubscriptionSource(String host, int port)
	{
		jms = new JMS(host, port);
	}
	
	@Override
	public ISubscription<TradeIndication>[] getSubscriptions() {
		throw new NotImplementedException("Not implemented yet.");
	}

	@Override
	public ISubscription<TradeIndication> subscribe(InstrumentSpecification spec) throws Exception {
		MessageHandler handler = new MessageHandler(null, internalQuoteSubscriptionSource, spec);
		jms.subscribeMessageHandler("AQID"+spec.getId(), handler);		
		return internalQuoteSubscriptionSource.subscribe(spec);		
	}

}
