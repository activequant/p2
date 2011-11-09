package org.activequant.tradesystems.s15;

import org.activequant.backtesting.SingularBacktester;
import org.activequant.optimization.domainmodel.AlgoEnvConfig;
import org.activequant.optimization.domainmodel.SimulationConfig;
import org.activequant.util.DateUtils;
import org.activequant.util.spring.ServiceLocator;

public class BacktestS15 {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		SingularBacktester runner = (SingularBacktester) ServiceLocator
				.instance("data/backtestS15.xml").getContext()
				.getBean("runner");
		SimulationConfig sc = new SimulationConfig();
		sc.setSimulationDays( DateUtils.createDateRange(20111020, 20111028).toArray(new Integer[]{}));
		sc.setId(1L);
		AlgoEnvConfig aec = new AlgoEnvConfigS15();
		sc.setAlgoEnvConfig(aec);

		runner.simulate(sc);

	}

}
