package org.activequant.util;

import java.util.Hashtable;

import org.activequant.core.domainmodel.InstrumentSpecification;
import org.activequant.core.domainmodel.Symbol;
import org.activequant.core.types.SecurityType;
import org.activequant.core.types.Currency;
import org.activequant.dao.IFactoryDao;
import org.activequant.dao.ISpecificationDao;
import org.activequant.dao.hibernate.FactoryLocatorDao;

public class SpecResolver {
	IFactoryDao factoryDao = new FactoryLocatorDao("activequantdao/config.xml");
	ISpecificationDao specDao = factoryDao.createSpecificationDao();

	// it would be possible to overwrite this from within spring ...
	private Hashtable<String, InstrumentSpecification> theSpecCache = new Hashtable<String, InstrumentSpecification>();

	public synchronized InstrumentSpecification getSpec(String aSymbol,
			String anExchange, String aCurrency, String aVendor) {
		String myKey = aSymbol + anExchange + aCurrency;
		if (!theSpecCache.containsKey(myKey)) {
			// fetch the key ..
			InstrumentSpecification myExampleSpec = new InstrumentSpecification();
			myExampleSpec.setSymbol(new Symbol(aSymbol));
			myExampleSpec.setCurrency(Currency.valueOf(aCurrency));
			myExampleSpec.setExchange(anExchange);
			myExampleSpec.setVendor(aVendor);
			myExampleSpec.setTickSize(0.25);
			myExampleSpec.setTickValue(12.5);

			InstrumentSpecification[] specs = specDao.findAll();
			InstrumentSpecification spec = null;
			for (InstrumentSpecification s : specs) {
				if (s.getSymbol().toString().equals(aSymbol)
						&& s.getCurrency().toString().equals(aCurrency)
						&& s.getExchange().toString().equals(anExchange)
						&& s.getVendor().toString().equals(aVendor)) {
					spec = s;
					break;
				}
			}

			if (spec == null) {
				myExampleSpec.setLotSize(1);
				myExampleSpec.setSecurityType(SecurityType.FUTURE);
				spec = specDao.update(myExampleSpec);
			}

			theSpecCache.put(myKey, spec);
		}
		return theSpecCache.get(myKey);

	}

}
