package org.activequant.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.SeriesSpecification;
import org.activequant.core.domainmodel.data.Candle;
import org.activequant.core.types.TimeFrame;
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

	private File getFile(Integer instrumentId, Date date, TimeFrame timeFrame)
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
	
	private BufferedWriter getWriter(Integer instrumentId, Date date, TimeFrame timeFrame) throws IOException
	{
		
		final String key = instrumentId.toString() + iso8601date.format(date)+timeFrame.toString();
		if(writers.containsKey(key))
			return writers.get(key);		
		BufferedWriter bw = new BufferedWriter(new FileWriter(getFile(instrumentId, date, timeFrame),true));
		writers.put(key, bw);
		return bw; 
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
		// TODO Auto-generated method stub
		return null;
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
			BufferedWriter bw = getWriter(entity.getInstrumentSpecification().getId().intValue(), entity.getTimeStamp().getDate(), entity.getTimeFrame());
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
		return null;
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
