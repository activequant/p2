/****

activequant - activestocks.eu

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.


contact  : contact@activestocks.eu
homepage : http://www.activestocks.eu

 ****/
package org.activequant.util;

import java.util.LinkedList;
import java.util.List;

import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.data.MarketDataEntity;
import org.activequant.dao.IMarketDataEntityDao;
import org.activequant.data.retrieval.ISubscription;
import org.activequant.util.pattern.events.IEventListener;
import org.apache.log4j.Logger;

/**
 * Base class for Quote, TradeIndication, and Candle feed recorders. <br>
 * <b>History:</b><br>
 * - [09.11.2007] Created (Mike Kroutikov)<br>
 * - [26.11.2007] Making class public as required by
 * http://opensource.atlassian.com/projects/spring/browse/SPR-3270 (GhostRider
 * Staudinger)<br>
 * 
 * @author Mike Kroutikov
 */
public abstract class MarketDataEntityRecorder<T extends MarketDataEntity<T>> {

	protected Logger log = Logger.getLogger(getClass());

	/**
	 * Override this in subclass to define how subscription is created.
	 * 
	 * @param spec
	 *            instrument specs.
	 * @return subscription.
	 */
	protected abstract ISubscription<T> subscribe(InstrumentSpecification spec) throws Exception;

	/**
	 * injected through spring.
	 */
	private IMarketDataEntityDao<T> dao = null;

	/**
	 * Dao saves the entities to the database.
	 * 
	 * @return quote dao.
	 */
	public IMarketDataEntityDao<T> getDao() {
		return dao;
	}

	/**
	 * Sets dao.
	 * 
	 * @param dao
	 *            quote dao value.
	 */
	public void setDao(IMarketDataEntityDao<T> dao) {
		this.dao = dao;
	}

	private InstrumentSpecification[] instrumentsToSubscribeTo = null;

	/**
	 * Instruments to watch and record.
	 * 
	 * @return array of instrument specifications.
	 */
	public InstrumentSpecification[] getInstrumentsToSubscribeTo() {
		return instrumentsToSubscribeTo;
	}

	/**
	 * Sets instruments to watch and record.
	 * 
	 * @param instrumentsToSubscribeTo
	 *            array of instrument specifications.
	 */
	public void setInstrumentsToSubscribeTo(InstrumentSpecification[] instrumentsToSubscribeTo) {
		this.instrumentsToSubscribeTo = instrumentsToSubscribeTo;
	}

	private long flushTimeout = 1000;

	/**
	 * How often to synchronize with the database (i.e. how long to collect
	 * quotes before flushing them out to the database). Default value is
	 * <code>1000</code> millis.
	 * 
	 * @param val
	 *            timeout in milliseconds.
	 */
	public void setFlushTimeout(long val) {
		flushTimeout = val;
	}

	/**
	 * Returns flush timeout in milliseconds.
	 * 
	 * @return
	 */
	public long getFlushTimeout() {
		return flushTimeout;
	}

	public MarketDataEntityRecorder() {
	}

	// controls recording of a single instrument
	private class Download implements IEventListener<T> {

		private final List<T> entities = new LinkedList<T>();

		public Download() {
		}

		public void eventFired(T event) {
			synchronized (entities) {
				entities.add(event);
			}
		}

		public void flush() {
			log.info("" + entities.size() + " events arrived since last flush");

			List<T> list = new LinkedList<T>();
			synchronized (entities) {
				list.addAll(entities);
				entities.clear();
			}

			if (list.size() == 0) {
				return;
			}

			try {

				class UpdaterRunnable implements Runnable {
					List<T> list;

					UpdaterRunnable(List<T> list) {
						this.list = list;
					}

					public void run() {
						dao.update(list);
						log.info("Saved " + list.size() + " entities");
					}
				}
				;

				UpdaterRunnable ur = new UpdaterRunnable(list);
				Thread t = new Thread(ur);
				t.start();

			} catch (Exception ex) {
				log.error(ex);
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Register this method as Spring's "init-method" to make the recording
	 * start as soon as all beans have initialized.
	 * 
	 * @throws Exception
	 *             if something goes wrong.
	 */
	public void record() throws Exception {

		if (instrumentsToSubscribeTo == null || instrumentsToSubscribeTo.length == 0) {
			return;
		}

		// subscribe to the quote source

		Download download = new Download();
		for (InstrumentSpecification instrument : instrumentsToSubscribeTo) {
			ISubscription<T> subscription = subscribe(instrument);
			subscription.addEventListener(download);
			subscription.activate();
		}

		long sleepDelay = flushTimeout;

		while (true) {
			log.info("collecting events (for " + sleepDelay + " millis)");
			Thread.sleep(sleepDelay);

			long start = System.currentTimeMillis();

			log.info("flushing out collected items");

			download.flush();

			// adjust sleep delay so that wakeup intervals are always equal
			// to timeout
			sleepDelay = flushTimeout - (System.currentTimeMillis() - start);
			if (sleepDelay < 0)
				sleepDelay = 0;
		}
	}
}
