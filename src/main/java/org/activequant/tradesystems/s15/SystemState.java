package org.activequant.tradesystems.s15;

import java.io.Serializable;

import org.activequant.util.LimitedQueue;

/**
 * contains all system states. 
 * 
 * @author Ulrich Staudinger
 *
 */

public class SystemState implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int period1, period2;
	private int formerPosition = 0;

	private double bid1, ask1, bid2, ask2;
	private double mp1, mp2;
	private int quoteCount = 0;
	private Double smoothedRatio;
	private LimitedQueue<Double> shortRatioQueue, longRatioQueue;
	private Double entryPrice1, entryPrice2;
	private double pnl = 0.0;
	private double totalPnl = 0.0;
	
		public double getTotalPnl() {
		return totalPnl;
	}
	public void setTotalPnl(double totalPnl) {
		this.totalPnl = totalPnl;
	}
		public void incQuoteCount(){
		quoteCount++;
	}
	public int getPeriod1() {
		return period1;
	}
	public void setPeriod1(int period1) {
		this.period1 = period1;
	}
	public int getPeriod2() {
		return period2;
	}
	public void setPeriod2(int period2) {
		this.period2 = period2;
	}
	public int getFormerPosition() {
		return formerPosition;
	}
	public void setFormerPosition(int formerPosition) {
		this.formerPosition = formerPosition;
	}
	public double getBid1() {
		return bid1;
	}
	public void setBid1(double bid1) {
		this.bid1 = bid1;
	}
	public double getAsk1() {
		return ask1;
	}
	public void setAsk1(double ask1) {
		this.ask1 = ask1;
	}
	public double getBid2() {
		return bid2;
	}
	public void setBid2(double bid2) {
		this.bid2 = bid2;
	}
	public double getAsk2() {
		return ask2;
	}
	public void setAsk2(double ask2) {
		this.ask2 = ask2;
	}
	public double getMp1() {
		return mp1;
	}
	public void setMp1(double mp1) {
		this.mp1 = mp1;
	}
	public double getMp2() {
		return mp2;
	}
	public void setMp2(double mp2) {
		this.mp2 = mp2;
	}
	public int getQuoteCount() {
		return quoteCount;
	}
	public void setQuoteCount(int quoteCount) {
		this.quoteCount = quoteCount;
	}
	public Double getSmoothedRatio() {
		return smoothedRatio;
	}
	public void setSmoothedRatio(Double ratioDiff) {
		this.smoothedRatio = ratioDiff;
	}
	public LimitedQueue<Double> getShortRatioQueue() {
		return shortRatioQueue;
	}
	public void setShortRatioQueue(LimitedQueue<Double> shortRatioQueue) {
		this.shortRatioQueue = shortRatioQueue;
	}
	public LimitedQueue<Double> getLongRatioQueue() {
		return longRatioQueue;
	}
	public void setLongRatioQueue(LimitedQueue<Double> longRatioQueue) {
		this.longRatioQueue = longRatioQueue;
	}
	public Double getEntryPrice1() {
		return entryPrice1;
	}
	public void setEntryPrice1(Double entryPrice1) {
		this.entryPrice1 = entryPrice1;
	}
	public Double getEntryPrice2() {
		return entryPrice2;
	}
	public void setEntryPrice2(Double entryPrice2) {
		this.entryPrice2 = entryPrice2;
	}
	public double getPnl() {
		return pnl;
	}
	public void setPnl(double pnl) {
		this.pnl = pnl;
	}
	
}
