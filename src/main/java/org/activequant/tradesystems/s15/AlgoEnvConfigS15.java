package org.activequant.tradesystems.s15;

import java.util.ArrayList;
import java.util.List;

import org.activequant.core.types.Tuple;
import org.activequant.optimization.domainmodel.AlgoConfig;
import org.activequant.optimization.domainmodel.AlgoEnvConfig;
import org.activequant.util.tools.ArrayUtils;

public class AlgoEnvConfigS15 extends AlgoEnvConfig {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AlgoEnvConfigS15()
	{
		List<Tuple<Integer, Integer>> startStopTimes = new ArrayList<Tuple<Integer, Integer>>();
		// UTC !!!
		startStopTimes.add(new Tuple<Integer, Integer>(0, 235959));
		this.setStartStopTimes(startStopTimes);
		
		// set, to which instrument to subscribe to. 
		// local instrument id, can differ across machines. 
		setInstruments(ArrayUtils.asList(new Integer[] { 284, 304 }));
		
		// create algo config and set it. 
		AlgoConfig ac = new AlgoConfig();
		ac.setAlgorithm("org.activequant.tradesystems.s15.S15");
		ac.put("period1", 3);
		ac.put("period2", 10);
		
		
		
		setAlgoConfig(ac);
	}
	
}
