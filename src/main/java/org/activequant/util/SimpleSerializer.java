package org.activequant.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.activequant.core.types.Tuple;
import org.activequant.optimization.domainmodel.AlgoConfig;
import org.activequant.optimization.domainmodel.AlgoEnvConfig;
import org.activequant.optimization.domainmodel.SimulationConfig;
import org.activequant.util.tools.ArrayUtils;

import com.thoughtworks.xstream.XStream;

/**
 * Serializes and deserializes an object to a folder with an id in json format. 
 * 
 * @author Ghost Rider
 *
 */
public class SimpleSerializer<T> {
	
	/**
	 * 
	 */
	public SimpleSerializer()
	{
		
	}
	
	public String serialize(T object)
	{
		XStream jsonSer = new XStream();
		String jsonStr = (String) jsonSer.toXML(object);
		return jsonStr;
	}
	
	@SuppressWarnings("unchecked")
	public T deserialize(String string)
	{
		XStream jsonSer = new XStream();
		T t = (T)jsonSer.fromXML(string);
		return t;
	}
	
	public void save(String targetFolder, long id, T t) throws Exception
	{		
		File file = new File(targetFolder, id+".xml");
		FileOutputStream fout = new FileOutputStream(file);
		fout.write(serialize(t).getBytes());
	}
	
	public T load(String sourceFolder, long id) throws Exception
	{
		File file = new File(sourceFolder, id+".xml");
		FileInputStream fin = new FileInputStream(file);
		byte[] bytes = new byte[(int)file.length()];
		fin.read(bytes);
		String jsonData = new String(bytes);
		return deserialize(jsonData);
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		SimulationConfig c = new SimulationConfig();
		c.setId(1L);
		c.setSimulationDays(new Integer[]{200, 201, 202});
		
		AlgoEnvConfig aec = new AlgoEnvConfig();
		aec.setId(10L);
		List<Tuple<Integer, Integer>> startStopTimes = new ArrayList<Tuple<Integer, Integer>>();
		// UTC !!! 
		startStopTimes.add(new Tuple<Integer, Integer>(0, 235959));
		aec.setStartStopTimes(startStopTimes);
		// local dax.
		aec.setInstruments(ArrayUtils.asList(new Integer[] { 43 }));
		
		c.setAlgoEnvConfig(aec);
		
		AlgoConfig ac = new AlgoConfig();
		ac.setAlgorithm("org.activequant.tradesystems.vlx.Vlx1");
		aec.setAlgoConfig(ac);
		ac.put("factor", 2);
		ac.put("periods", 100);
		
		SimpleSerializer<AlgoEnvConfig> t = new SimpleSerializer<AlgoEnvConfig>();
		System.out.println(t.serialize(aec));
		
		// save it to the resources folder. 
		t.save("./src/main/resources/algoconfigs", 100L, aec);
	}

}
