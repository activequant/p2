package org.activequant.tradesystems.system5;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.SeriesSpecification;
import org.activequant.core.domainmodel.data.Quote;
import org.activequant.core.types.TimeStamp;
import org.activequant.dao.IFactoryDao;
import org.activequant.dao.ISpecificationDao;
import org.activequant.dao.hibernate.FactoryLocatorDao;
import org.activequant.data.retrieval.IQuoteSubscriptionSource;

public class System5Dev {
	protected IFactoryDao factoryDao = new FactoryLocatorDao("data/config.xml");
	protected ISpecificationDao specDao = factoryDao.createSpecificationDao();
	protected IQuoteSubscriptionSource quoteSubscriptionSource;

	public System5Dev() {
		int instrumentId = 183;
		InstrumentSpecification spec = specDao.find(
				instrumentId);
		SeriesSpecification query = new SeriesSpecification(spec);
		Calendar cal = GregorianCalendar.getInstance();
		cal.add(Calendar.HOUR, -2);
		query.setStartTimeStamp(new TimeStamp(cal.getTime()));
		query.setEndTimeStamp(new TimeStamp(new Date()));
		// set start and stop time frame.
		Quote[] quotes = factoryDao.createQuoteDao().findBySeriesSpecification(
				query);
		int length = quotes.length;
		System.out.println("Replaying "+length);
		for (int i = length - 1; i >= 0; i--) {
			Quote q = quotes[i];
		}
	}

	public static void main(String[] a) {
		new System5Dev();
	}

}
