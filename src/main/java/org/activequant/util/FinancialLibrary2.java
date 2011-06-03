package org.activequant.util;

/****

 activequant - activestocks.eu

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along
 with this program; if not, write to the Free Software Foundation, Inc.,
 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.


 contact  : contact@activestocks.eu
 homepage : http://www.activestocks.eu

 ****/

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.activequant.core.types.TimeStamp;
import org.activequant.core.types.Tuple;
import org.activequant.math.algorithms.EMAAccumulator;
import org.activequant.util.exceptions.NotEnoughDataException;
import org.apache.log4j.Logger;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;

/**
 * A class storing various indicator computations.<br>
 * It makes sense to statically import this into your class<br>
 * <br>
 * <b>History:</b><br>
 * - [03.05.2007] Created (GhostRider)<br>
 * - [06.05.2007] Added min, max, priceRange, MEMA, vola (Erik Nijkamp)<br>
 * - [10.05.2007] added wma, sma, rsi, roc, normalizeArray, scale from ccapi2.
 * (GhostRider)<br>
 * - [14.11.2007] Added priceSlope (GhostRider)<br>
 * - [23.11.2007] Adding PivotPoints based on Dan O'Rourke's contribution
 * (GhostRider)<br>
 * - [11.01.2007] Adding yield based on Alberto Sfolcini's contribution (GhostRider
 * Staudinger)<br>
 * - [15.01.2007] Adding nondiscrete average computation (GhostRider)<br>
 * - [17.04.2008] Adding metastock like functions (GhostRider)<br>
 * <br>
 * 
 * @author GhostRider
 * @author Erik Nijkamp
 */
public class FinancialLibrary2 {

	protected final static Logger log = Logger.getLogger(FinancialLibrary2.class);

	public static double max(double[] values) {
		double max = Double.MIN_VALUE;
		for (double value : values) {
			if (value > max)
				max = value;
		}
		return max;
	}

	public static double min(double[] values) {
		double min = Double.MAX_VALUE;
		for (double value : values) {
			if (value < min)
				min = value;
		}
		return min;
	}

	public static double max(double[] values, int start, int end) {
		double[] sublist = new double[end - start + 1];
		System.arraycopy(values, start, sublist, 0, sublist.length);
		return max(sublist);
	}

	public static double min(double[] values, int start, int end) {
		double[] sublist = new double[end - start + 1];
		System.arraycopy(values, start, sublist, 0, sublist.length);
		return min(sublist);
	}

	public static double[] bollinger(int n, int deviations, double[] vals, int skipdays) {
		double[] value = new double[3];

		double centerband = SMA(n, vals, skipdays);

		double t2 = deviation(n, vals, skipdays);

		double upper = centerband + (deviations * t2);
		double lower = centerband - (deviations * t2);

		value[2] = upper;
		value[1] = centerband;
		value[0] = lower;

		return value;
	}

	public static double deviation(int n, double[] vals, int skipdays) {
		double centerband = SMA(n, vals, skipdays);

		double t1 = 0.0;

		for (int i = 0; i < n; i++) {
			t1 += ((vals[i + skipdays] - centerband) * (vals[i + skipdays] - centerband));
		}

		double t2 = Math.sqrt(t1 / (double) n);

		return t2;
	}

	/**
	 * returns the parabolic SAR - double check with a reference implementation
	 * !
	 * 
	 * @param initialValue
	 *            TODO
	 * @param candles
	 * @param skipdays
	 * @param n
	 * 
	 * @return the parabolic SAR
	 */
	public static double SAR(double af, double max, double[] lows, double[] highs, int skipdays) {

		Core core = new Core();

		List<Double> l1 = new ArrayList<Double>();
		List<Double> l2 = new ArrayList<Double>();
		for (Double d : lows)
			l1.add(d);
		for (Double d : highs)
			l2.add(d);

		Collections.reverse(l1);
		Collections.reverse(l2);

		// need to reverse from activequant norm for ta lib.
		double[] lowsReversed = new double[lows.length - skipdays];
		double[] highsReversed = new double[highs.length - skipdays];

		for (int i = 0; i < lowsReversed.length; i++) {
			lowsReversed[i] = l1.get(i);
			highsReversed[i] = l2.get(i);
		}

		MInteger outBegIdx = new MInteger();
		MInteger outNbElement = new MInteger();

		double[] outArray = new double[highsReversed.length];

		core.sar(0, highsReversed.length - 1, highsReversed, lowsReversed, af, max, outBegIdx, outNbElement, outArray);

		double value = outArray[outArray.length - 1 - outBegIdx.value];
		return value;

	}

	/**
	 * returns the minimum value of two doubles.
	 * 
	 * @param v1
	 * @param v2
	 * @return minimum of a value of two doubles.
	 */
	public static double minOf(double v1, double v2) {
		if (v1 <= v2)
			return v1;
		if (v2 <= v1)
			return v2;
		return v1;
	}

	/**
	 * returns the minimum value of three doubles
	 * 
	 * @param v1
	 * @param v2
	 * @param v3
	 * @return the minimum value of three doubles
	 */
	public static double minOf(double v1, double v2, double v3) {
		if (v1 <= v2 && v1 <= v3)
			return v1;
		if (v2 <= v1 && v2 <= v3)
			return v2;
		if (v3 <= v1 && v3 <= v2)
			return v3;
		return v1;
	}

	/**
	 * returns the max value out of three.
	 */
	public static double maxOf(double v1, double v2, double v3) {
		if (v1 >= v2 && v1 >= v3)
			return v1;
		if (v2 >= v1 && v2 >= v3)
			return v2;
		if (v3 >= v1 && v3 >= v2)
			return v3;
		return v1;
	}

	public static double maxOf(double v1, double v2) {
		if (v1 >= v2)
			return v1;
		else
			return v2;
	}

	public static double SMA(int period, double[] vals, int skipdays) {

		double value = 0.0;
		// debugPrint("SMA("+period+") for "+candles.size()+ " skipd:
		// "+skipdays);

		for (int i = skipdays; i < (period + skipdays); i++) {
			value += vals[i];
		}

		value /= (double) period;

		return value;
	}

	/**
	 * returns the linearly weighted moving average.
	 * 
	 * @param period
	 * @param candles
	 * @param skipdays
	 * @return the wma
	 */
	public static double WMA(int period, double[] vals, int skipdays) {

		double numerator = 0.0;

		int weight = period;
		for (int i = skipdays; i < (period + skipdays); i++) {
			numerator += vals[i] * weight;
			weight--;
		}

		int denominator = period * (period + 1) / 2;
		double value = numerator / denominator;

		return value;
	}

	/**
	 * returns the slope between two timepoints
	 * 
	 * @param n
	 * @param candles
	 * @param skipdays
	 * @return the slope
	 */
	public static double slope(int n, double[] values, int skipdays) {
		double value = 0.0;
		value = (values[skipdays] - values[n + skipdays]) / (double) n;
		return value;
	}

	/**
	 * calculates the slope, relative to the price and scales it by 100.
	 * 
	 * @param n
	 * @param values
	 * @param skipdays
	 * @return
	 */
	public static double priceSlope(int n, double[] values, int skipdays) {
		double value = 0.0;
		value = (values[skipdays] - values[n + skipdays]) / (double) values[skipdays] * 100;
		return value;
	}

	/**
	 * returns a SMA smoothed slope
	 * 
	 * @param n
	 * @param smoothingfactor
	 * @param candles
	 * @param skipdays
	 * @return smoother slope
	 */
	public static double smoothedSlope(int n, int smoothingunits, double[] vals, int skipdays) {
		double value = 0.0;
		double[] values = new double[smoothingunits];
		for (int i = 0; i < (smoothingunits); i++) {
			values[i] = slope(n, vals, skipdays + i);
		}
		value = SMA(smoothingunits, values, 0);
		return value;
	}

	/**
	 * returns the exponential moving average <br/> see
	 * http://www.quotelinks.com/technical/ema.html
	 * 
	 * @param n
	 * @param candles
	 * @param skipdays
	 * @return the exponential moving average
	 */
	public static double EMA(int n, double[] vals, int skipdays) {
		double value = 0;

		double exponent = 2 / (double) (n + 1);

		value = vals[vals.length - 1];

		for (int i = vals.length - 1; i > skipdays - 1; i--) {

			value = (vals[i] * exponent) + (value * (1 - exponent));

		}

		return value;
	}

	public static double volatilityIndex(int p1, int p2, double[][] ohlc, int skipdays) {
		return volatilityIndex(p1, p2, ohlc[0], ohlc[1], ohlc[2], ohlc[3], skipdays);
	}

	/**
	 * calculates the volatility Index, returns the trend following system
	 * working with SAR points. indicator requires at least 100 candles !
	 * 
	 * @param p1
	 *            the factor
	 * @param p2
	 *            the periods to work on.
	 * @param candles
	 *            this are the input candles.
	 * @param skipdays
	 *            this parameter specifies how many days to skip.
	 * @return the volatility Index
	 */
	public static double volatilityIndex(int p1, int p2, double[] opens, double[] highs, double[] lows, double[] closes, int skipdays) {

		boolean first_run_vlx = true;
		boolean position_long = true;
		double sip = 0, sar = 0, next_sar = 0, smoothed_range = 0;

		int max = closes.length - skipdays - 2;
		if (max > 200)
			max = 200;

		for (int i = max-p2; i > skipdays - 1; i--) {

			double value = closes[i];
			smoothed_range = MEMA(p2, priceRange(opens, highs, lows, closes, i), 0);
			double atr = smoothed_range * p1;
			// System.out.println("atr: " + atr);
			if (first_run_vlx && smoothed_range != 0) {
				first_run_vlx = false;
				sip = max(highs, i, i + p2);
				next_sar = sip - atr;
				sar = next_sar;
			} else {
				sar = next_sar;
				if (position_long) {
					if (value < sar) {
						position_long = false;
						sip = value;
						next_sar = sip + (smoothed_range * p1);
					} else {
						position_long = true;
						sip = (value > sip) ? value : sip;
						next_sar = sip - (smoothed_range * p1);
					}
				} else {
					if (value > sar) {
						position_long = true;
						sip = value;
						next_sar = sip - (smoothed_range * p1);
					} else {
						position_long = false;
						sip = (value < sip) ? value : sip;
						next_sar = sip + (smoothed_range * p1);
					}
				}
			}
		}
		return sar;
	}

	/**
	 * calculates the MEMA
	 * 
	 * @param period
	 * @param candles
	 * @param skipdays
	 * @return the MEMA
	 */
	public static double MEMA(int period, double[] values, int skipdays) {
		double mema = 0.0;
		double smoothing = 1;

		if (period != 0) {
			smoothing = 1 / (double) period;
		}

		int max = values.length;
		if (max > 600 + skipdays + 2 + period) {
			max = 500 + skipdays + 2 + period;
		} else {
			max = values.length-1;
		} 
		for (int i = max; i >= skipdays; i--) {
			double value = values[i];
			if (i == max) {
				// ok, beginning of calculation
				mema = value;
			} else {
				mema = (smoothing * value) + ((1 - smoothing) * mema);
			}
		}
		return mema;
	}

	/**
	 * returns the price range R as an array of doubles.
	 * 
	 * @return the price range
	 */
	public static double[] priceRange(double[] opens, double[] highs, double[] lows, double[] closes, int skipdays) {
		List<Double> results = new ArrayList<Double>();
		boolean first_run_range = true;
		int max = opens.length - 1;
		if (max > 200)
			max = 200;
		for (int i = max; i > skipdays - 1; i--) {
			double result = 0.0;
			if (first_run_range) {
				first_run_range = false;
				result = highs[i] - lows[i];
			} else {
				double v1 = highs[i] - lows[i];
				double v2 = highs[i] - closes[i + 1];
				double v3 = closes[i + 1] - lows[i];

				if (v1 >= v2 && v1 >= v3) {
					result = v1;
				} else if (v2 >= v1 && v2 >= v3) {
					result = v2;
				} else if (v3 >= v1 && v3 >= v2) {
					result = v3;
				}
			}
			results.add(0, result);
		}
		return unboxDoubles(results);
	}

	private static double[] unboxDoubles(List<Double> doubles) {
		double[] array = new double[doubles.size()];
		for (int i = 0; i < array.length; i++) {
			array[i] = doubles.get(i);
		}
		return array;
	}

	/**
	 * returns a normalized copy of the input array
	 */
	public static double[] normalizeArray(double[] in) {
		double min = min(in);
		double max = max(in);
		double[] ret = new double[in.length];

		for (int i = 0; i < in.length; i++) {
			ret[i] = (in[i] - min) / (max - min);
		}
		return ret;
	}

	/**
	 * calculates the plain mean of an array of doubles
	 * 
	 * @param vals
	 * @return plain mean of an array of doubles
	 */
	public static double mean(double[] vals) {
		double v = 0;
		for (int i = 0; i < vals.length; i++) {
			v += vals[i];
		}
		v /= (double) vals.length;
		return v;
	}

	/**
	 * returns the rate of change
	 * 
	 * @param n
	 * @param candles
	 * @param skipdays
	 * @return the rate of change
	 */
	public static double ROC(int n, double[] vals, int skipdays) {
		double value = 0.0;
		double v0 = vals[skipdays];
		double v1 = vals[skipdays + n];

		value = (v0 - v1) / v0 * 100;

		return value;
	}

	/**
	 * returns the RSI
	 * 
	 * @param n
	 * @param values
	 * @param skipdays
	 * @return the RSI
	 */
	public static double RSI(int n, double[] vals, int skipdays) {
		double U = 0.0;
		double D = 0.0;

		for (int i = 0; i < n; i++) {
			double v0 = vals[skipdays + i];
			double v1 = vals[skipdays + i + 1];

			double change = v0 - v1;

			if (change > 0) {
				U += change;
			} else {
				D += Math.abs(change);
			}
		}

		// catch division by zero
		if (D == 0 || (1 + (U / D)) == 0) {
			log.warn("Division by zero");
			return 0.0;
		}

		return 100 - (100 / (1 + (U / D)));
	}

	/**
	 * this function does scale the input parameters into the values -1...1. Can
	 * be useful for various aspects.
	 * 
	 * @param in
	 *            the input values
	 * @return the input values in the range -1 .. 1
	 */
	public static double[] scale(double[] in) {
		double[] ret = new double[in.length];

		for (int i = 0; i < in.length; i++) {
			ret[i] = -1 + 2 * in[i];
		}
		return ret;
	}

	/**
	 * returns the logarithmic change value for double[0] and double[1].
	 * double[0] must be the more recent value.
	 * 
	 * @param in
	 * @return
	 * @throws NotEnoughDataException
	 */
	public static double logChange(double[] in) throws NotEnoughDataException {
		if (in.length < 2)
			throw new NotEnoughDataException("Too few inputs.");
		double logReturn = Math.log(in[0] / in[1]);
		return logReturn;
	}

	/**
	 * checks if something is a hammer or not.
	 * 
	 * @param series
	 * @param filterPeriod
	 * @param multiplier
	 * @param position
	 * @return
	 */
	public static boolean isHammer(org.activequant.core.domainmodel.data.CandleSeries series, int filterPeriod, double multiplier,
			int position) {
		//

		// 
		double xaverage = EMA(filterPeriod, series.getCloses(), position);

		if (series.get(position).getClosePrice() < xaverage) {

			double open = series.get(position).getOpenPrice();
			double high = series.get(position).getHighPrice();
			double low = series.get(position).getLowPrice();
			double close = series.get(position).getClosePrice();

			double bodyMin = minOf(close, open);
			double bodyMax = maxOf(close, open);
			double candleBody = bodyMax - bodyMin;
			double rangeMedian = (high + low) * 0.5;
			double upShadow = high - bodyMax;
			double downShadow = bodyMin - low;

			boolean isHammer = (open != close) && (bodyMin > rangeMedian) && (downShadow > (candleBody * multiplier))
					&& (upShadow < candleBody);

			return isHammer;

		} else {
			return false;
		}

	}

	/**
	 * checks if something is a hammer or not by another algorithm
	 * 
	 * @param series
	 * @param filterPeriod
	 * @param multiplier
	 * @param position
	 * @return
	 */
	public static boolean isHammer2(org.activequant.core.domainmodel.data.CandleSeries series, int filterPeriod, double multiplier,
			int position) {
		//
		int ups = 0;
		int downs = 0;
		for (int i = position + 1; i < position + filterPeriod + 1; i++) {
			if (series.get(i).isRising())
				ups++;
			else
				downs++;
		}
		if (ups > downs)
			return false;

		if (series.get(position + 1).isRising())
			return false;
		if (series.get(position).getClosePrice() > series.get(position + 1).getClosePrice())
			return false;

		double open = series.get(position).getOpenPrice();
		double high = series.get(position).getHighPrice();
		double low = series.get(position).getLowPrice();
		double close = series.get(position).getClosePrice();

		double bodyMin = minOf(close, open);
		double bodyMax = maxOf(close, open);
		double candleBody = bodyMax - bodyMin;
		double rangeMedian = (high + low) * 0.5;
		double upShadow = high - bodyMax;
		double downShadow = bodyMin - low;

		boolean isHammer = (open != close) && (bodyMin > rangeMedian) && (downShadow > (candleBody * multiplier))
				&& (upShadow < downShadow);

		return isHammer;

	}

	/**
	 * Calculates Pivot Points, contributed by Dan O'Rourke.
	 * 
	 * @param open
	 * @param high
	 * @param low
	 * @param close
	 * @param positionInTime
	 * @return
	 */
	public static double[] getPivotPoints(double[] open, double[] high, double[] low, double[] close, int pos) {
		double[] ret = new double[7];

		ret[3] = (high[pos] + low[pos] + close[pos]) / 3;
		// r1
		ret[2] = (ret[2] * 2) - low[pos];
		// r2
		ret[1] = (ret[2] + high[pos] - low[pos]);
		// r3
		ret[0] = (ret[1] + high[pos] - low[pos]);

		// s1
		ret[4] = (ret[3] * 2) - high[pos];
		// s2
		ret[5] = (ret[4] - high[pos] + low[pos]);
		// s3
		ret[6] = (ret[5] - high[pos] + low[pos]);

		return ret;
	}

	/**
	 * returns the yield with a given lag.
	 * 
	 * @param in
	 * @return double
	 * @throws NotEnoughDataException
	 */
	public static double[] yield(double[] in, int lag) throws NotEnoughDataException {
		double[] ret = new double[in.length];
		if (in.length < lag)
			throw new NotEnoughDataException("Too few inputs.");
		for (int i = 0; i < in.length - lag; i++) {
			ret[i] = Math.log(in[i] / in[i + lag]) * 100;
		}
		return ret;
	}

	/**
	 * returns the normalized yield between in1[0] and in2[lag].
	 * 
	 * @param in1
	 * @param in2
	 * @return double yield
	 * @throws NotEnoughDataException
	 */
	public static double[] yield(double[] in1, double[] in2, int lag) throws NotEnoughDataException {
		double[] ret = new double[in1.length];
		if ((in1.length < lag) && (in1.length != in2.length))
			throw new NotEnoughDataException("Too few inputs.");
		for (int i = 0; i < in1.length - lag; i++) {
			ret[i] = Math.log(in1[i] / in2[i + lag]) * 100;
		}
		return ret;
	}

	/**
	 * computes the average price from a non discrete signal using a simple
	 * integral. List must be in order t(0) is oldest and t(X) is newest.
	 * 
	 * @param values
	 * @return
	 */
	public static double nonDiscreteAverage(List<Tuple<TimeStamp, Double>> values) {

		Tuple<TimeStamp, Double> current = values.get(0);
		double wholeArea = 0.0;
		for (Tuple<TimeStamp, Double> val : values) {
			double baseArea = (val.getObject2()) * (val.getObject1().getNanoseconds() - current.getObject1().getNanoseconds());
			double upperArea = ((current.getObject2() - val.getObject2()) * (val.getObject1().getNanoseconds() - current.getObject1()
					.getNanoseconds())) / 2.0;
			wholeArea = baseArea + upperArea;
			current = val;
		}

		return wholeArea;
	}

	/**
	 * calculates the sharpe Ratio, you need to pipe in returns in PERCENT!!!!
	 * this sharpe ratio calculation calculates based on a periodically series
	 * of returns and the std deviation of these returns Example of use: create
	 * an array of weekly returns, i.e. {0.1, 0.2, 0.01, 0.04} know the weekly
	 * interest rate, for example 0.04/52
	 * 
	 * @param returns
	 *            - this double[] must contain the return in percents for a
	 *            given period (i.e. 0.1)
	 * @param interest
	 *            - the interest rate in percent for this period (i.e. 0.035)
	 * @return the sharpe ratio
	 */
	public static double sharpeRatio(double[] returns, double interest) {
		if (returns.length > 1) {
			double ret = 0.0, avg = 0.0, stddev = 0.0;
			avg = mean(returns);
			stddev = deviation(returns.length, returns, 0);
			ret = (avg - interest) / stddev;
			return ret;
		} else if (returns.length == 1) {
			return returns[0] - interest;
		} else
			return 0;
	}

	/**
	 * Calculates Average True Range. For explanation of the Average True Range,
	 * see http://stockcharts.com/school/doku.php?id=chart_school:
	 * technical_indicators:average_true_range_atr
	 * 
	 * Remember, that values of the ATR for indices less than averaging period
	 * is not reliable and not well-defined. Here we define it as EMA mean
	 * values (which is the natural definition).
	 * 
	 * @param highs
	 *            array of high prices.
	 * @param lows
	 *            array of low prices.
	 * @param closes
	 *            array of close prices.
	 * @param period
	 *            averaging period (for EMA smoothening).
	 * 
	 * @return array of computed values for Average True Range.
	 */
	public static double[] averageTrueRange(double[] highs, double[] lows, double[] closes, int period) {

		int length = highs.length;

		if (period <= 0) {
			throw new IllegalArgumentException("Periods specified must be over 0");
		}

		if (lows.length != length || closes.length != length) {
			throw new IllegalArgumentException("The number of highs, lows, opens and closes must equal");
		}

		EMAAccumulator ema = new EMAAccumulator();
		ema.setPeriod(period);

		double[] atr = new double[length];

		double prevClose = closes[0];
		for (int i = 0; i < length; i++) {
			double delta;

			if (prevClose < lows[i])
				delta = highs[i] - prevClose;
			else if (prevClose > highs[i])
				delta = prevClose - lows[i];
			else
				delta = highs[i] - lows[i];

			/*
			 * if(i < period) { ema.setNumSamples(i);
			 * ema.setMeanValue(ema.getMeanValue() + delta / period); } else {
			 * ema.accumulate(delta); }
			 * 
			 * if(i < period - 1) { atr[i] = delta; } else { atr[i] =
			 * ema.getMeanValue(); }
			 */

			ema.accumulate(delta);
			atr[i] = ema.getMeanValue();

			prevClose = closes[i];
		}

		return atr;
	}

	/**
	 * checks if the values 0 and 1 of two double series cross over each other.
	 * Example:<br>
	 * series1: 10,9,8,7 <br>
	 * series2: 9,10,11<br>
	 * This would yield yes. <br>
	 * 
	 * @param series1
	 * @param series2
	 * @return
	 */
	public static boolean cross(double[] series1, double[] series2) {
		if (series1[0] < series2[0] && series1[1] > series2[1])
			return true;
		if (series1[0] > series2[0] && series1[1] < series2[1])
			return true;
		return false;
	}

	/**
	 * returns the units / bars since the last change in the array.<br>
	 * Example:<br>
	 * series data: 10,10,10,10,3,4,5,4,4,4,4<br>
	 * would return 4<br>
	 * This function tries to work like the metastock barsSince(..) function.<br>
	 * 
	 * @param array
	 * @return
	 */
	public static int unitsSinceChange(double[] array) {
		int i = 0;
		for (i = 0; i < array.length - 1; i++) {
			if (array[i] != array[i + 1])
				return i;
		}
		return i;
	}

	/**
	 * 
	 * This methods returns the the value of the array data at the n'th
	 * occurance of a true in the boolean array.<br>
	 * Example:<br>
	 * occurance : 2<br>
	 * boolean array: 0,1,0,0,0,1<br>
	 * data: 2,1,3,4,1,2<br>
	 * would return 2.
	 * 
	 * @param occurance
	 * @param booleanArray
	 * @param data
	 * @return the last (oldest) element in data if the criterias are never
	 *         matched, otherwise see above.
	 */
	public static double valueWhen(int occurance, boolean[] booleanArray, double[] data) {
		int n = 0;
		for (int i = 0; i < booleanArray.length; i++) {
			if (booleanArray[i]) {
				n++;
				if (n == occurance)
					return data[i];
			}
		}
		return data[data.length - 1];
	}

	/**
	 * maps to the metastock hhvbars method.
	 * 
	 * @param n
	 * @param data
	 * @return
	 */
	public static int hhvbars(int n, double[] data) {
		int periodsSinceHigh = 0;
		double max = -10000000;
		if (n > data.length - 1)
			n = data.length - 1;
		for (int i = 0; i < n; i++) {
			if (data[i] > max) {
				periodsSinceHigh = i;
				max = data[i];
			}
		}
		return periodsSinceHigh;
	}

	/**
	 * maps to the metastock llvbars method.
	 * 
	 * @param n
	 * @param data
	 * @return
	 */
	public static int llvbars(int n, double[] data) {
		int periodsSinceHigh = 0;
		double min = 1000000000;
		if (n > data.length - 1)
			n = data.length - 1;
		for (int i = 0; i < n; i++) {
			if (data[i] < min) {
				periodsSinceHigh = i;
				min = data[i];
			}
		}
		return periodsSinceHigh;
	}

	/**
	 * method to compute the compound interest.
	 * 
	 * @param cash
	 *            i.e. 5000
	 * @param unitsToComputeFor
	 *            i.e. 12 days
	 * @param unitsPerAnnum
	 *            i.e. 360 days
	 * @param interestRatePerAnnum
	 *            i.e. 0.04 for 4%.
	 * @return the interest result, according to ((
	 *         Math.pow((1.0+(interestRatePerAnnum/365.0)),daysToComputeFor) -
	 *         1) * cash; )
	 */
	double compoundInterest(double cash, double unitsToComputeFor, double unitsPerAnnum, double interestRatePerAnnum) {
		double interest = (Math.pow((1.0 + (interestRatePerAnnum / unitsPerAnnum)), unitsToComputeFor) - 1) * cash;
		return interest;
	}

}
