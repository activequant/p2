package org.activequant.backtesting;

import java.io.File;

import org.activequant.container.report.SimpleReport;
import org.activequant.dao.IFactoryDao;
import org.activequant.dao.ISpecificationDao;
import org.activequant.dao.hibernate.FactoryLocatorDao;
import org.activequant.optimization.domainmodel.SimulationConfig;
import org.activequant.reporting.ReportToKeyValue;
import org.activequant.reporting.VoidValueReporter;
import org.activequant.util.CsvArchiveQuoteSourceWrapper;
import org.activequant.util.SimpleSerializer;
import org.apache.log4j.Logger;

/**
 * The SimConfigFileBacktester will load a simulation configuration file and simulate it. 
 * It uses the SimpleSerializer to load this application.  
 * 
 * @author GhostRider
 *
 */
public class SimConfigFileBacktester {

	private static Logger log = Logger.getLogger(SimConfigFileBacktester.class);
	IFactoryDao factoryDao = new FactoryLocatorDao("activequantdao/config.xml");
	ISpecificationDao specDao = factoryDao.createSpecificationDao();
	
	
	public SimConfigFileBacktester(String[] a) throws Exception 
	{		
		log.info("Simulating algo env config file "+a[0]+"/"+a[1]);
		log.info("Archive folder : "+a[2]);
		log.info("Log, Report and Chart folder : "+a[3]);
		SimulationConfig simConfig = new SimpleSerializer<SimulationConfig>().load(a[0], Integer.parseInt(a[1]));
		System.out.println(simConfig);

		// 
		CsvArchiveQuoteSourceWrapper csvQuoteSource = new CsvArchiveQuoteSourceWrapper(a[2]);
		csvQuoteSource.setDelimiter(";");
		
		SingularBacktester b = new SingularBacktester();
		SimpleReport report = null;// b.simulate(a[3]);
		
		String reportFileName = a[3]+File.separator+System.currentTimeMillis()+".html";
		log.info("Generating report to "+reportFileName);
		
		new ReportToKeyValue().render(reportFileName, report);

	}
	
	public static void printUsage(String[] a)
	{
		System.out.println("= = = = = = = = = = = = = = = = = = = =");
		System.out.println("Usage: ");
		System.out.println(" java org.activequant.backtesting.SimConfigFileBacktester <configfolder> <configid> <archivefolder> <reportFileName>");
		System.out.println("where: ");
		System.out.println(" configfolder is the folder containing the configuration file ");
		System.out.println(" configid is the configuration number, ie. something like 10350 ");
		System.out.println(" archivefolder is the data archive folder ");
		System.out.println(" reportFileName is the folder where the log and charts are written to");
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
			new SimConfigFileBacktester(a);
		}
		catch(Exception anEx)
		{
			anEx.printStackTrace();
			printUsage(a);
		}
	}
	
}
