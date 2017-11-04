package ec.app.izhikevich.evaluator.qualifier;

public enum ClassificationParameterID {
	B_1p,
	M_2p, B_2p,
	M_3p, B1_3p, B2_3p,
	M1_4p, B1_4p, M2_4p, B2_4p,
	
	N_ISI_cut_3p, N_ISI_cut_4p,
	
	F_12, F_crit_12, F_23, F_crit_23, F_34, F_crit_34,
	P_12, P_12_UV, P_23, P_23_UV, P_34, P_34_UV,
	
	M_RASP, B_RASP, N_ISI_cut_RASP;
	
	public static void main(String[] args){
		ClassificationParameterID[] parms = ClassificationParameterID.values();
		for(ClassificationParameterID parm: parms){
			System.out.print(parm+"\t");
		}
	}
}
