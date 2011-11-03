package org.activequant.persistence;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class TradeStatisticsDao {
	private Session session;

	public TradeStatisticsDao() {

	}

	public void persist(TradeStatistics ts) {
		session = SessionFactoryUtil.getInstance().getCurrentSession();

		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			if(ts.getId()==0l)
			{
				long id = (Long)session.save(ts);
				ts.setId(id);
			}
			else
			{
				session.update(ts);
			}
			/*ts.setMinPnl(-100.0);
			session.update(ts);*/
			tx.commit();
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive()) {
				try {
					// Second try catch as the rollback could fail as well
					tx.rollback();
				} catch (HibernateException e1) {
					e1.printStackTrace();
				}
				// throw again the first exception
				throw e;
			}
		}
	}
}
