package org.activequant.util;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.SeriesSpecification;
import org.activequant.core.domainmodel.data.Candle;
import org.activequant.core.types.TimeFrame;
import org.activequant.core.types.TimeStamp;
import org.activequant.dao.ICandleDao;
import org.activequant.util.exceptions.DaoException;

public class RecorderCandleDao  implements ICandleDao {
	private SimpleDateFormat iso8601date = new SimpleDateFormat("yyyyMMdd");
	private String baseFolder; 
	private HashMap<String, BufferedWriter> writers = new HashMap<String, BufferedWriter>();
	
	public RecorderCandleDao(String baseFolder)
	{
		System.out.println("Using base folder: "+baseFolder);
		this.baseFolder = baseFolder; 
	}

	private File getFile(Long instrumentId, Date date, TimeFrame timeFrame)
	{		
		// check for the folders ... 
		if(!baseFolder.equals(".") && !new File(baseFolder).exists())
		{
			new File(baseFolder).mkdir();
		}
		if(!new File(baseFolder + File.separator+instrumentId.toString()).exists())
		{
			new File(baseFolder + File.separator+instrumentId.toString()).mkdir();
		}
		if(!new File(baseFolder + File.separator+instrumentId.toString()+File.separator+iso8601date.format(date)).exists())
		{
			new File(baseFolder + File.separator+instrumentId.toString()+File.separator+iso8601date.format(date)).mkdir();
		}
			
		// have to instantiate that file.
		File file = new File(baseFolder + File.separator+instrumentId.toString()+File.separator+iso8601date.format(date)+File.separator
				+"candles_"+timeFrame.toString()+".csv");
		return file;
	}
	
	private BufferedWriter getWriter(Long instrumentId, Date date, TimeFrame timeFrame) throws IOException
	{
		
		final String key = instrumentId.toString() + iso8601date.format(date)+timeFrame.toString();
		if(writers.containsKey(key))
			return writers.get(key);		
		BufferedWriter bw = new BufferedWriter(new FileWriter(getFile(instrumentId, date, timeFrame),true));
		writers.put(key, bw);
		return bw; 
	}
	
	public static List<Date> daysBetweenDates(Date fechaInicial, Date fechaFinal)
	{
	    List<Date> dates = new ArrayList<Date>();
	    Calendar calendar = new GregorianCalendar();
	    calendar.setTime(fechaInicial);

	    while (calendar.getTime().before(fechaFinal))
	    {
		Date resultado = calendar.getTime();
		dates.add(resultado);
		calendar.add(Calendar.DATE, 1);
	    }
	    return dates;
	}

	private List<Candle> loadDay(InstrumentSpecification iSpec, Date date, TimeFrame timeFrame) throws DaoException {
		try{
			BufferedReader br = getReader(iSpec.getId(), date, timeFrame);
			List<Candle> ret = new ArrayList<Candle>();
			String l = br.readLine();
			while(l!=null) {
				String[] splits = l.split(";");
				Long nanos = Long.parseLong(splits[0]);	
				Double open = Double.parseDouble(splits[1]);
				Double high = Double.parseDouble(splits[2]);
				Double low = Double.parseDouble(splits[3]);
				Double close = Double.parseDouble(splits[4]);
				Double vol = Double.parseDouble(splits[5]);
				Candle c = new Candle(iSpec, new TimeStamp(nanos), open, high, low, close, vol, timeFrame);	
				ret.add(c);
			}		
			return ret; 
		}
		catch(IOException ex)
		{
			throw new DaoException(ex);
		}
	}

	private BufferedReader getReader(Long instrumentId, Date date, TimeFrame timeFrame) throws IOException
	{
		
		final String key = instrumentId.toString() + iso8601date.format(date)+timeFrame.toString();
		BufferedReader br = new BufferedReader(new FileReader(getFile(instrumentId, date, timeFrame)));
		return br; 
	}
	
	@Override
	public void deleteByInstrumentSpecification(
			InstrumentSpecification instrumentSpecification)
			throws DaoException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteBySeriesSpecification(
			SeriesSpecification seriesSpecification) throws DaoException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Candle[] findByInstrumentSpecification(
			InstrumentSpecification instrumentSpecification)
			throws DaoException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Candle[] findBySeriesSpecification(
			SeriesSpecification seriesSpecification) throws DaoException {
		TimeFrame timeFrame = seriesSpecification.getTimeFrame();
		InstrumentSpecification is = seriesSpecification.getInstrumentSpecification();
		TimeStamp startTimeStamp = seriesSpecification.getStartTimeStamp();
		TimeStamp endTimeStamp = seriesSpecification.getEndTimeStamp();
		// have to iterate over the days and load the candle files individually. 
		List<Date> dates = daysBetweenDates(startTimeStamp.getDate(), endTimeStamp.getDate());
		List<Candle> candles = new ArrayList<Candle>();
		for(Date d : dates)
		{	
			candles = loadDay(is, d, timeFrame);
		}
		return candles.toArray(new Candle[]{});
	}

	@Override
	public int count() throws DaoException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void delete(Candle entity) throws DaoException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(Candle... entities) throws DaoException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(List<Candle> entities) throws DaoException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAll() throws DaoException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Candle find(long id) throws DaoException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Candle[] findAll() throws DaoException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Candle[] findAllByExample(Candle entity) throws DaoException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Candle findByExample(Candle entity) throws DaoException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Candle update(Candle entity) throws DaoException {
	try {
			//System.out.print(entity);
			BufferedWriter bw = getWriter(entity.getInstrumentSpecification().getId(), entity.getTimeStamp().getDate(), entity.getTimeFrame());
			// have to write it all to CSV. 
			bw.write(Long.toString(entity.getTimeStamp().getNanoseconds()));
			bw.write(";");
			bw.write(Double.toString(entity.getOpenPrice()));
			bw.write(";");
			bw.write(Double.toString(entity.getHighPrice()));
			bw.write(";");
			bw.write(Double.toString(entity.getLowPrice()));
			bw.write(";");
			bw.write(Double.toString(entity.getClosePrice()));
			bw.write(";");
			bw.write(Double.toString(entity.getVolume()));
			bw.write(";");
			bw.newLine();
			bw.flush();
			
		} catch (IOException e) {
			throw new DaoException(e);
		}		
		return entity;
	}

	@Override
	public Candle[] update(Candle... entities) throws DaoException {
		for(Candle entity : entities)
			update(entity);
		return null;
	}

	@Override
	public List<Candle> update(List<Candle> entities) throws DaoException {
		for(Candle entity : entities)
			update(entity);
		return null;
	}

}
