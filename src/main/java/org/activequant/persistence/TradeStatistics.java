package org.activequant.persistence;

public class TradeStatistics {
	private long tradeId;
	private double maxPnl;
	private double minPnl;
	private String description, sessionId; 
	private long timeStampMs, lastQuoteTimeStampMs;
	public long getLastQuoteTimeStampMs() {
		return lastQuoteTimeStampMs;
	}
	public void setLastQuoteTimeStampMs(long lastQuoteTimeStampMs) {
		this.lastQuoteTimeStampMs = lastQuoteTimeStampMs;
	}
	private long id;
	private double lastPnl;
	public TradeStatistics(){}
	public TradeStatistics(long ts, String desc){
		timeStampMs = ts;
		description = desc;
	}
	public double getLastPnl() {
		return lastPnl;
	}
	public void setLastPnl(double lastPnl) {
		this.lastPnl = lastPnl;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getTradeId() {
		return tradeId;
	}
	public void setTradeId(long tradeId) {
		this.tradeId = tradeId;
	}
	public double getMaxPnl() {
		return maxPnl;
	}
	public void setMaxPnl(double maxPnl) {
		this.maxPnl = maxPnl;
	}
	public double getMinPnl() {
		return minPnl;
	}
	public void setMinPnl(double minPnl) {
		this.minPnl = minPnl;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String d) {
		this.sessionId = d;
	}
	public long getTimeStampMs() {
		return timeStampMs;
	}
	public void setTimeStampMs(long timeStampNs) {
		this.timeStampMs = timeStampNs;
	}
}
