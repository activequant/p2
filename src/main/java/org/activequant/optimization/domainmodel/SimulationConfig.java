package org.activequant.optimization.domainmodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Ghost Rider
 *
 */
public class SimulationConfig implements Serializable {
	List<Integer> simulationDays = new ArrayList<Integer>();
	AlgoEnvConfig algoEnvConfig;
	Long id; 
	private static final long serialVersionUID = 1L;
	public List<Integer> getSimulationDays() {
		return simulationDays;
	}

	public void setSimulationDays(List<Integer> simulationDays) {
		this.simulationDays = simulationDays;
	}
	
	public void setSimulationDays(Integer[] simulationDays) {
		this.simulationDays.clear();
		for(Integer i : simulationDays)
			this.simulationDays.add(i);
	}
	
	public AlgoEnvConfig getAlgoEnvConfig() {
		return algoEnvConfig;
	}

	public void setAlgoEnvConfig(AlgoEnvConfig algoEnvConfig) {
		this.algoEnvConfig = algoEnvConfig;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	} 
}
