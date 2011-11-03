package org.activequant.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.data.Quote;
import org.activequant.core.types.TimeFrame;
import org.activequant.data.retrieval.IQuoteSubscriptionSource;
import org.activequant.data.retrieval.ISubscription;
import org.activequant.util.exceptions.SubscriptionException;
import org.activequant.util.pattern.events.IEventListener;
import org.activequant.util.tools.UniqueDateGenerator;

/**
 * @author Ghost Rider
 */
public class VirtualQuoteSubscriptionSource implements IQuoteSubscriptionSource {

	private HashMap<InstrumentSpecification, List<ISubscription<Quote>>> sububscriptions = new HashMap<InstrumentSpecification, List<ISubscription<Quote>>>();
	private HashMap<InstrumentSpecification, Quote> theQuoteSheet = new HashMap<InstrumentSpecification, Quote>();
	private UniqueDateGenerator dateGenerator = new UniqueDateGenerator(); 
	
	/**
	 * 
	 * @author Ghost Rider
	 * 
	 */
	class Subscription implements ISubscription<Quote> {

		@Override
		public void activate() throws SubscriptionException {
			// resend current quote.
			isActive = true;
			if (theQuoteSheet.containsKey(theSpec)) {
				Quote myCurrentQuote = theQuoteSheet.get(theSpec);
				// rewrite the current quote time ..
				myCurrentQuote.setTimeStamp(
						dateGenerator.generate(myCurrentQuote.getTimeStamp().getDate()));
				synchronized (listeners) {
					for (IEventListener<Quote> myListener : listeners)
						myListener.eventFired(myCurrentQuote);
				}
			}
		}

		@Override
		public void addEventListener(IEventListener<Quote> arg0) {
			listeners.add(arg0);
		}

		@Override
		public void cancel() throws SubscriptionException {
			sububscriptions.get(theSpec).remove(this);
		}

		@Override
		public InstrumentSpecification getInstrument() {
			return theSpec;
		}

		@Override
		public TimeFrame getTimeFrame() {
			return TimeFrame.TIMEFRAME_1_TICK;
		}

		@Override
		public boolean isActive() {
			return isActive;
		}

		@Override
		public void removeEventListener(IEventListener<Quote> arg0) {
			listeners.remove(arg0);
		}

		List<IEventListener<Quote>> listeners = Collections
				.synchronizedList(new ArrayList<IEventListener<Quote>>());
		InstrumentSpecification theSpec;
		boolean isActive = false;
	}

	/**
	 * Carefull: buggy!
	 */
	@Override
	@SuppressWarnings("unchecked")
	public ISubscription<Quote>[] getSubscriptions() {
		ISubscription<Quote>[] myRet = new ISubscription[sububscriptions
				.size()];
		Iterator<List<ISubscription<Quote>>> myIt = sububscriptions.values()
				.iterator();
		for (int i = 0; i < sububscriptions.size(); i++) {
			myRet[i] = (ISubscription<Quote>) myIt.next();
		}
		return myRet;
	}

	public void distributeQuote(Quote aQuote) {
		// update the local quote sheet
		theQuoteSheet.put(aQuote.getInstrumentSpecification(), aQuote);

			List<ISubscription<Quote>> subs = (List<ISubscription<Quote>>) sububscriptions
					.get(aQuote.getInstrumentSpecification());
			if (subs == null)
				return;

			List<ISubscription<Quote>> subsClone = new ArrayList<ISubscription<Quote>>();
			for(ISubscription<Quote> sub : subs) subsClone.add(sub);
			
			for (// get the subscription ...
			ISubscription<Quote> sub : subsClone) {
				if (sub != null) {
					synchronized (((Subscription) sub).listeners) {
						for (IEventListener<Quote> myListener : ((Subscription) sub).listeners)
							myListener.eventFired(aQuote);
					}
				}
			}
		
	}

	@Override
	public ISubscription<Quote> subscribe(InstrumentSpecification arg0) {
			if (!sububscriptions.containsKey(arg0)) {
				sububscriptions.put(arg0,
						new ArrayList<ISubscription<Quote>>());
			}
			Subscription mySub = new Subscription();
			mySub.theSpec = arg0;
			sububscriptions.get(arg0).add(mySub);
			return mySub;
		
	}

}
