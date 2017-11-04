package ec.app.izhikevich.model;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;

public class Izhikevich9pModel2CISO extends Izhikevich9pModelMC{
	
	boolean isInhibitory=false;
	double appCurrentSingle;
	double k,a,b,d,cM,vR,vT,vMin,vPeak;
	double g;
	public Izhikevich9pModel2CISO(Izhikevich9pModel model, boolean isInhibitory) {
		super(2);
		
		this.k=model.getK();
		this.a=model.getA();
		this.b=model.getB();
		this.cM=model.getcM();
		this.d=model.getD();
		
		this.vR=model.getvR();
		this.vT=model.getvT();
		this.vMin=model.getC();
		this.vPeak = model.getvPeak();
		
		this.isInhibitory=isInhibitory;
	}
	public void setInputParameters(double appCurrent, double time_min, double duration_of_current) {
		this.appCurrentSingle = appCurrent;
		this.timeMin = time_min;
		this.timeMax = time_min + duration_of_current;	
		this.setDurationOfCurrent(duration_of_current);
	}
	
	@Override
	public void computeDerivatives(double t, double[] y, double[] dy)
			throws MaxCountExceededException, DimensionMismatchException {
		double appCurrent;
		if(t>=timeMin && t<=timeMax) {
			appCurrent = this.appCurrentSingle;
		}else{
			appCurrent = 0;	
		}
		
		// Soma
		double V0 = y[0];
		double U0 = y[1];	
		//Dendrite
		double V1 = y[2];
		double U1 = y[3];
		
		//Dendrite
		double iSoma = g * (V0 - V1);
		if(isInhibitory)
			iSoma = -iSoma;
		dy[2] = ((k * (V1 - vR) * (V1 - vT))  - U1 + iSoma + appCurrent) / cM;
		dy[3] = a * ((b * (V1 - vR)) - U1);	
						
		// Soma	
		double iDend = g * (V1 - V0);
		if(isInhibitory)
			iDend = -iDend;
		dy[0] = ((k * (V0 - vR) * (V0 - vT))  - U0 + iDend + appCurrent) / cM;
		dy[1] = a * ((b * (V0 - vR)) - U0);		
	}
	//must override
	public double[] getInitialStateForSolver(){
		//System.out.println("chk2");
		return new double[] {this.vR, 0, this.vR, 0 };
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
	

	
	@Override
	public int getDimension() {		
		return   4;				//izh state variables
	}

	
	public int getNCompartments(){
		return 2;
	}
	
	public double getDurationOfCurrent() {
		return durationOfCurrent;
	}
	public double getTimeMin() {
		return timeMin;
	}
	
	public void setDurationOfCurrent(double durationOfCurrent) {
		this.durationOfCurrent = durationOfCurrent;
	}
	public void setAppCurrent(double currents){
		this.appCurrentSingle = currents;
	}
	public double setAppCurrent(int compIdx, double current){
		return this.appCurrentSingle=current;
	}
		
}
