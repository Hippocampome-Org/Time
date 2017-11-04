package ec.app.izhikevich.spike;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import ec.app.izhikevich.model.Izhikevich9pModel;
import ec.app.izhikevich.util.GeneralUtils;
import ec.app.izhikevich.util.StatUtil;
import ec.util.MersenneTwisterFast;

public class SpikePatternAdapting extends SpikePattern{

	private int sfaISIs;	
	
	public SpikePatternAdapting() {
		super();
		// TODO Auto-generated constructor stub
	}
	public SpikePatternAdapting(ModelSpikePatternData spikePatternData, double current, double timeMin, double durationOfCurrent, double vR) {
		super(spikePatternData, current, timeMin, durationOfCurrent, vR);
		if(this.ISIs!=null) {
			this.ISI0 = getFirstISI();
			this.frequency0 = getFirstFrequency();
		}
	}
	
	public SpikePatternAdapting(double[] spikeTimes, double currentInjected, double timeMin, double durationOfCurrentInjected){
		super(spikeTimes, currentInjected, timeMin, durationOfCurrentInjected);
	}
	private double ISI0;
	private double frequency0;
	
	private double getFirstISI(){
		return this.getISIs()[0];
	}
	
	private double getFirstFrequency() {
		double firstISI = getFirstISI() * 0.001;
		return 1.0d/firstISI;
	}
	
	public double getISI(int idx){
		return this.getISIs()[idx];
	}

	public double[] getISIsStartingFromIdx(int startIdx){		
		double[] isis = null;
		if(startIdx < ISIs.length){
			if(this.ISIs!=null){
				isis = new double[ISIs.length-startIdx];
				for(int i=0; i<ISIs.length; i++) {				
					if(i>=startIdx){
						isis[i-startIdx] = ISIs[i];	
					}												
				}
			}					
		}
		return isis;
	}
	public double[] getNisisStartingFromIdx(int startIdx, int n){		
		double[] isis = null;
		if(startIdx < ISIs.length){
			int N = ((startIdx+n) <= ISIs.length)? n: (ISIs.length-startIdx);
			if(this.ISIs!=null){
				isis = new double[N];
				for(int i=startIdx; i<startIdx+N; i++) {						
					isis[i-startIdx] = ISIs[i];	
				}
			}					
		}
		return isis;
	}
	public double[] getFirstNISIs(int n) {			
		double[] firstNIsis = null;
		if(ISIs!=null) {
			int max_length = (ISIs.length < n)? ISIs.length : n;
			firstNIsis = new double[max_length];
			for(int i=0; i<firstNIsis.length; i++) {
				firstNIsis[i] = ISIs[i];
			}
		}
		return firstNIsis;
	}
	/*
	 * n not needed for now!
	 */
	public double[] getFirstNISIs2(int previous_n, int n) {			
		double[] firstNIsis = null;
		if(ISIs!=null && ISIs.length>previous_n) {				
			firstNIsis = new double[ISIs.length-previous_n];
			for(int i=previous_n; i<ISIs.length; i++) {
				firstNIsis[i-previous_n] = ISIs[i];
			}
		}		
		return firstNIsis;
	}
	
	public double getRandomIsiBetween(double timeStart, double duration) {
		
		double[][] ISILat_ISI = getFirstNISIsAndTheirLatenciesToSecondSpike(ISIs.length);
		double[] ISI_Lat = ISILat_ISI[0];
		double[] ISI = ISILat_ISI[1];
		
		if(ISI == null) return 0;
		
		if(timeStart < (this.timeMin) ) {
			System.out.println("Time start invalid!");
			System.exit(-1);
		}
		
		double lat_min = timeStart - (this.timeMin);// + this.getPreSpikeDelay() + ISI[0]);
		double lat_max = lat_min + duration - ISI[0];
		
	//	System.out.println(lat_min+"; "+lat_max);
		int lat_min_idx = -1;
		int lat_max_idx = -1;
		
		int flag = -1;
		
		for(int i=0; i<ISI_Lat.length; i++)	{
			if(lat_min <= ISI_Lat[i] && flag == -1) {
				flag = 0 ;
				lat_min_idx = i;
			}
			if(lat_max <= ISI_Lat[i] && flag == 0) {
				flag = 1;
				lat_max_idx = i;
				break;
			}
		}
		
		if(lat_min_idx == -1 || lat_max_idx == -1) {
			return 0;
		}
		if(lat_max_idx == lat_min_idx) {
			return ISI[lat_min_idx];
			//System.out.println(lat_min +", "+ lat_max);
			//System.out.println(lat_min_idx +", "+ lat_max_idx);
			//System.out.println(ISI.length);
		}
	
		MersenneTwisterFast mtf = new MersenneTwisterFast();
		int rdIdx = lat_min_idx + mtf.nextInt(lat_max_idx - lat_min_idx);
		
		return ISI[rdIdx];
	}
/*
 * ISI latencies calculated for 2nd spike of an ISI (from 2 spikes that form ISI)
 */
// Assumption: Spike train is continuous : no silence in the middle
	//get them in a 2 row format
	public double[][] getFirstNISIsAndTheirLatenciesToSecondSpike(int n) {
		double[] ISILatency = null;
		double[] firstNISIs = getFirstNISIs(n);
		if(firstNISIs!=null) {				
			ISILatency = new double[firstNISIs.length];
			double LatencysoFar = this.getFSL();
			for(int i=0; i<ISILatency.length; i++) {
				LatencysoFar += firstNISIs[i];
				ISILatency[i] = LatencysoFar;								
			}
		}
		return new double[][]{ISILatency, firstNISIs};
	}
	public double[] getISILatenciesToSecondSpike(){
		double[] latencies = null;
		if(this.ISIs!=null){
			latencies = new double[ISIs.length];
			double latSoFar = this.getFSL();
			for(int i=0; i<latencies.length; i++) {
				latSoFar += ISIs[i];
				latencies[i] = latSoFar;								
			}
		}		
		return latencies;
	}
	public double[] getISILatenciesToSecondSpike(int startISIidx){
		double[] latencies = null;
		if(this.ISIs!=null){
			latencies = new double[ISIs.length-startISIidx];
			double latSoFar = this.getFSL();
			for(int i=0; i<ISIs.length; i++) {
				latSoFar += ISIs[i];
				if(i>=startISIidx){
					latencies[i-startISIidx] = latSoFar;	
				}												
			}
		}		
		return latencies;
	}
	/*
	 * ISI latencies calculated for 2nd spike of an ISI (from 2 spikes that form ISI)
	 */
	// Assumption: Spike train is continuous : no silence in the middle
	// get it in a xy format; 
		public double[][] getFirstNISIsAndTheirLatenciesForRegression(int n) {
			double[][] xy = null;
			double[] firstNISIs = getFirstNISIs(n);
			if(firstNISIs!=null) {				
				xy = new double[firstNISIs.length][2];
				double LatencysoFar = this.getFSL();
				for(int i=0; i<xy.length; i++) {
					LatencysoFar += firstNISIs[i];
					xy[i][0] = LatencysoFar;	
					xy[i][1] = firstNISIs[i];
				}
			}
			return xy;
		}
		/*
		 * for the 2nd spike train; must use nsfaisis from the above
		 * and no latency added (no fsl added for x axis)
		 */
		public double[][] getFirstNISIsAndTheirLatenciesForRegression2(int previous_nsfaISIs, int n) {
			double[][] xy = null;
			double[] firstNISIs = getFirstNISIs2(previous_nsfaISIs, n);	
			
			if(firstNISIs!=null) {				
				xy = new double[firstNISIs.length][2];
				double LatencysoFar = 0;
				for(int i=0; i<xy.length; i++) {
					LatencysoFar += firstNISIs[i];
					xy[i][0] = LatencysoFar;	
					xy[i][1] = firstNISIs[i];
				}
			}
			return xy;
		}
	/*
	 * slopes at first n individual points
	 */
	public double[] getFirstNIsiLatencySlopes(int n) {
		double[] slopes = null;		
		double[] ISILatency = getFirstNISIsAndTheirLatenciesToSecondSpike(n+1)[0];
		
		if(ISILatency!=null) {				
			slopes = new double[ISILatency.length-1];
			for(int i=0; i<slopes.length; i++){
				slopes[i] = StatUtil.calculateSlope(ISILatency[i] , ISIs[i], ISILatency[i+1], ISIs[i+1]);
			}
		}
		return slopes;
	}
	/*
	 * slope of the linear regression fit
	 */
	public double calculateSfa() {
		return getSimpleRegression().getSlope();
		//getSimpleRegression().
	}
	public double calculateSfa(int n) {
		//double[][] xy = getFirstNISIsAndTheirLatenciesToSecondSpike(n);		
		//return StatUtil.calculateSlopeOfRegression(xy[0], xy[1]);
		return getSimpleRegression(n).getSlope();
	}
	
	public double calculateSfa2(int pre_n, int n) {
		//double[][] xy = getFirstNISIsAndTheirLatenciesToSecondSpike(n);		
		//return StatUtil.calculateSlopeOfRegression(xy[0], xy[1]);
		return getSimpleRegression2(pre_n, n).getSlope();
	}
	
	public double calculateSfaYintrcpt(int n) {
		//double[][] xy = getFirstNISIsAndTheirLatenciesToSecondSpike(n);		
		//return StatUtil.calculateSlopeOfRegression(xy[0], xy[1]);
		return getSimpleRegression(n).getIntercept();
	}
	
	public double calculateSfaYintrcpt2(int pre_n, int n) {
		//double[][] xy = getFirstNISIsAndTheirLatenciesToSecondSpike(n);		
		//return StatUtil.calculateSlopeOfRegression(xy[0], xy[1]);
		return getSimpleRegression2(pre_n, n).getIntercept();
	}
	public SimpleRegression getSimpleRegression() {
		return getSimpleRegression(sfaISIs);
	}
	public SimpleRegression getSimpleRegression(int n) {
		double[][] xy = getFirstNISIsAndTheirLatenciesForRegression(n);
		
		SimpleRegression sr = new SimpleRegression();
		sr.addData(xy);
		return sr;
	}
	public SimpleRegression getSimpleRegression2(int pre_n, int n) {
		double[][] xy = getFirstNISIsAndTheirLatenciesForRegression2(pre_n, n);
		
		SimpleRegression sr = new SimpleRegression();
		sr.addData(xy);
		return sr;
	}
	public double getLinearFitCoeffOfDeter(){
		return getSimpleRegression().getRSquare();
	}
	
	
	/*
	 * ISI latencies calculated for first spike of an ISI (from 2 spikes that form ISI)
	 */
	// Assumption: Spike train is continuous : no silence in the middle
	/*
	private double[] getISILatenciesToFirstSpike() {
		double[] ISILatency = null;
		if(ISIs!=null) {
			ISILatency = new double[ISIs.length];
			double ISIsoFar = 0;
			for(int i=0; i<ISIs.length; i++) {
				ISILatency[i] = ISIsoFar;			
				ISIsoFar += ISIs[i];
			}
		}
		return ISILatency;
	}*/
/*
 * 
	// to be used to calculate R2 - WONT WORK!!
	public double[] get_first_three_ISIlatencies() {
		double[] first3ISILatencies = null;
		double[] allISILatencies = getISILatenciesToSecondSpike();
		if(allISILatencies!=null) {
			int max_length = (allISILatencies.length < 3)? allISILatencies.length : 3;
			first3ISILatencies = new double[max_length];
			for(int i=0; i<first3ISILatencies.length; i++){
				first3ISILatencies[i] = allISILatencies[i];
			}
		}
		return first3ISILatencies;
	}*/
	/*
public double[] getAllISILatencySlopes() {
	double[] slopes = null;		
	double[] ISILatency = getISILatenciesToFirstSpike();
	if(ISILatency!=null) {
		slopes = new double[ISILatency.length-1];
		for(int i=0; i<ISILatency.length-1; i++){
			slopes[i] = StatUtil.calculateSlope(ISILatency[i] , ISIs[i], ISILatency[i+1], ISIs[i+1]);
		}
	}
	return slopes;
}*/

/*
 * problem specific function - ISI latencies calculated for spike 2 of 2 spikes that form ISI
 */
	/*
public double get_Regression_Slope_of_first_three_ISILatency() {
	double slope = 0;		
	double[] ISILatency = getISILatenciesToFirstSpike2();
	
	if(ISILatency!=null) {
		int max_length = (ISILatency.length < 3)? ISILatency.length : 3;
		double[] tempISI = new double[max_length];
		double[] tempLat = new double[max_length];
		
		for(int i=0;i<max_length;i++) {
			tempISI[i] = ISIs[i];
			tempLat[i] = ISILatency[i];
		}
		slope = StatUtil.calculateSlopeOfRegression(tempLat, tempISI);
	}
	return slope;
}
*/
	/*
	 * ISI latencies calculated for 2nd spike of an ISI (from 2 spikes that form ISI)
	 */
	// Assumption: Spike train is continuous : no silence in the middle
	/*	private double[] getISILatenciesToSecondSpike() {
			double[] ISILatency = null;
			if(ISIs!=null) {
				ISILatency = new double[ISIs.length];
				double LatencysoFar = this.getPreSpikeDelay();
				for(int i=0; i<ISIs.length; i++) {
					LatencysoFar += ISIs[i];
					ISILatency[i] = LatencysoFar;								
				}
			}
			return ISILatency;
		}*/
	public double getISI0(){return ISIs[0];}
	public double getISILast() {return ISIs[ISIs.length-1];}
	public double getFrequency0() {return frequency0;}
	
		
	public static void main(String[] args) {
		
	}
	public int getSfaIsis() {
		return sfaISIs;
	}
	public void setSfaSpikes(int sfaSpikes) {
		this.sfaISIs = sfaSpikes;
	}
	public ModelSpikePatternData getModelSpikePatternData(){
		return spikePatternData;
		
	}
	
	public double measureSWA(double modelVmin){
		double swa = 0;
		double[] ISIs = getISIs();
		if(ISIs==null || ISIs.length ==0) {
			return swa;
		}			
		double timeMin=getTimeMin()+getFSL();
		double timeMax=getTimeMin() + getDurationOfCurrentInjection()-5;		
		double minVolt = getModelSpikePatternData().getMinVoltage(timeMin, timeMax, 1);
		swa= modelVmin - minVolt;
		return swa;
	}
	
	
}
