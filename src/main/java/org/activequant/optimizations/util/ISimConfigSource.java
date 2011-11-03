package org.activequant.optimizations.util;

import java.util.List;

import org.activequant.optimization.domainmodel.SimulationConfig;

public interface ISimConfigSource {

	public abstract List<SimulationConfig> simConfigs();

}