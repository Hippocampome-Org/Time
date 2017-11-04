package ec.app.izhikevich.model;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
/*
 * [1]====((0))====[2]
 */
public class Izhikevich9pModel3CwSyn extends Izhikevich9pModelMCwSyn{
		
	public Izhikevich9pModel3CwSyn(int nComps) {
		super(nComps);
	}

	@Override
	public void computeDerivatives(double t, double[] y, double[] dy)
			throws MaxCountExceededException, DimensionMismatchException {
		double appCurrentSoma;
		double appCurrentDend1;
		double appCurrentDend2;
		
		if(t>=timeMin && t<=timeMax) {
			appCurrentSoma = appCurrent[0];
			appCurrentDend1 = appCurrent[1];
			appCurrentDend2 = appCurrent[2];
		}else{
			appCurrentSoma = 0;		
			appCurrentDend1 = 0;
			appCurrentDend2 = 0;
		}
		
		// Soma
		double V0 = y[0];
		double U0 = y[1];	
		
		//Dendrite1
		double V1 = y[2];
		double U1 = y[3];					
		//synaptic1
		double g_ampa1 =  y[4];	
		double iSynapse1 = g_ampa1*(0-V1);		
		dy[4] = -g_ampa1/tau_ampa[0];				
				
		//Dendrite2
		double V2 = y[5];
		double U2 = y[6];
		//synaptic 2
		double g_ampa2 = y[7];
		double iSynapse2 = g_ampa2*(0-V2);		
		dy[7] = -g_ampa2/tau_ampa[1];			
						
		//Dendrite1
		double iFromSoma = g[0] * (1-p[0])*(V0 - V1);
		dy[2] = ((k[1] * (V1 - vR[1]) * (V1 - vT[1]))  - U1 + iFromSoma + appCurrentDend1 + iSynapse1) / cM[1];
		dy[3] = a[1] * ((b[1] * (V1 - vR[1])) - U1);	
						
		//Dendrite2
		iFromSoma = g[1] * (1-p[1])*(V0 - V2);
		dy[5] = ((k[2] * (V2 - vR[2]) * (V2 - vT[2]))  - U2 + iFromSoma + appCurrentDend2 + iSynapse2) / cM[2];
		dy[6] = a[2] * ((b[2] * (V2 - vR[2])) - U2);
				
		// Soma	
		double iFromDend1 = g[0] * (p[0])* (V1 - V0);
		double iFromDend2 = g[1] * (p[1])* (V2 - V0);
		dy[0] = ((k[SOMA_IDX] * (V0 - vR[SOMA_IDX]) * (V0 - vT[SOMA_IDX]))  - U0 + iFromDend1 + iFromDend2 + appCurrentSoma) / cM[SOMA_IDX];
		dy[1] = a[SOMA_IDX] * ((b[SOMA_IDX] * (V0 - vR[SOMA_IDX])) - U0);		
		
	}
	
	public double[] getInitialStateForSolver(){
		return new double[] {this.vR[0], 0, this.vR[1], 0, this.weight[0], this.vR[2], 0, this.weight[1]};
	}	
	public int[] getStateIdxToRecordForSolver() {
		return new int[] {0, 2, 5};
	}
	public int[] getAllVidxForSolver() {
		return new int[] {0, 2, 5};
	}
	public int[] getAllUidxForSolver() {
		return new int[] {1, 3, 6};		
	}
}
