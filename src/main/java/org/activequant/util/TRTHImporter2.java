package org.activequant.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

class TRTHImporter2 {

	IFactoryDao factoryDao = new FactoryLocatorDao("activequantdao/config.xml");
	ISpecificationDao specDao = factoryDao.createSpecificationDao();
	IQuoteDao quoteDao = factoryDao.createQuoteDao();
	ITradeIndicationDao tradeDao = factoryDao.createTradeIndicationDao();
	protected final static Logger log = Logger.getLogger(TRTHImporter2.class);
	private SimpleDateFormat df = new SimpleDateFormat( "dd-MMM-yyyy HH:mm:ss.S z" );

	public TRTHImporter2(String fileName, Integer instrumentId) throws Exception {
		InstrumentSpecification spec = specDao.find(instrumentId);
		if(spec==null){
			System.out.println("Spec is null.");
			System.exit(0);
		}
		System.out.println("SPEC loaded. ");
		Date dt; 
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String l = br.readLine();
		long formerNanoSeconds = 0L;
		List<Quote> quotes = new ArrayList<Quote>();
		List<TradeIndication> ticks = new ArrayList<TradeIndication>();
		
		
		Double bidPrice=null, askPrice=null, bidSize=null, askSize=null;  
		
		while(l!=null){
//			System.out.println(l);
			if(l.startsWith("#")){
				l = br.readLine();
				continue;
			}
			String[] lineParts = l.split(",",100);
			String date = lineParts[1];
			String time = lineParts[2];
			time = time.substring(0, 12);
			String timeZone = lineParts[3];
			
			String compoundDateTime = date + " "+time +" GMT"+timeZone+":00";
			dt = df.parse(compoundDateTime);
			long nanoSeconds = dt.getTime() * 1000000;
			while(nanoSeconds <= formerNanoSeconds)
				nanoSeconds++;
			formerNanoSeconds = nanoSeconds; 
			
			
			// 
			String qualifiers = lineParts[18];
			
			
			String type = lineParts[4];
			if(type.equals("Quote") && qualifiers.startsWith("BBO")){
				
				String bp = lineParts[11];
				String bs = lineParts[12];
				String ap = lineParts[15];
				String as = lineParts[16];
						
				if(bp.length()>0)
					bidPrice = Double.parseDouble(bp);
				if(bs.length()>0)
					bidSize = Double.parseDouble(bs);
				if(ap.length()>0)
					askPrice = Double.parseDouble(ap);
				if(as.length()>0)
					askSize = Double.parseDouble(as);
				Quote q = new Quote();
				q.setTimeStamp(new TimeStamp(nanoSeconds));
				if(bidPrice!=null)
					q.setBidPrice(bidPrice);
				if(askPrice!=null)
					q.setAskPrice(askPrice);
				if(bidSize!=null)
					q.setBidQuantity(bidSize);
				if(askSize!=null)
					q.setAskQuantity(askSize);
				q.setInstrumentSpecification(spec);
				quotes.add(q);
				if(quotes.size()==1000){
					quoteDao.update(quotes);
					quotes.clear();
					System.out.print("Q");
				}				
			}
			/*else if(type.equals("Trade")){
				Double tradePrice = Double.parseDouble(lineParts[5]);
				Double tradeVol = Double.parseDouble(lineParts[6]);
				TradeIndication ti = new TradeIndication(spec);
				ti.setPrice(tradePrice);
				ti.setQuantity(tradeVol);
				ti.setTimeStamp(new TimeStamp(nanoSeconds));
				ticks.add(ti);
				if(ticks.size()==1000)
				{
					tradeDao.update(ticks);
					ticks.clear();
					System.out.print("T");
				}
			}
			
			*/
			
			
			l = br.readLine();
		}
		quoteDao.update(quotes);
		tradeDao.update(ticks);
	}

	
	public static void main(String[] args) throws Exception {
		new TRTHImporter2(args[0], Integer.parseInt(args[1]));		
	}
}
