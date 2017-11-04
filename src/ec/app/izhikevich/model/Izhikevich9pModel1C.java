package ec.app.izhikevich.model;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;

public class Izhikevich9pModel1C extends Izhikevich9pModelMC{
	
	
	
	public Izhikevich9pModel1C(int nComps) {	
		super(nComps);		
	}
	
	
	/*
	 * this method not generic.. implementing '2' compartment without synapse..
	 */
	@Override
	public void computeDerivatives(double t, double[] y, double[] dy)
			throws MaxCountExceededException, DimensionMismatchException {
		double appCurrentSoma;
		if(t>=timeMin && t<=timeMax) {
			appCurrentSoma = appCurrent[0];
		}else{
			appCurrentSoma = 0;	
		}
		
		// Soma
		double V0 = y[0];
		double U0 = y[1];	
								
		dy[0] = ((k[SOMA_IDX] * (V0 - vR[SOMA_IDX]) * (V0 - vT[SOMA_IDX]))  - U0 +  appCurrentSoma) / cM[SOMA_IDX];
		dy[1] = a[SOMA_IDX] * ((b[SOMA_IDX] * (V0 - vR[SOMA_IDX])) - U0);		
		
		
	}
	//must override
	public double[] getInitialStateForSolver(){
		return new double[] {this.vR[0], 0 };
	}
	//must override
	public int[] getStateIdxToRecordForSolver() {
		return new int[] {0};
	}
	//must override
	public int[] getAllVidxForSolver() {
		return new int[] {0};
	}
	//must override
	public int[] getAllUidxForSolver() {
		return new int[] {1};
		
	}
	

	
}
