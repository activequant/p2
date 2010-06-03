package org.activequant.tradesystems;

import java.util.List;

import org.activequant.broker.IBroker;
import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.account.BrokerAccount;
import org.activequant.optimization.domainmodel.AlgoEnvConfig;
import org.activequant.reporting.IValueReporter;

/**
 * This class contains all relevant environment parameters, it is strongly comparable to the trade system context of AQ. 
 *
 * @author Ghost Rider
 *
 */
public class AlgoEnvironment {

	private AlgoEnvConfig algoEnvConfig;
	private IBroker broker;
	private BrokerAccount brokerAccount;
	private IValueReporter valueReporter; 

	
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
}
