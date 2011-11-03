package org.activequant.tradesystems;

import java.util.ArrayList;
import java.util.List;

import org.activequant.broker.IBroker;
import org.activequant.core.domainmodel.InstrumentSpecification;
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
	private ISpecificationDao specDao;		
	private IQuoteDao quoteDao; 
	private List<InstrumentSpecification> instrumentSpecs = new ArrayList<InstrumentSpecification>();

	
	public List<InstrumentSpecification> getInstrumentSpecs() {
		return instrumentSpecs;
	}
	public void setInstrumentSpecs(List<InstrumentSpecification> instrumentSpecs) {
		this.instrumentSpecs = instrumentSpecs;
	}
	public void setSpecDao(ISpecificationDao specDao) {
		this.specDao = specDao;
	}
	public void setQuoteDao(IQuoteDao quoteDao) {
		this.quoteDao = quoteDao;
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
