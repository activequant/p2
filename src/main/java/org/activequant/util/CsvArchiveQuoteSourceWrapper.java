package org.activequant.util;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.activequant.core.domainmodel.SeriesSpecification;
import org.activequant.core.domainmodel.data.Quote;
import org.activequant.data.retrieval.series.CsvQuoteIteratorSource;
import org.apache.log4j.Logger;

public class CsvArchiveQuoteSourceWrapper extends CsvQuoteIteratorSource {

	private SeriesSpecification spec;
	private String baseFolder;
	private static Logger log = Logger.getLogger(CsvArchiveQuoteSourceWrapper.class);

	public CsvArchiveQuoteSourceWrapper(String baseFolder) {
		this.baseFolder = baseFolder;
	}

	private class FileCombiner implements Iterable<Quote> {
		private String[] quoteFileNames;

		FileCombiner(String... quoteFiles) {
			this.quoteFileNames = quoteFiles;
		}

		private int currentFileIndex = 0;

		@Override
		public Iterator<Quote> iterator() {
			class LocalIterator implements Iterator<Quote> {
				CsvQuoteIteratorSource currentSource = null;
				Iterator<Quote> quoteIterator = null;
				private String currentFileName = ""; 
				private void nextFile() throws Exception {
					//
					currentSource = new CsvQuoteIteratorSource();
					currentSource.setDelimiter(";");
					// check if file exists.
					while (currentFileIndex < quoteFileNames.length+1) {
						currentFileName = quoteFileNames[currentFileIndex];
						if (new File(quoteFileNames[currentFileIndex]).exists()) {
							currentSource.setFileName(quoteFileNames[currentFileIndex]);
							quoteIterator = currentSource.fetch(spec).iterator();
							currentFileIndex++;
							break;
						}
						currentFileIndex++;
					}
				}

				@Override
				public boolean hasNext() {
					try {
						if (currentSource == null) {
							nextFile();
						}
						if (!quoteIterator.hasNext()) {
							// check if we are at the last filename to use
							// already.
							if (currentFileIndex == quoteFileNames.length) {
								// last file and no more data.
								return false;
							} else {
								// roll over to next file.
								nextFile();
							}
						}
						return quoteIterator.hasNext();
					} catch (Exception anEx) {
						throw new RuntimeException("Problem merging files ("+currentFileName+")! " + anEx.getMessage(), anEx);
					}
				}

				@Override
				public Quote next() {
					// called to check the merging.
					hasNext();
					// 
					return quoteIterator.next();
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			}
			return new LocalIterator();
		}
	}

	@Override
	public Iterable<Quote> fetch(SeriesSpecification spec) throws Exception {
		// 
		this.spec = spec;
		// set the filename, based on a combination of instrument specification
		// id and date.

		// iterating over the days.
		Calendar currentDay = GregorianCalendar.getInstance();
		currentDay.setTime(spec.getStartTimeStamp().getDate());
		Calendar endDay = GregorianCalendar.getInstance();
		endDay.setTime(spec.getEndTimeStamp().getDate());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		// iterate over the days until we are at the end.
		List<String> fileNames = new ArrayList<String>();
		while (currentDay.before(endDay) || currentDay.equals(endDay)) {
			//
			String filename = baseFolder + File.separatorChar
					+ NumberFormat.getIntegerInstance().format(spec.getInstrumentSpecification().getId()) + File.separatorChar
					+ sdf.format(currentDay.getTime()) + File.separatorChar + "quotes.csv";
			log.info("Using filename " + filename);
			fileNames.add(filename);
			currentDay.add(Calendar.DATE, 1);
		}
		// 
		return new FileCombiner(fileNames.toArray(new String[] {}));
	}

}
