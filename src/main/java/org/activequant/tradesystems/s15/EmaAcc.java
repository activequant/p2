package org.activequant.tradesystems.s15;

import java.io.Serializable;


public class EmaAcc  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private double value = 0;
	private int accValues = 0; 
	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public int getPeriod() {
		return period;
	}

	private int period = 50;
	private double exponent = 2 / (double) (period + 1);
	public double eacc2(double val){
		accValues++; 
		if(accValues <= period){
			value += val/(double)period; 
		}
		else{
			value = val * exponent + (value * (1 - exponent));
		}
		return value;
	}
	
	public EmaAcc(int period)
	{
		this.period = period; 
	}
	
	/**
	 * @param args
	 * @throws IOException
	 */
	/*public static void main(String[] args) throws IOException {
		LimitedQueue<Double> q1 = new LimitedQueue<Double>(50);
		BufferedReader br = new BufferedReader(new FileReader(
				"/home/knoppix/test.csv"));
		BufferedWriter bw = new BufferedWriter(new FileWriter("/home/knoppix/test2.csv"));
		String l = br.readLine();
		l = br.readLine();
		double f1 = 0,  f2 = 0;
		while (l != null) {
			String[] p = l.split(",");
			q1.add(Double.parseDouble(p[1]));
			eacc2(Double.parseDouble(p[1]));
			double currentR2Ema  = 0; 
			if(q1.isFull()){
				
				Double[] r2 = q1.toArray(new Double[] {});
				currentR2Ema= value; //FinancialLibrary2.rEMA(r2.length,
						//ArrayUtils.convert(r2));
				System.out.print(currentR2Ema+ " <-> " + p[2]);
				//bw.write(""+currentR2Ema+",\n");
				//bw.flush();
				if(f1!=0.0){
					System.out.print(" ........... "+(currentR2Ema>f1) + "   <-> " + (Double.parseDouble(p[2]) > f2) +" ");
				}
				f1 = currentR2Ema;
				f2 = Double.parseDouble(p[2]);
			}
			
			System.out.println();
			l = br.readLine();
			//System.out.println(l);
		}
	}
*/
}
