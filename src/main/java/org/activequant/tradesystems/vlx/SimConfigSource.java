package org.activequant.tradesystems.vlx;

import java.util.ArrayList;
import java.util.List;

import org.activequant.core.types.Tuple;
import org.activequant.optimization.domainmodel.AlgoConfig;
import org.activequant.optimization.domainmodel.AlgoEnvConfig;
import org.activequant.optimization.domainmodel.SimulationConfig;
import org.activequant.optimizations.util.ISimConfigSource;
import org.activequant.util.tools.ArrayUtils;
import org.apache.log4j.Logger;

public class SimConfigSource implements ISimConfigSource {
	public List<SimulationConfig> simConfigs() {
		log.info("Creating list if simulation configs ... ");
		Long runningId = 0L;
		List<SimulationConfig> l = new ArrayList<SimulationConfig>();
		for (int i = 1; i < 10; i++) {
			for (int j = 2; j < 50; j++) {
				
				SimulationConfig sc = new SimulationConfig();
				sc.setId(runningId);
				l.add(sc);
				sc.setSimulationDays(new Integer[] { 
						20090618});
				AlgoEnvConfig aec = new AlgoEnvConfig();
				sc.setAlgoEnvConfig(aec);
				List<Tuple<Integer, Integer>> startStopTimes = new ArrayList<Tuple<Integer, Integer>>();
				// UTC !!!
				startStopTimes.add(new Tuple<Integer, Integer>(80000, 200000));
				aec.setStartStopTimes(startStopTimes);
				// local dax.
				aec.setInstruments(ArrayUtils.asList(new Integer[] { 34 }));
				AlgoConfig ac = new AlgoConfig();
				ac.setAlgorithm("org.activequant.tradesystems.vlx.Vlx1");
				aec.setAlgoConfig(ac);
				ac.put("factor", i);
				ac.put("periods", j * 10);
				runningId++;
			}
		}
		return l;
	}

	private static Logger log = Logger.getLogger(SimConfigSource.class);
}
