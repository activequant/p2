package org.activeuqant.optimization.reporting;

import org.activequant.persistence.SessionFactoryUtil;
import org.activequant.persistence.TradeStatistics;
import org.activequant.persistence.TradeStatisticsDao;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Test;

public class TradeStatisticsTest {

	@Test
	public void testUpdateStatistics() {
		
		TradeStatisticsDao dao = new TradeStatisticsDao();
		
		TradeStatistics ts = new TradeStatistics();
		dao.persist(ts);
		System.out.println(ts.getId());
		ts.setMaxPnl(100);
		dao.persist(ts);
	}
}
