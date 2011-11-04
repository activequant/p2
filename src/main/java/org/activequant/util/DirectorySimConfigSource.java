package org.activequant.util;

import java.util.ArrayList;
import java.util.List;

import org.activequant.core.types.Tuple;
import org.activequant.optimization.domainmodel.AlgoConfig;
import org.activequant.optimization.domainmodel.AlgoEnvConfig;
import org.activequant.optimization.domainmodel.SimulationConfig;
import org.activequant.util.tools.ArrayUtils;
import org.apache.log4j.Logger;

/**
 * TODO: WIP
 * 
 * @author GhostRider
 *
 */
public class DirectorySimConfigSource {
	public List<SimulationConfig> simConfigs() {
		log.info("Creating list if simulation configs ... ");
		List<SimulationConfig> l = new ArrayList<SimulationConfig>();
		for (int i = 10; i < 50; i++) {
			for (double j = 1; j < 20; j++) {
				SimulationConfig sc = new SimulationConfig();
				l.add(sc);
				sc.setSimulationDays(new Integer[] {20090615,20090616,20090617,20090618});
				AlgoEnvConfig aec = new AlgoEnvConfig();
				sc.setAlgoEnvConfig(aec);
				List<Tuple<Integer, Integer>> startStopTimes = new ArrayList<Tuple<Integer, Integer>>();
				// UTC !!! 
				startStopTimes.add(new Tuple<Integer, Integer>(0, 235959));
				aec.setStartStopTimes(startStopTimes);
				// local dax.
				aec.setInstruments(ArrayUtils.asList(new Integer[] { 34 }));
				AlgoConfig ac = new AlgoConfig();
				ac.setAlgorithm("org.activequant.applications.MomentumTradeSystem");
				aec.setAlgoConfig(ac);
				ac.put("n", i * 10);
				ac.put("thresh", j / 2000.0);
			}
		}
		return l;
	}

	private static Logger log = Logger.getLogger(DirectorySimConfigSource.class);
}
