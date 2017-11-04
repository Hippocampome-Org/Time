package ec.app.izhikevich.model;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;

public class Izhikevich9pModelMCwSyn extends Izhikevich9pModelMC{
	protected double[] weight;
	protected float[] tau_ampa;
	
	public Izhikevich9pModelMCwSyn(int nComps) {
		super(nComps);
	}

	public double[] getWeight() {
		return weight;
	}
	public void setWeight(double[] weight) {
		this.weight = weight;
	}
	public float[] getTau_ampa() {
		return tau_ampa;
	}
	public void setTau_ampa(float[] tau_ampa) {
		this.tau_ampa = tau_ampa;
	}
	
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
		
		double g_ampa = y[4];
		
		//synaptic
		double iSynapse = g_ampa*(0-V1);		
		dy[4] = -g_ampa/tau_ampa[0];
		//Dendrite
		double iSoma = g[0] * (1-p[0])*(V0 - V1);
		dy[2] = ((k[DEND_IDX] * (V1 - vR[SOMA_IDX]) * (V1 - vT[DEND_IDX]))  - U1 + iSoma + appCurrentDend + iSynapse) / cM[DEND_IDX];
		dy[3] = a[DEND_IDX] * ((b[DEND_IDX] * (V1 - vR[SOMA_IDX])) - U1);	
						
		// Soma	
		double iDend = g[0] * (p[0])* (V1 - V0);
		dy[0] = ((k[SOMA_IDX] * (V0 - vR[SOMA_IDX]) * (V0 - vT[SOMA_IDX]))  - U0 + iDend + appCurrentSoma) / cM[SOMA_IDX];
		dy[1] = a[SOMA_IDX] * ((b[SOMA_IDX] * (V0 - vR[SOMA_IDX])) - U0);		
		
	}
	
	public double[] getInitialStateForSolver(){
		return new double[] {this.vR[0], 0, this.vR[1], 0, this.weight[0] };
	}
	public int[] getStateIdxToRecordForSolver() {
		return new int[] {0, 2};
	}
	public int[] getAllVidxForSolver() {
		return new int[] {0, 2};
	}
	public int[] getAllUidxForSolver() {
		return new int[] {1, 3};		
	}
	
	public int getDimension() {		
		return   (nCompartments*2)+				//izh state variables
				((nCompartments-1)*1);			//syn. state variable per dend compartment
	}		
}
