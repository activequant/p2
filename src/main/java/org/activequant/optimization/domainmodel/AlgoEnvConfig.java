package org.activequant.optimization.domainmodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.activequant.core.types.Tuple;

/**
 * 
 * @author Ghost Rider
 *
 */
public class AlgoEnvConfig  implements Serializable  {
	private List<Integer> instruments = new ArrayList<Integer>();
	private List<Tuple<Integer, Integer>> startStopTimes = new ArrayList<Tuple<Integer, Integer>>(); 
	private AlgoConfig algoConfig;
	private Long id; 
	private static final long serialVersionUID = 1L;
	public List<Integer> getInstruments() {
		return instruments;
	}
	public void setInstruments(List<Integer> instruments) {
		this.instruments = instruments;
	}
	public List<Tuple<Integer, Integer>> getStartStopTimes() {
		return startStopTimes;
	}
	public void setStartStopTimes(List<Tuple<Integer, Integer>> startStopTimes) {
		this.startStopTimes = startStopTimes;
	}
	public AlgoConfig getAlgoConfig() {
		return algoConfig;
	}
	public void setAlgoConfig(AlgoConfig algoConfig) {
		this.algoConfig = algoConfig;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getId() {
		return id;
	}
}
