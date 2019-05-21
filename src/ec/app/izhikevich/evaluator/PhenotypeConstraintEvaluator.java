package ec.app.izhikevich.evaluator;

import ec.app.izhikevich.inputprocess.PhenotypeConstraint;
import ec.app.izhikevich.inputprocess.labels.PhenotypeConstraintAttributeID;
import ec.app.izhikevich.inputprocess.labels.PhenotypeConstraintType;
import ec.app.izhikevich.model.Izhikevich9pModelMC;
import ec.app.izhikevich.model.IzhikevichSolverMC;
import ec.app.izhikevich.outputprocess.CarlMcSimData;
import ec.app.izhikevich.spike.SpikePatternAdapting;
import ec.app.izhikevich.util.GeneralUtils;
import ec.app.izhikevich.util.StatUtil;

public class PhenotypeConstraintEvaluator{
	
	Izhikevich9pModelMC model;
	private boolean display;
	
	private CarlMcSimData carlMcSimData; // for later
	private boolean externalSimulatorUsed; // for later
	
	public PhenotypeConstraintEvaluator(Izhikevich9pModelMC model, boolean display, CarlMcSimData carlMcData) {
		this.model = model;
		this.display = display;
		carlMcSimData = carlMcData;
		externalSimulatorUsed = true;
	}	
	
	public PhenotypeConstraintEvaluator(Izhikevich9pModelMC model, boolean display) {
		this.model = model;
		this.display = display;
		carlMcSimData = null;
		externalSimulatorUsed = false;
	}	
	
	public float calculatePhenotypeConstraintError(PhenotypeConstraint phenConstraintData){
		float consError = Float.MAX_VALUE;	
		
		if(phenConstraintData.getType().equals(PhenotypeConstraintType.FAST_SPIKER)) {	
			float cons_weight =  (float)phenConstraintData.getAttribute(PhenotypeConstraintAttributeID.cons_weight);
			if(externalSimulatorUsed){
				consError = 0;
				//consError = cons_weight * dendriticExcitabilityError(); // later
			}else{
				float current_min = (float)phenConstraintData.getAttribute(PhenotypeConstraintAttributeID.current_min);
				float current_max = (float)phenConstraintData.getAttribute(PhenotypeConstraintAttributeID.current_max);
				float current_dur = (float)phenConstraintData.getAttribute(PhenotypeConstraintAttributeID.current_duration);
				float current_step = (float)phenConstraintData.getAttribute(PhenotypeConstraintAttributeID.current_step);	
				float min_freq = (float)phenConstraintData.getAttribute(PhenotypeConstraintAttributeID.min_freq);	
				consError = cons_weight * minFreqError(min_freq, current_min, current_max, current_dur, current_step);
			}			
		}	
		return consError;
	}
	
	public float minFreq() {
		float _2SpikeSomaticRheo = model.determine2SpikeSomaticRheobase(500, 10, 500, 1);
		double[] newIs = new double[model.getNCompartments()];
		newIs[0] = _2SpikeSomaticRheo+5;		
		model.setInputParameters(newIs, 0d, (double)500);
		
		IzhikevichSolverMC solver = new IzhikevichSolverMC(model);
		solver.setsS(0.1);
		SpikePatternAdapting[] modelSpikePattern = solver.solveAndGetSpikePatternAdapting();			
		if(modelSpikePattern == null || modelSpikePattern[0] == null){						
			return 0;	 										
		}		
		return (float)modelSpikePattern[0].getFiringFrequencyBasedOnISIs();
	}
	/*
	 * minFreq error for fast spiker
	 */
	private float minFreqError(float min_freq, float iMin, float iMax, float iDur, float iStep) {
		float error = 0;				
		float _2SpikeSomaticRheo = model.determine2SpikeSomaticRheobase(iDur, iMin, iMax, iStep);
		
		double[] newIs = new double[model.getNCompartments()];
		newIs[0] =  _2SpikeSomaticRheo+iStep;		
		model.setInputParameters(newIs, 0d, (double)iDur);
		
		IzhikevichSolverMC solver = new IzhikevichSolverMC(model);
		solver.setsS(0.1);
		SpikePatternAdapting[] modelSpikePattern = solver.solveAndGetSpikePatternAdapting();			
		if(modelSpikePattern == null || modelSpikePattern[0] == null){						
			return Float.MAX_VALUE;	 										
		}
		
		double minModelfreq = modelSpikePattern[0].getFiringFrequencyBasedOnISIs();
		error += (float) StatUtil.calculateObsNormalizedError(min_freq, minModelfreq);
				
			if(display) {
				String displayString = "\nFastSpikerErr.\t\t(I:"+newIs[0]+"pA)"+
										GeneralUtils.formatTwoDecimal(min_freq)+"\t"+
										GeneralUtils.formatTwoDecimal(minModelfreq)+"\t"+
										error;
				System.out.print(displayString);
			}	
		
		return error;
	}
	/*
	 * from external simulator LATER!!
	 */
	private float minFreqError() {
		float error = 0;				
		double[] iComp = carlMcSimData.getRampRheos();	
		for(int i=1;i<iComp.length;i++){
			if(iComp[i]>iComp[0]){
				error += 0;
			}else{
				error += (float) StatUtil.calculateObsNormalizedError(iComp[0], iComp[i]);
			}			
			if(display) {
				String displayString = "\nRampRheoErr(cmp#"+i+").\t\t\t"+
										GeneralUtils.formatTwoDecimal(iComp[0])+"\t"+
										GeneralUtils.formatTwoDecimal(iComp[i])+"\t"+
										error;
				System.out.print(displayString);
			}	
		}
		return error;
	}
	
	
}
