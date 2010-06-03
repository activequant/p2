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
import org.activequant.core.domainmodel.data.Quote;
import org.activequant.dao.IQuoteDao;
import org.activequant.util.exceptions.DaoException;
import org.activequant.util.exceptions.NotImplementedException;

public class RecorderQuoteDao implements IQuoteDao{

	/**
	 * stores the files in a hashmap. 
	 */
	private SimpleDateFormat iso8601date = new SimpleDateFormat("yyyyMMdd");
	private String baseFolder; 
	
	/**
	 * Constructor for this very special quote dao.  
	 * 
	 * @param baseFolder base folder is the folder under which all information is saved.
	 *  
	 */
	public RecorderQuoteDao(String baseFolder)
	{
		this.baseFolder = baseFolder; 
	}
	
	private File getFile(Integer instrumentId, Date date)
	{
		
		// check for the folders ... 
		if(!new File(baseFolder).exists())
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
		File file = new File(baseFolder + File.separator+instrumentId.toString()+File.separator+iso8601date.format(date)+File.separator+"quotes.csv");
		return file;
	}
	
	private BufferedWriter getWriter(Integer instrumentId, Date date) throws IOException
	{
		final String key = instrumentId.toString() + iso8601date.format(date);
		BufferedWriter bw = new BufferedWriter(new FileWriter(getFile(instrumentId, date),true));
		return bw; 
	}
	
	
	@Override
	public void deleteByInstrumentSpecification(InstrumentSpecification instrumentSpecification) throws DaoException {
		throw new NotImplementedException("Not possible in recorder layer.");
	}

	@Override
	public void deleteBySeriesSpecification(SeriesSpecification seriesSpecification) throws DaoException {
		throw new NotImplementedException("Not possible in recorder layer.");
	}

	@Override
	public Quote[] findByInstrumentSpecification(InstrumentSpecification instrumentSpecification) throws DaoException {
		throw new NotImplementedException("Not possible in recorder layer.");
	}

	@Override
	public Quote[] findBySeriesSpecification(SeriesSpecification seriesSpecification) throws DaoException {
		// 		
		throw new NotImplementedException("Not possible in recorder layer.");
	}

	@Override
	public int count() throws DaoException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void delete(Quote entity) throws DaoException {
		throw new NotImplementedException("Not possible in recorder layer.");
	}

	@Override
	public void delete(Quote... entities) throws DaoException {
		throw new NotImplementedException("Not possible in recorder layer.");
	}

	@Override
	public void delete(List<Quote> entities) throws DaoException {
		throw new NotImplementedException("Not possible in recorder layer.");
	}

	@Override
	public void deleteAll() throws DaoException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Quote find(long id) throws DaoException {
		throw new NotImplementedException("Not possible in recorder layer.");
	}

	@Override
	public Quote[] findAll() throws DaoException {
		throw new NotImplementedException("Not possible in recorder layer.");		
	}

	@Override
	public Quote[] findAllByExample(Quote entity) throws DaoException {
		throw new NotImplementedException("Not possible in recorder layer.");
	}

	@Override
	public Quote findByExample(Quote entity) throws DaoException {
		throw new NotImplementedException("Not possible in recorder layer.");
	}

	@Override
	public Quote update(Quote entity) throws DaoException {
		try {
			
			BufferedWriter bw = getWriter(entity.getInstrumentSpecification().getId().intValue(), entity.getTimeStamp().getDate());
			// have to write it all to CSV. 
			bw.write(Long.toString(entity.getTimeStamp().getNanoseconds()));
			bw.write(";");
			bw.write(Double.toString(entity.getBidPrice()));
			bw.write(";");
			bw.write(Double.toString(entity.getBidQuantity()));
			bw.write(";");
			bw.write(Double.toString(entity.getAskPrice()));
			bw.write(";");
			bw.write(Double.toString(entity.getAskQuantity()));
			bw.write(";");
			bw.newLine();
			bw.flush();
			System.out.print(".");
		} catch (IOException e) {
			throw new DaoException(e);
		}		
		return null;
	}

	@Override
	public Quote[] update(Quote... entities) throws DaoException {
		for(Quote entity : entities)
			update(entity);
		return entities;
	}

	@Override
	public List<Quote> update(List<Quote> entities) throws DaoException {
		for(Quote entity : entities)
			update(entity);
		return null;
	}
	
}
