package ec.app.izhikevich.spike;

import java.util.ArrayList;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import ec.app.izhikevich.util.StatUtil;

public class BurstPattern {
	/*
	 * DEFN:
	 */
	//private static final double MAX_BURST_ISI = 10;
	//private static final double MIN_IBI = 50;
	
	private ArrayList<ArrayList<Double>> ISIsDuringBurst;
	private ArrayList<Double> IBIs;
	/*
	 * I: clustering used in TSWB.NASP
	 * each individual spike makes a burst, following ISI is IBI
	 * - follow isIBI flag for logic
	 * - worked like a charm with TSWB.NASP 
	 * 
	 * However type II may be required : 
	 * I may not be ideal for TSTUT.ASP.NASP (long train of non-burst spikes),
	 * considering the long train o non-burst spikes as individual bursts may not be a great idea
	 * 
	 */
	public void BurstPattern2(double[] ISIs, int diffFactor) {
		
		if(ISIs!=null){
			ISIsDuringBurst = new ArrayList<>();
			IBIs = new ArrayList<>();
		
			ArrayList<Double> burstISI = new ArrayList<>();
			boolean firstBurst = true;
			boolean isIBI = false;
			for(int i=0;i<ISIs.length;i++){		
				if(i>0	&&	ISIs[i]> diffFactor*ISIs[i-1]){	
					isIBI = true;
				}			
				if(!firstBurst){
					if(ISIs[i] < ISIs[i-1]/(1.0d*diffFactor)){
						isIBI = false;
					}
				}
				if(!isIBI){
					burstISI.add(ISIs[i]);
				}
				if(isIBI){
					ISIsDuringBurst.add(burstISI);
					burstISI = new ArrayList<>();
					IBIs.add(ISIs[i]);	
					firstBurst= false;
				}					
			}
			ISIsDuringBurst.add(burstISI);
		}		
	}
	/*
	 * II: clustering for TSTUT.ASP.NASP	 *  
	 * for long train of non-burst spikes, make them one single burst: is this better than I?
	 * Perhaps I should be incorporated into this later
	 * 
	 * removed the following already commented out lines for exp pstut new logic
	 * 
	 * //(ISIs[i]> diffFactorPre*ISIs[i-1] ) 
								//||
								//new condition: if previous isi was an ibi=> current isi should be smaller than that, otherwise it's another ibi
								//				, causing null burstISIs for previous.
								// oct-15: why this condition was added?
								//(ISIs[i]> ISIs[i-1] && burstISI.isEmpty())
	 */
	public BurstPattern(double[] ISIs, double diffFactorPre, double diffFactorPost) {		
		if(ISIs!=null){
			ISIsDuringBurst = new ArrayList<>();
			IBIs = new ArrayList<>();
		
			ArrayList<Double> burstISI = new ArrayList<>();
			boolean isIBI = false;
			for(int i=0;i<ISIs.length;i++){					
					
				double fac1 = 0;
				double fac2 = 0;
				if(i>0)
					fac1 = ISIs[i] / ISIs[i-1];
				if(i<ISIs.length-1)
					fac2 = ISIs[i] / ISIs[i+1];
				
				if((fac1 > diffFactorPre || i==0) 
						&& (fac2 > diffFactorPost || i==ISIs.length-1)) 						
					isIBI = true;
										
				
				if(!isIBI){
					burstISI.add(ISIs[i]);
				}
				if(isIBI){
					ISIsDuringBurst.add(burstISI);
					burstISI = new ArrayList<>();
					IBIs.add(ISIs[i]);	
					isIBI= false;
				}					
			}			
			ISIsDuringBurst.add(burstISI);
		}		
	}
	/*
	 * III : Clustering based on pre-defined min IBI and max burst ISI (see constants)
	 * not really used anywhere, first attempt this was at clustering
	 * perhaps can be removed
	 */
	/*public BurstPattern(double[] ISIs) {
		if(ISIs!=null){
			ISIsDuringBurst = new ArrayList<>();
			IBIs = new ArrayList<>();
		
			ArrayList<Double> burstISI = new ArrayList<>();
			for(int i=0;i<ISIs.length;i++){
				if(ISIs[i] < MAX_BURST_ISI){
					burstISI.add(ISIs[i]);
				}
				if(ISIs[i]>MIN_IBI && burstISI.size()>0){					
					ISIsDuringBurst.add(burstISI);
					burstISI = new ArrayList<>();
					IBIs.add(ISIs[i]);					
				}
			}		
		}		
	}
	/*
	 * IV: cluster based on number of spikes
	 * probably the worst way of clustering: Think in terms of intervals and durations not #of spikes
	 */
	/*public BurstPattern(int[] nspikes, double[] ISIs) {
		//GeneralUtils.displayArray(ISIs);
		if(ISIs!=null){
			ISIsDuringBurst = new ArrayList<>();
			IBIs = new ArrayList<>();	
			int ISIcnt = 0;
			for(int i=0;i<nspikes.length; i++){
				ArrayList<Double> burstISI = new ArrayList<>();
				for(int j=0;j<nspikes[i]-1;j++){
					if(ISIcnt<ISIs.length)
						burstISI.add(ISIs[ISIcnt++]);
				}
				ISIsDuringBurst.add(burstISI);
				if(ISIcnt<ISIs.length)
					IBIs.add(ISIs[ISIcnt++]);
			}			
		}		
	}*/
	/*
	 * V: not really used anywhere; this was modified to be I to handle TSWB.NASP: 
	 * to decide what should go to IBI or ISI
	 */
	/*public void BurstPattern3(double[] ISIs, int diffFactor) {
		
		if(ISIs!=null){
			ISIsDuringBurst = new ArrayList<>();
			IBIs = new ArrayList<>();
		
			ArrayList<Double> burstISI = new ArrayList<>();
			boolean firstISIinBurst = true;
			for(int i=0;i<ISIs.length;i++){				
				if(!firstISIinBurst && i<ISIs.length-1&&
						ISIs[i]> diffFactor*ISIs[i-1] 
//						&& ISIs[i]> diffFactor*ISIs[i+1]
								){					
					ISIsDuringBurst.add(burstISI);
					burstISI = new ArrayList<>();
					IBIs.add(ISIs[i]);	
					firstISIinBurst= true;
				}else{
					burstISI.add(ISIs[i]);
					firstISIinBurst=false;
				}
					
			}
			ISIsDuringBurst.add(burstISI);
		}		
	}*/
	public boolean isBurst(){
		if(getNBursts()>1) 
			return true;
		/*int realBurstCnt = 0;
		for(ArrayList<Double> burst: ISIsDuringBurst){
			if(burst.size()>0)
				realBurstCnt++;
		}
		if(realBurstCnt>1)
			return true;*/
		return false;
	}

		
	private double[] getFirstNISIs(int burstIdx, int n) {			
		double[] firstNIsis = null;
		if(burstIdx<ISIsDuringBurst.size()){
			ArrayList<Double> ISIs = ISIsDuringBurst.get(burstIdx);
			if(!ISIs.isEmpty()) {
				int max_length = (ISIs.size() < n)? ISIs.size() : n;
				firstNIsis = new double[max_length];
				for(int i=0; i<firstNIsis.length; i++) {
					firstNIsis[i] = ISIs.get(i);
				}
			}
		}
		
		return firstNIsis;
	}
	/*
	 * little different from the one in spike pattern class:
	 * 	- no fsl. latency calculated from first isi
	 */
	public double[][] getFirstNISIsAndTheirLatenciesToSecondSpike(int burstIdx, int n) {
		double[] ISILatency = null;
		double[] firstNISIs = getFirstNISIs(burstIdx, n);
		if(firstNISIs!=null) {				
			ISILatency = new double[firstNISIs.length];
			double LatencysoFar = 0;
			for(int i=0; i<ISILatency.length; i++) {
				LatencysoFar += firstNISIs[i];
				ISILatency[i] = LatencysoFar;								
			}
		}
		return new double[][]{ISILatency, firstNISIs};
	}
	
	/*
	 * only for sfa calculation
	 */
	public double calculateSfa(int burstIdx, int n) {
		return getSimpleRegression(burstIdx,n).getSlope();
	}		
	public SimpleRegression getSimpleRegression(int burstIdx, int n) {
		double[][] xy = getFirstNISIsAndTheirLatenciesForRegression(burstIdx,n);		
		SimpleRegression sr = new SimpleRegression();
		sr.addData(xy);
		return sr;
	}
	public double[][] getFirstNISIsAndTheirLatenciesForRegression(int burstIdx, int n) {
		double[][] xy = null;
		double[] firstNISIs = getFirstNISIs(burstIdx, n);
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
	
	public ArrayList<ArrayList<Double>> getISIsDuringBurst(){
		return ISIsDuringBurst;
	}
	public ArrayList<Double> getIBIs(){
		return IBIs;
	}
	public int getNSpikes(int burstIdx){
		if(burstIdx<ISIsDuringBurst.size()){
			//if(!ISIsDuringBurst.get(burstIdx).isEmpty())
			{
				return ISIsDuringBurst.get(burstIdx).size()+1;
			}
		}				
		else
			return -1;
	}
	public double getIBI(int burstIdx){
		if(burstIdx<IBIs.size())
			return IBIs.get(burstIdx);
		else
			return -1;
	}
	public double getBW(int burstIdx){
		double bw=-1;
		if(burstIdx<ISIsDuringBurst.size()&&
			!ISIsDuringBurst.get(burstIdx).isEmpty()){
			ArrayList<Double> isisDuringBurst = ISIsDuringBurst.get(burstIdx);		
			for(double isi:isisDuringBurst){
				bw+=isi;
			}
		}		
		return bw;
	}
	public int getNBursts(){
		return this.ISIsDuringBurst.size();
	}
	public double getPbiTimeMin(double timeMin, double fsl, int burstIdx){
		double accumDurs = timeMin + fsl;
		for(int i=0;i<=burstIdx; i++){
			if(i>0){
				accumDurs += this.IBIs.get(i-1); 
			}
			accumDurs += getBW(i);
		}
		return accumDurs;
	}
	public double getPbiTimeMax(double timeMin, double fsl, int burstIdx, double timeMax){
		if(burstIdx >= this.IBIs.size()){
			return timeMax;
		}
		return getPbiTimeMin(timeMin, fsl, burstIdx) + this.IBIs.get(burstIdx);
	}
	public void displayBursts(){
		System.out.println(ISIsDuringBurst);
		System.out.println(IBIs);
	}
	
	public double getIntraBurstAvgISI(int n){
		if(n>=ISIsDuringBurst.size()){
			throw new IllegalStateException();
		}
		ArrayList<Double> burst = this.ISIsDuringBurst.get(n);
		if(burst.size()==0) 
			return 0;
		return StatUtil.calculateMean(burst);
	}
	
	public double getAvgIntraBurstAvgISI(){
		int n = this.ISIsDuringBurst.size();
		int realN = 0;
		double IntraburstAvg = 0;
		for(int i=0;i<n;i++){
			if(this.ISIsDuringBurst.get(i).size() > 0){
				IntraburstAvg += StatUtil.calculateMean(this.ISIsDuringBurst.get(i));
				realN++;
			}			
		}
		return (IntraburstAvg/realN*1.0d);
	}
	public static void main(String[] args){
		double[] ISIs = new double[] {333.88,
				18.09,
				16.45,
				16.45,
				14.8,
				21.38,
				19.74,
				18.09,
				18.09,
				18.093,
				19.737
};
		
		BurstPattern bp = new BurstPattern(ISIs, 2.0d, 1.25d);
		bp.displayBursts();
		System.out.println(bp.getAvgIntraBurstAvgISI());
	}
}
