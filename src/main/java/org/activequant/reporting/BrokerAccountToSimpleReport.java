package org.activequant.reporting;

import org.activequant.container.report.SimpleReport;
import org.activequant.core.domainmodel.account.BrokerAccount;
import org.activequant.core.domainmodel.account.OrderHistory;
import org.activequant.core.domainmodel.data.ValueSeries;
import org.activequant.core.types.OrderSide;

public class BrokerAccountToSimpleReport {

	public void transform(BrokerAccount brokerAccount, SimpleReport simpleReport)
	{
		//
		int length = brokerAccount.getOrderBook().getHistories().length;
		simpleReport.getReportValues().put("Orders", length);
		
		//
		int longOrders = 0; 
		int shortOrders = 0; 
		int executedOrders = 0; 
		int cancelledOrders = 0;
		int openOrders = 0; 
		for(OrderHistory h : brokerAccount.getOrderBook().getHistories())
		{
			if(h.getOrder().getOrderSide().equals(OrderSide.BUY))
					longOrders++;
			else shortOrders++;
			
			if(h.getHistoryCompletion()==null)
				openOrders++;
			else{
				if(h.getHistoryCompletion().getTerminalError()!=null)
					cancelledOrders++;
				else 
					executedOrders++;
			}
		}
		simpleReport.getReportValues().put("LongOrders", longOrders);
		simpleReport.getReportValues().put("ShortOrders", shortOrders);
		simpleReport.getReportValues().put("ExecutedOrders", executedOrders);
		simpleReport.getReportValues().put("CancelledOrders", cancelledOrders);
		simpleReport.getReportValues().put("OpenOrders", openOrders);
		ValueSeries v = (ValueSeries)simpleReport.getReportValues().get("PNLObject");
		// clean up some memory ... 
		simpleReport.getReportValues().remove("PNLObject");
		populatePnlRelatedStatistics(simpleReport, v);
	}
	
	private void populatePnlRelatedStatistics(SimpleReport r, ValueSeries v)
	{
		if(!v.isEmpty())
			r.getReportValues().put("PNL", v.lastElement().getValue());
		else
			r.getReportValues().put("PNL", 0.0);	
	}
	
}
