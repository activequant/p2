package org.activequant.tradesystems.sma;

import java.util.ArrayList;
import java.util.List;

import org.activequant.core.types.Tuple;
import org.activequant.optimization.domainmodel.AlgoConfig;
import org.activequant.optimization.domainmodel.AlgoEnvConfig;
import org.activequant.optimization.domainmodel.SimulationConfig;
import org.activequant.optimizations.util.ISimConfigSource;
import org.activequant.util.tools.ArrayUtils;
import org.apache.log4j.Logger;

public class SingleSimConfigSource implements ISimConfigSource {
	
	public List<SimulationConfig> simConfigs() {
		log.info("Creating list if simulation configs ... ");
		List<SimulationConfig> l = new ArrayList<SimulationConfig>();

		SimulationConfig sc = new SimulationConfig();
		l.add(sc);
		sc.setSimulationDays(new Integer[] { 
				20090614,20090615,20090616,20090617,20090618});
		AlgoEnvConfig aec = new AlgoEnvConfig();
		sc.setAlgoEnvConfig(aec);
		List<Tuple<Integer, Integer>> startStopTimes = new ArrayList<Tuple<Integer, Integer>>();
		// UTC !!!
		startStopTimes.add(new Tuple<Integer, Integer>(140000, 180000));
		aec.setStartStopTimes(startStopTimes);
		// local dax.
		aec.setInstruments(ArrayUtils.asList(new Integer[] { 34 }));
		AlgoConfig ac = new AlgoConfig();
		ac.setAlgorithm("org.activequant.tradesystems.sma.Sma1");
		aec.setAlgoConfig(ac);
		ac.put("period1", 30);
		ac.put("period2", 100);

		return l;
	}

	private static Logger log = Logger.getLogger(SingleSimConfigSource.class);
}
