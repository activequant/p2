package org.activequant.util;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;

public class TalibWrapper {

	private Core core = new Core();
	
	public double sma(double[] values, int period)
	{

		MInteger outBegIdx = new MInteger();
		MInteger outNbElement = new MInteger();

		double[] outArray = new double[values.length];
		
		core.sma(0, values.length-1, values, period, outBegIdx, outNbElement, outArray);
		
		return outArray[outNbElement.value - outBegIdx.value];
		
	}
	

	public double ema(double[] values, int period)
	{

		
		
		MInteger outBegIdx = new MInteger();
		MInteger outNbElement = new MInteger();

		double[] outArray = new double[values.length];
		
		core.ema(0, values.length-1, values, period, outBegIdx, outNbElement, outArray);
		
		return outArray[outNbElement.value - outBegIdx.value];
		
	}
	


	public double wma(double[] values, int period)
	{

	
		MInteger outBegIdx = new MInteger();
		MInteger outNbElement = new MInteger();

		double[] outArray = new double[values.length];
		
		core.wma(0, values.length-1, values, period, outBegIdx, outNbElement, outArray);
		
		return outArray[outNbElement.value - outBegIdx.value];
		
	}
	

	public double tema(double[] values, int period)
	{

		
		MInteger outBegIdx = new MInteger();
		MInteger outNbElement = new MInteger();

		double[] outArray = new double[values.length];
		
		core.tema(0, values.length-1, values, period, outBegIdx, outNbElement, outArray);
		
		return outArray[outNbElement.value - outBegIdx.value];
		
	}
	


	public double sar(double[] highs, double[] lows, double accelerationFactor, double maxAcceleration)
	{

		
		MInteger outBegIdx = new MInteger();
		MInteger outNbElement = new MInteger();

		double[] outArray = new double[highs.length];
		
		core.sar(0, highs.length-1, highs, lows, accelerationFactor, maxAcceleration, outBegIdx, outNbElement, outArray);
		
		return outArray[outNbElement.value - outBegIdx.value];
		
	}

	
	public double willR(double[] highs, double[] lows, double[] closes, int optInTimePeriod)
	{
		MInteger outBegIdx = new MInteger();
		MInteger outNbElement = new MInteger();

		double[] outArray = new double[highs.length];
		
		core.willR(0, highs.length-1, highs, lows, closes, optInTimePeriod, outBegIdx, outNbElement, outArray);
		
		return outArray[outNbElement.value - outBegIdx.value];	
	}


	public double kama(double[] values, int optInTimePeriod)
	{
		MInteger outBegIdx = new MInteger();
		MInteger outNbElement = new MInteger();
		double[] outArray = new double[values.length];		
		core.kama(0, values.length-1, values, optInTimePeriod, outBegIdx, outNbElement, outArray);		
		return outArray[outNbElement.value - outBegIdx.value];	
	}


	public double adx(double[] highs, double[] lows, double[] closes, int optInTimePeriod)
	{
		MInteger outBegIdx = new MInteger();
		MInteger outNbElement = new MInteger();
		double[] outArray = new double[highs.length];		
		core.adx(0, highs.length-1, highs, lows, closes, optInTimePeriod, outBegIdx, outNbElement, outArray);
		
		return outArray[outNbElement.value - outBegIdx.value];	
	}
		
	/**
	 * returns a three value array, where value 0 is the macd, value 1 is the macd signal and value 2 is the macd histogram. 
	 * @param values
	 * @param fastPeriod
	 * @param slowPeriod
	 * @param signalPeriod
	 * @return
	 */
	public double[] macd(double[] values, int fastPeriod, int slowPeriod, int signalPeriod)
	{
		double[] ret = new double[3];
		MInteger outBegIdx = new MInteger();
		MInteger outNbElement = new MInteger();
		double[] macd = new double[values.length];
		double[] macdSignal = new double[values.length];
		double[] macdHist = new double[values.length];
		
		core.macd(0, values.length-1, values, fastPeriod, slowPeriod, signalPeriod, outBegIdx, outNbElement, macd, macdSignal, macdHist);
		
		ret[0] = macd[outNbElement.value - outBegIdx.value];
		ret[1] = macdSignal[outNbElement.value - outBegIdx.value];
		ret[2] = macdHist[outNbElement.value - outBegIdx.value];
		
		return ret; 
	}
	
	
	public static void main(String[] args)
	{
		System.out.println(new TalibWrapper().tema(new double[]{1,2,3,5,6,7,8,9,10}, 2));
		
	}
	
}

