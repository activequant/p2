package org.activequant.tradesystems;

import org.activequant.broker.IBroker;
import org.activequant.core.domainmodel.account.BrokerAccount;
import org.activequant.dao.IFactoryDao;
import org.activequant.dao.IQuoteDao;
import org.activequant.dao.ISpecificationDao;
import org.activequant.dao.hibernate.FactoryLocatorDao;
import org.activequant.dao.hibernate.QuoteDao;
import org.activequant.dao.hibernate.SpecificationDao;
import org.activequant.optimization.domainmodel.AlgoEnvConfig;
import org.activequant.reporting.IValueReporter;

/**
 * This class contains all relevant environment parameters, it is strongly comparable to the trade system context of AQ.
 * It will also contain several dao objects as needed.  
 *
 * @author Ghost Rider
 *
 */
public class AlgoEnvironment {

	private AlgoEnvConfig algoEnvConfig;
	private IBroker broker;
	private BrokerAccount brokerAccount;
	private IValueReporter valueReporter; 
	private RunMode runMode = RunMode.PRODUCTION;
	private final ISpecificationDao specDao;		
	private final IQuoteDao quoteDao; 
	
	public AlgoEnvironment()
	{	
		IFactoryDao factoryDao = new FactoryLocatorDao("data/config.xml");
		specDao = factoryDao.createSpecificationDao();
		quoteDao = factoryDao.createQuoteDao();
	}	
	public RunMode getRunMode() {
		return runMode;
	}
	public void setRunMode(RunMode runMode) {
		this.runMode = runMode;
	}
	public IValueReporter getValueReporter() {
		return valueReporter;
	}
	public void setValueReporter(IValueReporter valueReporter) {
		this.valueReporter = valueReporter;
	}	
	public AlgoEnvConfig getAlgoEnvConfig() {
		return algoEnvConfig;
	}
	public void setAlgoEnvConfig(AlgoEnvConfig algoEnvConfig) {
		this.algoEnvConfig = algoEnvConfig;
	}
	public IBroker getBroker() {
		return broker;
	}
	public void setBroker(IBroker broker) {
		this.broker = broker;
	}
	public BrokerAccount getBrokerAccount() {
		return brokerAccount;
	}
	public void setBrokerAccount(BrokerAccount brokerAccount) {
		this.brokerAccount = brokerAccount;
	}
	public ISpecificationDao getSpecDao() {
		return specDao;
	}
	public IQuoteDao getQuoteDao() {
		return quoteDao;
	}
}
