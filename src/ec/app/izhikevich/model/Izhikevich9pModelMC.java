package ec.app.izhikevich.model;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.stat.StatUtils;

import ec.app.izhikevich.evaluator.MultiCompConstraintEvaluator;
import ec.app.izhikevich.inputprocess.labels.ModelParameterID;
import ec.app.izhikevich.model.neurontypes.mc.EAGenes;
import ec.app.izhikevich.spike.SpikePatternAdapting;
import ec.app.izhikevich.util.GeneralUtils;

public class Izhikevich9pModelMC implements FirstOrderDifferentialEquations{
	
	public static final int SOMA_IDX = 0;
	public static final int DEND_IDX = 1;
	
	protected int nCompartments;
	//attributes	
	protected 	double appCurrent[];
	protected	double durationOfCurrent;
	protected   double timeMin;
	protected	double timeMax; // set based on the duration of current
	//private boolean dendriticInjection;
	
	protected   double k[];	
	protected   double a[] ; 
	protected   double b[] ;	
	protected   double d[];
	
	protected   double cM[] ;	
	protected   double vR[] ;
	protected   double vT[] ;	
	protected   double vPeak[] ;	
	protected   double vMin[];
		
	//Attributes
	
	protected double g[];
	protected double p[];
	
	//compartmental rheobases
	private double rheoBases[];
	
	private boolean iso_comp;
	
	public Izhikevich9pModelMC(int nComps) {	
		this.nCompartments = nComps;
		rheoBases = new double[nComps];
		setIso_comp(false);
	}
	
	public void setInputParameters(double[] appCurrent, double time_min, double duration_of_current) {
		this.appCurrent = appCurrent;
		this.timeMin = time_min;
		this.timeMax = time_min + duration_of_current;	
		this.setDurationOfCurrent(duration_of_current);
		//setDendriticInjection(false);
	}
	
	/*
	 * this method not generic.. implementing '2' compartment without synapse..
	 */
	@Override
	public void computeDerivatives(double t, double[] y, double[] dy)
			throws MaxCountExceededException, DimensionMismatchException {
		double appCurrentSoma;
		double appCurrentDend;
		if(t>=timeMin && t<=timeMax) {
			appCurrentSoma = appCurrent[0];
			appCurrentDend = appCurrent[1];
		}else{
			appCurrentSoma = 0;		
			appCurrentDend = 0;
		}
		
		// Soma
		double V0 = y[0];
		double U0 = y[1];	
		//Dendrite
		double V1 = y[2];
		double U1 = y[3];
		
		//Dendrite
		double iSoma = g[0] * (1-p[0])*(V0 - V1);
		dy[2] = ((k[DEND_IDX] * (V1 - vR[SOMA_IDX]) * (V1 - vT[DEND_IDX]))  - U1 + iSoma + appCurrentDend) / cM[DEND_IDX];
		dy[3] = a[DEND_IDX] * ((b[DEND_IDX] * (V1 - vR[SOMA_IDX])) - U1);	
						
		// Soma	
		double iDend = g[0] * (p[0])*(V1 - V0);
		dy[0] = ((k[SOMA_IDX] * (V0 - vR[SOMA_IDX]) * (V0 - vT[SOMA_IDX]))  - U0 + iDend + appCurrentSoma) / cM[SOMA_IDX];
		dy[1] = a[SOMA_IDX] * ((b[SOMA_IDX] * (V0 - vR[SOMA_IDX])) - U0);		
		
		
	}
	//must override
	public double[] getInitialStateForSolver(){
		//System.out.println("chk2");
		return new double[] {this.vR[0], 0, this.vR[1], 0 };
	}
	//must override
	public int[] getStateIdxToRecordForSolver() {
		return new int[] {0, 2};
	}
	//must override
	public int[] getAllVidxForSolver() {
		return new int[] {0, 2};
	}
	//must override
	public int[] getAllUidxForSolver() {
		return new int[] {1, 3};
		
	}
	public double[] getK() {	return k;}
	public void setK(double[] k) {this.k = k;	}
	public double[] getA() {	return a;}
	public void setA(double[] a) {this.a = a;	}
	public double[] getB() {	return b;}
	public void setB(double[] b) {this.b = b;	}
	public double[] getvR() {	return vR;}
	public void setvR(double vR) {			
		this.vR = new double[nCompartments];
		for(int i=0;i<this.vR.length;i++) {
			this.vR[i] = vR;
		}
	}
	public double[] getvT() {	return vT;}
	public void setvT(double[] vT) {this.vT = vT;}
	public double[] getcM() {	return cM;}
	public void setcM(double[] cM) {this.cM = cM;	}
	//public double[] getvMinOffset() {return this.vMin-this.vR;}
	public double[] getG() { return g; }
	public void setG(double[] g) { this.g = g; }
	public double[] getP() { return p; }
	public void setP(double[] p) { this.p = p; }
	
	public double[] getParm(ModelParameterID id){
		if(id.equals(ModelParameterID.K)) return getK();
		if(id.equals(ModelParameterID.A)) return getA();
		if(id.equals(ModelParameterID.B)) return getB();
		if(id.equals(ModelParameterID.D)) return getD();
		if(id.equals(ModelParameterID.CM)) return getcM();
		if(id.equals(ModelParameterID.VR)) return getvR();
		if(id.equals(ModelParameterID.VT)) return getvT();
		if(id.equals(ModelParameterID.VMIN)) return getvMin();
		if(id.equals(ModelParameterID.VPEAK)) return getvPeak();
		if(id.equals(ModelParameterID.I)) return new double[]{(double) this.appCurrent[0]};
		System.out.println("Not a valid model parameter id-- Izhikevich9pModelMC -- getParm()");
		System.exit(1);
		return null;
	}

	public void setParm(ModelParameterID id, double[] newVal){
		if(id.equals(ModelParameterID.K)) setK(newVal);
		if(id.equals(ModelParameterID.A)) setA(newVal);
		if(id.equals(ModelParameterID.B)) setB(newVal);
		if(id.equals(ModelParameterID.D)) setD(newVal);
		if(id.equals(ModelParameterID.CM)) setcM(newVal);
		if(id.equals(ModelParameterID.VR)) setvR(newVal[0]);
		if(id.equals(ModelParameterID.VT)) setvT(newVal);
		if(id.equals(ModelParameterID.VMIN)) setvMin(newVal);
		if(id.equals(ModelParameterID.VPEAK)) setvPeak(newVal);
		if(id.equals(ModelParameterID.I)) setAppCurrent(newVal);
		//System.out.println("Not a valid model parameter id-- Izhikevich9pModelMC -- setParm()");
		//System.exit(1);
	}
	
	@Override
	public int getDimension() {		
		return   (nCompartments*2);				//izh state variables
	}

	public double[] getD() {
		return d;
	}

	public void setD(double d[]) {
		this.d = d;
	}

	public double[] getvPeak() {
		return vPeak;
	}

	public void setvPeak(double vPeak[]) {
		this.vPeak = vPeak;
	}

	public double[] getvMin() {
		return vMin;
	}

	public void setvMin(double vMin[]) {
		this.vMin = vMin;
	}

	public int getNCompartments(){
		return this.nCompartments;
	}
	public double[] getvMinOffset() {
		return new double[] { this.vMin[0]-this.vR[0], this.vMin[1]-this.vR[1]};
	}	
	
	public double getDurationOfCurrent() {
		return durationOfCurrent;
	}
	public double getTimeMin() {
		return timeMin;
	}
	public double[] getAppCurrent(){
		return this.appCurrent;
	}
	public double getAppCurrent(int compIdx){
		return this.appCurrent[compIdx];
	}
	public void setDurationOfCurrent(double durationOfCurrent) {
		this.durationOfCurrent = durationOfCurrent;
	}
	public void setAppCurrent(double[] currents){
		this.appCurrent = currents;
	}
	public double setAppCurrent(int compIdx, double current){
		return this.appCurrent[compIdx]=current;
	}
	
	public void determineRheobases(float currDur, double iMin, double iMax, double iSearchStep) {
		//return LinearSearchForRheo( compIdx,  currDur,  iMin,  iMax,  iSearchStep);
		for(int i=0;i<nCompartments;i++){
			Izhikevich9pModel isolatedCompartment = getIsolatedCompartment(i);
			double rheo = isolatedCompartment.getRheo(currDur, iMin, iMax, iSearchStep);
			if(rheo>iMax) {
				rheo=iMax+iSearchStep;
			}
			this.rheoBases[i]=rheo;
		}
		
		//return rheo;
		//return binarySearchForRheo( compIdx,  currDur,  iMin,  iMax, iSearchStep);
	}
	
	public void determineRheobases_rb(float currDur, double i_max, double i_min, double iSearchStep) {
		//return LinearSearchForRheo( compIdx,  currDur,  iMin,  iMax,  iSearchStep);
		for(int i=0;i<nCompartments;i++){
			Izhikevich9pModel isolatedCompartment = getIsolatedCompartment(i);
			double rheo = isolatedCompartment.getRheo_rb(currDur, i_max, i_min, iSearchStep);
			if(rheo<i_min) {
				rheo=i_min-iSearchStep;
			}
			this.rheoBases[i]=rheo;
		}
		
		//return rheo;
		//return binarySearchForRheo( compIdx,  currDur,  iMin,  iMax, iSearchStep);
	}
	
	public void determineRampRheobases(float currDur, double iMin, double iMax, double iSearchStep) {
		for(int i=0;i<nCompartments;i++){
			Izhikevich9pModel isolatedCompartment = getIsolatedCompartment(i);
			double rheo = isolatedCompartment.getRampRheo(currDur, iMin, iMax, iSearchStep);
			this.rheoBases[i]=rheo;
		}
	}
	/*
	 * for fast spike class, at least 2 spike to determine frequency based on ISI
	 */
	public float determine2SpikeSomaticRheobase(float currDur, float iMin, float iMax, float iSearchStep) {				
			Izhikevich9pModel isolatedCompartment = getIsolatedCompartment(0);
			float _2spikerheo = isolatedCompartment.get2SpikeRheo(currDur, iMin, iMax, iSearchStep);
			return _2spikerheo;
	}
	public float[] determineVDeflections(float I, float currDur, float vAt) {
		float[] vDefs=new float[this.nCompartments];
		float I_forTest = I;//GeneralUtils.findMin(this.rheoBases)-10;
		
		for(int i=0;i<nCompartments;i++){
			Izhikevich9pModel isolatedCompartment = getIsolatedCompartment(i);
			float v = isolatedCompartment.getVDefAt(I_forTest, currDur, vAt);
			vDefs[i]=v;
		}		
		return vDefs;
	}
	
	public Izhikevich9pModel getIsolatedCompartment(int compIdx){
		Izhikevich9pModel model = new Izhikevich9pModel(); 
        model.setK(this.getK()[compIdx]);
		model.setA(this.getA()[compIdx]);
		model.setB(this.getB()[compIdx]);
		model.setD(this.getD()[compIdx]);	
		model.setcM(this.getcM()[compIdx]);
		model.setvR(this.getvR()[compIdx]);
		model.setvT(this.getvT()[compIdx]);		
		model.setvMin(this.getvMin()[compIdx]);//		
        model.setvPeak(this.getvPeak()[compIdx]);
        return model;
	}
	
	public Izhikevich9pModelMC cloneModelWith(ModelParameterID id, int compIdx, double newVal){
		
		
		Izhikevich9pModelMC newModel = getRightInstanceForModel();
		newModel.setK(getK());
		newModel.setA(getA());
		newModel.setB(getB());
		newModel.setD(getD());	
		newModel.setcM(getcM());
		newModel.setvR(getvR()[0]);
		newModel.setvT(getvT());		
		newModel.setvMin(getvMin());	
		newModel.setvPeak(getvPeak());
		newModel.setG(getG()); 
		newModel.setP(getP());   
       newModel.setInputParameters(appCurrent, timeMin, durationOfCurrent);    
     
       double[] oldVal = getParm(id);
       double[] newVals = oldVal.clone();
       newVals[compIdx] = newVal;
       
       newModel.setParm(id, newVals);
       return newModel;
	}
	

	/*
	 * this module hangs krasnow node.. something wrong with high frequency spike events
	 * also not generic.. applies only to 2 compartment models
	 */
	public int[] getNSpikesForDendFrequency(float dendFreq, float currDur, float iMin, float iMax, float incStep, boolean display)
	   {
		 int[] nSpikes = new int[2];
	     float iForDendFreq = Float.MAX_VALUE;
	     while(iMin <= iMax)    {	
	    	iForDendFreq = (iMin + iMax) / 2;	
	    	//System.out.println(iMin +"\t"+ iMax);
			this.setInputParameters(new double[]{0, iForDendFreq}, timeMin, currDur);			
			IzhikevichSolverMC solver = new IzhikevichSolverMC(this);
			solver.setsS(0.01);
			SpikePatternAdapting[] modelSpikePattern = solver.solveAndGetSpikePatternAdapting();			
			if(modelSpikePattern == null || modelSpikePattern[0] == null || modelSpikePattern[1] == null){				
				iMin = iForDendFreq + incStep;   
				continue;
				//return Float.MAX_VALUE;	 										
			}
			nSpikes[0] = modelSpikePattern[0].getNoOfSpikes();
			nSpikes[1] = modelSpikePattern[1].getNoOfSpikes();
			
			//System.out.println(iForDendFreq +"\t"+ nSpikes[0]+"\t"+nSpikes[1]);
			
			/*if( nSpikes == 1){
				return rheo;
			}else{*/
				 if (modelSpikePattern[1].getFiringFrequencyBasedOnSpikesCount() > dendFreq){                                             
		              iMax = iForDendFreq - incStep;   
		         } else {                                                        
		              iMin = iForDendFreq + incStep;   
		         }		       
			//}			
	     } 
	     if(display){
	    	 System.out.print("\n"+iForDendFreq +"for dend. "+dendFreq+" Hz.\t");
	     }
	    // System.out.println(iForDendFreq);
	 /*  if(GeneralUtils.isCloseEnough(holdIMax, iMax, 1.0)) {
		   return nSpikes;
	   }*/
	   return nSpikes;	         
	  }

	public double[] getRheoBases() {
		return rheoBases;
	}
	private Izhikevich9pModelMC getRightInstanceForModel(){
		if(EAGenes.nComps==1){
			return new Izhikevich9pModel1C(1);
		}
		if(EAGenes.nComps==2){
			return new Izhikevich9pModelMC(2);
		}
		if(EAGenes.nComps==3){
			if(MultiCompConstraintEvaluator.forwardConnectionIdcs[2]==0)
				return new Izhikevich9pModel3C(3);
			else
				return new Izhikevich9pModel3C_L2(3);
		}
		if(EAGenes.nComps==4){
			return new Izhikevich9pModel4C(4);
		}
		System.out.println("rightModel needs to be instantiated!!--Izhikevich9pModelMC.java");
		return null;	
	}

	public boolean isIso_comp() {
		return iso_comp;
	}

	public void setIso_comp(boolean iso_comp) {
		this.iso_comp = iso_comp;
	}
	
}
