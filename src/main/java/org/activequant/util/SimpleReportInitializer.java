package org.activequant.util;

import org.activequant.container.report.SimpleReport;
import org.activequant.optimization.domainmodel.SimulationConfig;

public class SimpleReportInitializer {

	/**
	 * Fills the sim config values into the simple report. 
	 * @param report
	 * @param simConfig
	 */
	public static void initialize(SimpleReport report, SimulationConfig simConfig)
	{
		report.getReportValues().put("SimulationDays", simConfig.getSimulationDays());
		report.getReportValues().put("Instruments", simConfig.getAlgoEnvConfig().getInstruments());
		report.getReportValues().put("StartStopTimes", simConfig.getAlgoEnvConfig().getStartStopTimes());
		report.getReportValues().put("AlgoConfigId", simConfig.getAlgoEnvConfig().getAlgoConfig().getId());
		report.getReportValues().put("AlgoEnvConfigId", simConfig.getAlgoEnvConfig().getId());
		report.getReportValues().put("SimConfigId", simConfig.getId());
		report.getReportValues().put("AlgoConfig", simConfig.getAlgoEnvConfig().getAlgoConfig());
		report.getReportValues().put("Algorithm", simConfig.getAlgoEnvConfig().getAlgoConfig().getAlgorithm());
	}
	
}
