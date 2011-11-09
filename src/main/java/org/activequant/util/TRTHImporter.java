package org.activequant.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.data.Quote;
import org.activequant.core.domainmodel.data.TradeIndication;
import org.activequant.core.types.TimeStamp;
import org.activequant.dao.IFactoryDao;
import org.activequant.dao.IQuoteDao;
import org.activequant.dao.ISpecificationDao;
import org.activequant.dao.ITradeIndicationDao;
import org.activequant.dao.hibernate.FactoryLocatorDao;
import org.apache.log4j.Logger;

class TRTHImporter {

	IFactoryDao factoryDao = new FactoryLocatorDao("activequantdao/config.xml");
	ISpecificationDao specDao = factoryDao.createSpecificationDao();
	IQuoteDao quoteDao = factoryDao.createQuoteDao();
	ITradeIndicationDao tradeDao = factoryDao.createTradeIndicationDao();
	protected final static Logger log = Logger.getLogger(TRTHImporter.class);
	private SimpleDateFormat df = new SimpleDateFormat( "dd-MMM-yyyy HH:mm:ss.S" );

	public TRTHImporter(String fileName, Integer instrumentId) throws Exception {
		InstrumentSpecification spec = specDao.find(instrumentId);
		if(spec==null){
			System.out.println("Spec is null.");
			System.exit(0);
		}
		System.out.println("SPEC loaded. ");
		Date dt; 
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String l = br.readLine();
			
		while(l!=null){
			System.out.println(l);
			if(l.startsWith("#")){
				l = br.readLine();
				continue;
			}
			String[] lineParts = l.split(",");
			String date = lineParts[1];
			String time = lineParts[2];
			String timeZone = lineParts[3];
			df.setTimeZone(TimeZone.getTimeZone("GMT "+timeZone+":00"));
			String compoundDateTime = date + " "+time;
			dt = df.parse(compoundDateTime);
			
			String type = lineParts[4];
			
			if(type.equals("Quote")){
				Double bidPrice = Double.parseDouble(lineParts[8]);
				Double bidSize = Double.parseDouble(lineParts[9]);
				Double askPrice = Double.parseDouble(lineParts[10]);
				Double askSize = Double.parseDouble(lineParts[11]);
				Quote q = new Quote();
				q.setTimeStamp(new TimeStamp(dt));
				q.setBidPrice(bidPrice);
				q.setAskPrice(askPrice);
				q.setBidQuantity(bidSize);
				q.setAskQuantity(askSize);
				q.setInstrumentSpecification(spec);
				quoteDao.update(q);
				System.out.print("Q");
			}
			else if(type.equals("Trade")){
				Double tradePrice = Double.parseDouble(lineParts[5]);
				Double tradeVol = Double.parseDouble(lineParts[6]);
				TradeIndication ti = new TradeIndication(spec);
				ti.setPrice(tradePrice);
				ti.setQuantity(tradeVol);
				ti.setTimeStamp(new TimeStamp(dt));
				tradeDao.update(ti);
				System.out.print("T");
			}
			
			
			
			
			l = br.readLine();
		}
	}

	
	public static void main(String[] args) throws Exception {
		new TRTHImporter(args[0], Integer.parseInt(args[1]));		
	}
}
