package org.activequant.optimization;

import java.util.ArrayList;
import java.util.List;

import org.activequant.backtesting.SingularBacktester;
import org.activequant.container.report.SimpleReport;
import org.activequant.dao.IFactoryDao;
import org.activequant.dao.ISpecificationDao;
import org.activequant.dao.hibernate.FactoryLocatorDao;
import org.activequant.optimization.domainmodel.BatchConfig;
import org.activequant.optimization.domainmodel.SimulationConfig;
import org.activequant.optimizations.util.ISimConfigSource;
import org.activequant.reporting.BatchSimReportWriter;
import org.activequant.reporting.VoidValueReporter;
import org.activequant.util.CsvArchiveQuoteSourceWrapper;
import org.activequant.util.spring.ServiceLocator;
import org.apache.log4j.Logger;


/**
 * Requires a srping batch runner config file as main parameter. 
 * Use ./src/main/resources/data/batchrunner.xml as a start. 
 * 
 * @author GhostRider
 *
 */
public class BatchRunner {

	private static Logger log = Logger.getLogger(BatchRunner.class);
	IFactoryDao factoryDao = new FactoryLocatorDao("activequantdao/config.xml");
	ISpecificationDao specDao = factoryDao.createSpecificationDao();

	public BatchRunner(String algoConfigSourceClass, String batchReportTargetFileName, String archiveFolder ) throws Exception {
		BatchConfig bc = new BatchConfig();
		bc.setAlgoConfigSourceClass(algoConfigSourceClass);
		bc.setBatchReportTargetFileName(batchReportTargetFileName);
		bc.setArchiveFolder(archiveFolder);
		batchRun(bc);
	}

	@SuppressWarnings("unchecked")
	public void batchRun(BatchConfig config) throws Exception {
		log.info("Starting batch run ... ");

		SingularBacktester b = new SingularBacktester();
		List<SimpleReport> reports = new ArrayList<SimpleReport>();
		//
		Class clazz = Class.forName(config.getAlgoConfigSourceClass());
		ISimConfigSource source = (ISimConfigSource) clazz.newInstance();
		List<SimulationConfig> simConfigs = source.simConfigs();
		log.info("Instantiated sim config source (" + config.getAlgoConfigSourceClass() + ") and obtained " + simConfigs.size()
				+ " simulation configs.");

		for (int i = 0; i < simConfigs.size(); i++) {

			// 
			CsvArchiveQuoteSourceWrapper csvQuoteSource = new CsvArchiveQuoteSourceWrapper(config.getArchiveFolder());
			csvQuoteSource.setDelimiter(";");
			log.info("Backtesting SimConfig #" + (i + 1) + " of " + (simConfigs.size()));
			SimulationConfig s = simConfigs.get(i);
			SimpleReport report = b.simulate(null);
			reports.add(report);
		}

		// generate the report.
		new BatchSimReportWriter(config).write(reports);

	}

	public static void main(String[] args) throws Exception {
		ServiceLocator.instance(args[0]).getContext().getBean("runner");
	}

}
