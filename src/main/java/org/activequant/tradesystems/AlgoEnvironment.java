package org.activequant.tradesystems;

import org.activequant.broker.IBroker;
import org.activequant.core.domainmodel.account.BrokerAccount;
import org.activequant.dao.hibernate.QuoteDao;
import org.activequant.dao.hibernate.SpecificationDao;
import org.activequant.optimization.domainmodel.AlgoEnvConfig;
import org.activequant.reporting.IValueReporter;
import org.activequant.util.spring.ServiceLocator;

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
	private final SpecificationDao specDao;		
	private final QuoteDao quoteDao; 
	
	public AlgoEnvironment()
	{
		specDao = (org.activequant.dao.hibernate.SpecificationDao) ServiceLocator.instance("activequantdao/config.xml").getContext().getBean("specificationDao");
		quoteDao = (org.activequant.dao.hibernate.QuoteDao) ServiceLocator.instance("activequantdao/config.xml").getContext().getBean("quoteDao");
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
	public SpecificationDao getSpecDao() {
		return specDao;
	}
	public QuoteDao getQuoteDao() {
		return quoteDao;
	}
}
