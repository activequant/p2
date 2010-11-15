package org.activequant.tradesystems.system5;

import java.util.ArrayList;
import java.util.List;

import org.activequant.core.types.Tuple;
import org.activequant.optimization.domainmodel.AlgoConfig;
import org.activequant.optimization.domainmodel.AlgoEnvConfig;
import org.activequant.util.tools.ArrayUtils;

public class AlgoEnvConfigSystem5 extends AlgoEnvConfig {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AlgoEnvConfigSystem5()
	{
		List<Tuple<Integer, Integer>> startStopTimes = new ArrayList<Tuple<Integer, Integer>>();
		// UTC !!!
		startStopTimes.add(new Tuple<Integer, Integer>(0, 235959));
		this.setStartStopTimes(startStopTimes);
		
		// set, to which instrument to subscribe to. 
		// local instrument id, can differ across machines. 
		setInstruments(ArrayUtils.asList(new Integer[] { 85 }));
		
		// create algo config and set it. 
		AlgoConfig ac = new AlgoConfig();
		ac.setAlgorithm("org.activequant.tradesystems.system5.System5");
		
		setAlgoConfig(ac);
	}
	
}
