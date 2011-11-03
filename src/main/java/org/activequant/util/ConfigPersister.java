package org.activequant.util;

import java.util.ArrayList;
import java.util.List;

import org.activequant.core.types.Tuple;
import org.activequant.optimization.domainmodel.AlgoConfig;
import org.activequant.optimization.domainmodel.AlgoEnvConfig;
import org.activequant.optimization.domainmodel.SimulationConfig;
import org.activequant.util.tools.ArrayUtils;

public class ConfigPersister {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		AlgoEnvConfig aec = new AlgoEnvConfig();
		
		List<Tuple<Integer, Integer>> startStopTimes = new ArrayList<Tuple<Integer, Integer>>();
		// UTC !!! 
		startStopTimes.add(new Tuple<Integer, Integer>(0, 230000));
		aec.setStartStopTimes(startStopTimes);
		// local dax.
		aec.setInstruments(ArrayUtils.asList(new Integer[] { 34 }));
		AlgoConfig ac = new AlgoConfig();
		ac.setAlgorithm("org.activequant.tradesystems.vlx.Vlx1");
		aec.setAlgoConfig(ac);
		ac.put("factor", 2);
		ac.put("periods", 100);		
		
		
		SimulationConfig simConfig = new SimulationConfig();
		simConfig.setId(100L);
		simConfig.setAlgoEnvConfig(aec);
		simConfig.setSimulationDays(new Integer[]{20090615,20090616,20090617,20090618});
		
		
		SimpleSerializer<SimulationConfig> t = new SimpleSerializer<SimulationConfig>();
		System.out.println(t.serialize(simConfig));
		
		// save it to the resources folder. 
		t.save("./src/main/resources/algoconfigs", simConfig.getId(), simConfig);
		System.out.println("Saved file.");
	}
}
