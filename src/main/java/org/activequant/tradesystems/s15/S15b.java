package org.activequant.tradesystems.s15;

import org.activequant.container.report.SimpleReport;
import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.data.Quote;
import org.activequant.optimization.domainmodel.AlgoConfig;
import org.activequant.production.InMemoryAlgoEnvConfigRunner;
import org.activequant.tradesystems.AlgoEnvironment;
import org.activequant.tradesystems.BasicTradeSystem;
import org.activequant.util.LimitedQueue;
import org.activequant.util.spring.ServiceLocator;
import org.apache.log4j.Logger;

public class S15b extends BasicTradeSystem {

	private static Logger log = Logger.getLogger(S15b.class);
	private SystemState ss = new SystemState();
	private InstrumentSpecification spec1, spec2;
	
	@Override
	public boolean initialize(AlgoEnvironment algoEnv, AlgoConfig algoConfig) {
		super.initialize(algoEnv, algoConfig);
		// load the system state. 
		loadState();		
		ss.setPeriod1((Integer) algoConfig.get("period1"));
		ss.setPeriod2((Integer) algoConfig.get("period2"));
		if(ss.getShortRatioQueue()==null){
			ss.setShortRatioQueue(new LimitedQueue<Double>(ss.getPeriod1()));
			ss.setLongRatioQueue(new LimitedQueue<Double>(ss.getPeriod2()));
		}
		spec1 = getAlgoEnv().getInstrumentSpecs().get(0); 
		spec2 = getAlgoEnv().getInstrumentSpecs().get(1);
		System.out.printf("Initialized with %d, %d\n", ss.getPeriod1(), ss.getPeriod2());
		return true;
	}
	
	private void loadState(){
	}
	
	private void saveState(){
	}
	
	
	int i =0; 
	@Override
	public void onQuote(Quote quote) {
		if(i==0){
			if(quote.getInstrumentSpecification().getId() == spec1.getId())
				setTargetPosition(quote.getTimeStamp(), quote.getInstrumentSpecification(), 1,quote.getAskPrice()*100+10);
			if(quote.getInstrumentSpecification().getId() == spec2.getId())
				setTargetPosition(quote.getTimeStamp(), quote.getInstrumentSpecification(), 1, quote.getAskPrice()*1000+100);
				
		}
		i++;
		if(i==10){
			if(quote.getInstrumentSpecification().getId() == spec1.getId())
				setTargetPosition(quote.getTimeStamp(), quote.getInstrumentSpecification(), -1,quote.getBidPrice()*100-10);
			if(quote.getInstrumentSpecification().getId() == spec2.getId())
				setTargetPosition(quote.getTimeStamp(), quote.getInstrumentSpecification(), -1, quote.getBidPrice()*1000-100);
			
		}
		if(i==20){
			i = 0; 
		}
		
	}

	public void populateReport(SimpleReport report) {
	}

	@Override
	public void forcedTradingStop() {

	}

	@Override
	public void stop(){
		super.stop();
		saveState();	
	}
	
	public static void main(String[] args) throws Exception {
		InMemoryAlgoEnvConfigRunner runner = (InMemoryAlgoEnvConfigRunner) ServiceLocator
				.instance("data/s15b.xml").getContext()
				.getBean("runner");
		runner.init("org.activequant.tradesystems.s15.AlgoEnvConfigS15b");

	}

}
