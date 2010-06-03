package org.activequant.optimizations.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.activequant.optimization.domainmodel.SimulationConfig;
import org.activequant.util.SimpleSerializer;

import common.Logger;

/**
 * Loads all sim configs from a folder and returns these as a list.
 *  
 * @author Ghost Rider
 *
 */
public class SimConfigFilesSource implements ISimConfigSource {

	private static Logger log = Logger.getLogger(SimConfigFilesSource.class);
	private String folder; 
	
	public List<SimulationConfig> simConfigs() {
		log.info("Loading list if simulation configs from folder "+folder+"... ");
		List<SimulationConfig> l = new ArrayList<SimulationConfig>();
		SimpleSerializer<SimulationConfig> ser = new SimpleSerializer<SimulationConfig>();
		String[] files = new File(folder).list();
		for(String fileName : files)
		{
			if(fileName.endsWith(".json"))
			{
				try {
					l.add(ser.load(folder, Long.parseLong(fileName.substring(0, fileName.indexOf(".")))));
				}
				catch(Exception ex) {
					log.warn("Error while loading file : "+fileName, ex);
				}
			}
		}		
		return l;
	}

	public SimConfigFilesSource(String folder)
	{
		this.folder = folder; 
	}
	
}
