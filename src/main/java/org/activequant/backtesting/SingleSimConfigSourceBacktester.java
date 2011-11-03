package org.activequant.backtesting;

import java.io.File;

import org.activequant.container.report.SimpleReport;
import org.activequant.dao.IFactoryDao;
import org.activequant.dao.ISpecificationDao;
import org.activequant.dao.hibernate.FactoryLocatorDao;
import org.activequant.optimization.domainmodel.SimulationConfig;
import org.activequant.optimizations.util.ISimConfigSource;
import org.activequant.reporting.CsvFileValueReporter;
import org.activequant.reporting.ReportToKeyValue;
import org.activequant.util.CsvArchiveQuoteSourceWrapper;
import org.apache.log4j.Logger;

/**
 * The SingleSimConfigFileBacktester takes an ISimConfigSource isntance and 
 * retrieves a simulation configuration from it and simulates it. 
 * This class does not take a file, but works fully in memory.
 * 
 * @author GhostRider
 *
 */
public class SingleSimConfigSourceBacktester {

	private static Logger log = Logger.getLogger(SingleSimConfigSourceBacktester.class);
	IFactoryDao factoryDao = new FactoryLocatorDao("activequantdao/config.xml");
	ISpecificationDao specDao = factoryDao.createSpecificationDao();

	@SuppressWarnings("unchecked")
	public SingleSimConfigSourceBacktester(String[] a) throws Exception 
	{		
		log.info("Archive folder : "+a[0]);
		log.info("Log, Report and Chart folder : "+a[1]);
		log.info("Single Sim Config Source Class "+a[2]);
		Class<ISimConfigSource> clazz = (Class<ISimConfigSource>)Class.forName(a[2]);
		ISimConfigSource source = clazz.newInstance();
		SimulationConfig simConfig = source.simConfigs().get(0);
		
		System.out.println(simConfig);

		// 
		CsvArchiveQuoteSourceWrapper csvQuoteSource = new CsvArchiveQuoteSourceWrapper(a[0]);
		csvQuoteSource.setDelimiter(";");
		
		CsvFileValueReporter valueReporter = new CsvFileValueReporter("./values.csv");
		SingularBacktester b = null;//new SingularBacktester(specDao, valueReporter);
		SimpleReport report = null;//b.simulate(a[1], simConfig, csvQuoteSource);
		String reportFileName = a[1]+File.separator+System.currentTimeMillis()+".html";
		log.info("Generating report to "+reportFileName);
		new ReportToKeyValue().render(reportFileName, report);
		valueReporter.flush();
		
	}
	
	public static void printUsage(String[] a)
	{
		System.out.println("= = = = = = = = = = = = = = = = = = = =");
		System.out.println("Usage: ");
		System.out.println(" java org.activequant.backtesting.SimConfigFileBacktester <archivefolder> <report folder name> <simconfigsourceclass>");
		System.out.println("where: ");
		System.out.println(" archivefolder is the data archive folder ");
		System.out.println(" reportFolderName is the folder where the log and charts are written to (has to exist)");
		System.out.println(" simconfigsourceclass is the class name where to get the sim config from");
		System.out.println("as a return the simple report is printed, which will contain values of some sort. ");
		System.out.println("= = = = = = = = = = = = = = = = = = = =");
		System.out.println("Your input: ");
		for(String s : a)
		{
			System.out.println(" Parameter: "+s);
		}
		System.out.println("= = = = = = = = = = = = = = = = = = = =");
		
	}
	

	public static void main(String[] a) throws Exception
	{
		try{
			new SingleSimConfigSourceBacktester(a);
		}
		catch(Exception anEx)
		{
			anEx.printStackTrace();
			printUsage(a);
		}
	}
	
	
}
