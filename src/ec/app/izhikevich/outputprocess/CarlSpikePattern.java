package ec.app.izhikevich.outputprocess;

import ec.app.izhikevich.inputprocess.InputSpikePatternConstraint;
import ec.app.izhikevich.model.ODESolution;
import ec.app.izhikevich.util.GeneralUtils;

public class CarlSpikePattern {
	private float I;
	private float IDur;
	private float tStep;
	private double[] vTrace;
	private double[] spikeTimes;
	public float getI() {
		return I;
	}
	public void setI(float i) {
		I = i;
	}
	public float getIDur() {
		return IDur;
	}
	public void setIDur(float iDur) {
		IDur = iDur;
	}
	public float gettStep() {
		return tStep;
	}
	public void settStep(float tStep) {
		this.tStep = tStep;
	}
	public double[] getvTrace() {
		return vTrace;
	}
	public void setvTrace(double[] vTrace) {
		this.vTrace = vTrace;
	}
	public double[] getSpikeTimes() {
		return spikeTimes;
	}
	public void setSpikeTimes(double[] spikeTimes) {
		this.spikeTimes = spikeTimes;
	}
	public ODESolution getODESolutionFormat(){		
		int nTimeSteps = (int)Math.round(IDur/tStep);
		if(nTimeSteps!=vTrace.length){
		//	System.out.println("nTimeSteps -" + nTimeSteps +"- do not match with vTrace length  -" + vTrace.length + "- for I -"+I);
		//	System.exit(-1);
		}
		double[] time = new double[vTrace.length];
		int i=0;
		for(double t=0;GeneralUtils.isCloseEnough(t, IDur, tStep/2.0f);t+=tStep){
			time[i++]=t;
		}
		return new ODESolution(time, vTrace, spikeTimes);
	}
	
	public boolean matchesInputSpikePatternCons(InputSpikePatternConstraint inputCons) {
		double ic_Idur = inputCons.getCurrentDuration();
		double ic_Imin = inputCons.getCurrent().getValueMin();
		double ic_Imax = inputCons.getCurrent().getValueMax();
		
		if(GeneralUtils.isCloseEnough(ic_Idur, IDur, 1f) &&
				I>=ic_Imin && I<=ic_Imax)
			return true;
		
		return false;
	}
	public void display(){
		System.out.println("I:\t"+this.I);
		System.out.println("IDur:\t"+this.IDur);
		System.out.println("tStep:\t"+this.tStep);
		System.out.print("vTrace:\t"); GeneralUtils.displayArray(vTrace);
		System.out.print("spikeTimes:\t"); GeneralUtils.displayArray(spikeTimes);
	}
	
	
}
