package org.activequant.reporting;

import java.util.ArrayList;
import java.util.List;

import org.activequant.core.domainmodel.account.Order;
import org.activequant.core.domainmodel.data.Quote;
import org.activequant.core.domainmodel.events.OrderEvent;
import org.activequant.util.log.LoggerBase;

/**
 * 
 * @author GhostRider
 * 
 */
public class DispatchingLogger extends LoggerBase {

	private List<LoggerBase> loggers = new ArrayList<LoggerBase>();
	
	public DispatchingLogger(){}
	
	@Override
	public void log(Quote arg0) {
		for(LoggerBase l : loggers)
			l.log(arg0);		
	}
	
	@Override
	public void log(Order arg0, OrderEvent arg1) {
		for(LoggerBase l : loggers)
			l.log(arg0, arg1);
	}

}
