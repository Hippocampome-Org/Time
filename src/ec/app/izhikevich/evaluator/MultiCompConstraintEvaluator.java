package ec.app.izhikevich.evaluator;

import java.util.ArrayList;
import java.util.List;

import ec.app.izhikevich.inputprocess.InputMCConstraint;
import ec.app.izhikevich.inputprocess.labels.MCConstraintAttributeID;
import ec.app.izhikevich.inputprocess.labels.MCConstraintType;
import ec.app.izhikevich.model.Izhikevich9pModel3C;
import ec.app.izhikevich.model.Izhikevich9pModel3C_L2;
import ec.app.izhikevich.model.Izhikevich9pModel3CwSyn;
import ec.app.izhikevich.model.Izhikevich9pModel3CwSyn_L2;
import ec.app.izhikevich.model.Izhikevich9pModel4CwSyn;
import ec.app.izhikevich.model.Izhikevich9pModelMC;
import ec.app.izhikevich.model.Izhikevich9pModelMCwSyn;
import ec.app.izhikevich.model.IzhikevichSolver;
import ec.app.izhikevich.model.IzhikevichSolverMC;
import ec.app.izhikevich.outputprocess.CarlMcSimData;
import ec.app.izhikevich.spike.SpikePatternAdapting;
import ec.app.izhikevich.util.GeneralUtils;
import ec.app.izhikevich.util.ModelDBInterface;
import ec.app.izhikevich.util.StatUtil;

public class MultiCompConstraintEvaluator{
/*
 * Remember, all error methods here return weighted 0-1 normed errors
 */
	public static boolean TURN_OFF_MC_ERROR_DISPLAY=false;
	public static int[] forwardConnectionIdcs;
	Izhikevich9pModelMC model;
	private boolean display;
	private double[] synWeight;
	private boolean rampRheo;
	
	private List<Float> SP_Is_hold;
	private boolean saveSP_Is;
	
	private CarlMcSimData carlMcSimData;
	private boolean externalSimulatorUsed;
	
	public MultiCompConstraintEvaluator(Izhikevich9pModelMC model, boolean display, double[] weight,  CarlMcSimData carlMcData) {
		this.model = model;
		this.display = display;
		carlMcSimData = carlMcData;
		externalSimulatorUsed = true;
		SP_Is_hold=null;
		saveSP_Is=false;
	}	
	
	public MultiCompConstraintEvaluator(Izhikevich9pModelMC model, boolean display, double[] weight, boolean rampRheo) {
		this.model = model;
		this.display = display;
		this.synWeight = weight;
		this.rampRheo = rampRheo;
		carlMcSimData = null;
		externalSimulatorUsed = false;
		SP_Is_hold=null;
		saveSP_Is=false;
	}	
	
	public float calculateMcConstraintError(InputMCConstraint mcConstraintData){
		float consError = Float.MAX_VALUE;	
		
		if(mcConstraintData.getType().equals(MCConstraintType.EXCITABILITY)) {	
			float cons_weight =  (float)mcConstraintData.getAttribute(MCConstraintAttributeID.cons_weight);
			if(externalSimulatorUsed){
				consError = cons_weight * dendriticExcitabilityError();
			}else{
				double current_min = mcConstraintData.getAttribute(MCConstraintAttributeID.current_min);
				double current_max = mcConstraintData.getAttribute(MCConstraintAttributeID.current_max);
				float current_dur = (float) ModelDBInterface.MC_RHEO_DUR;//(float)mcConstraintData.getAttribute(MCConstraintAttributeID.current_duration);
				double current_step = mcConstraintData.getAttribute(MCConstraintAttributeID.current_step);	
				double rheo_diff = mcConstraintData.getAttribute(MCConstraintAttributeID.rheo_diff);	
				consError = cons_weight * dendriticExcitabilityError(rheo_diff, current_min, current_max, current_dur, current_step);
			}			
		}			
		
		if(mcConstraintData.getType().equals(MCConstraintType.INP_RES)) {
			float cons_weight =  (float)mcConstraintData.getAttribute(MCConstraintAttributeID.cons_weight);
			if(externalSimulatorUsed){
				consError = cons_weight * dendriticIRError();
			}else{
				float current_dur = (float)mcConstraintData.getAttribute(MCConstraintAttributeID.current_duration);
				current_dur = (float) ModelDBInterface.MC_IR_DUR;
				float v_at_time = (float)mcConstraintData.getAttribute(MCConstraintAttributeID.v_at_time);	
				float current = (float) ModelDBInterface.MC_IR_I;//(float)mcConstraintData.getAttribute(MCConstraintAttributeID.current);
				consError = cons_weight * dendriticIRError(current, current_dur, v_at_time);
			}
		}
		
		if(mcConstraintData.getType().equals(MCConstraintType.PROPAGATION)) {
			float cons_weight =  (float)mcConstraintData.getAttribute(MCConstraintAttributeID.cons_weight);
			if(externalSimulatorUsed){
				consError = cons_weight * forwardSpikePropagationError();
			}else{	
				float dend_current_min = (float) ModelDBInterface.MC_SP_I_MIN;;//(float)mcConstraintData.getAttribute(MCConstraintAttributeID.dend_current_min);
				float dend_current_max = (float)mcConstraintData.getAttribute(MCConstraintAttributeID.dend_current_max);
				float dend_current_time_min = (float) ModelDBInterface.MC_SP_TIME_MIN;//(float)mcConstraintData.getAttribute(MCConstraintAttributeID.dend_current_time_min);
				float dend_current_duration = (float) ModelDBInterface.MC_SP_DUR;//(float)mcConstraintData.getAttribute(MCConstraintAttributeID.dend_current_duration);
				float dend_current_step = (float)ModelDBInterface.MC_SP_I_STEP;//mcConstraintData.getAttribute(MCConstraintAttributeID.dend_current_step);
				float dend_target_spike_freq = (float)mcConstraintData.getAttribute(MCConstraintAttributeID.dend_target_spike_freq);
				float spike_prop_rate_min = (float)mcConstraintData.getAttribute(MCConstraintAttributeID.spike_prop_rate_min);		
				
				consError = cons_weight * forwardSpikePropagationError(dend_current_min, dend_current_max,
						dend_current_time_min, dend_current_duration, 
						dend_current_step, 
						dend_target_spike_freq,
						spike_prop_rate_min);
				
			//	consError = cons_weight * forwardSpikePropagationError(spike_prop_rate_min);
			}
		}
		
		if(mcConstraintData.getType().equals(MCConstraintType.SYN_STIM_EPSP)) {
			float cons_weight =  (float)mcConstraintData.getAttribute(MCConstraintAttributeID.cons_weight);
			double[] expEPSP = mcConstraintData.getAttributeWrange(MCConstraintAttributeID.ampa_epsp);	
			if(externalSimulatorUsed){
				consError = cons_weight * synapticallyStimulatedEpspError((float)expEPSP[0], (float)expEPSP[1]);
			}else{		
				float timeConst = ModelDBInterface.MC_EPSP_TC;//(float)mcConstraintData.getAttribute(MCConstraintAttributeID.ampa_tau);
				float simDur = (float) ModelDBInterface.MC_EPSP_DUR;//(float)mcConstraintData.getAttribute(MCConstraintAttributeID.sim_duration);
				consError = cons_weight * synapticallyStimulatedEpspError((float)expEPSP[0], (float)expEPSP[1], timeConst, simDur);
			}
		}
		
		return consError;
	}
	
	/*
	 * dendritic excitability based on Rheobase I
	 */
	private float dendriticExcitabilityError(double rheoDiff, double iMin, double iMax, float iDur, double iStep) {
		float error = 0;			
		if(!rampRheo)
			model.determineRheobases(iDur, iMin, iMax, iStep);
		else
			model.determineRampRheobases(iDur, iMin, iMax, iStep);
		
		double[] iComp = model.getRheoBases();			
	
		for(int i=1;i<iComp.length;i++){
			if(iComp[i]>iComp[0] && iComp[i]-iComp[0]>0.1){
				error += 0;
			}else{
				error += (float) StatUtil.calculateObsNormalizedError(iComp[0]+0.1, iComp[i]);
			}	
			if(display) {
				String displayString = "";
				if(!TURN_OFF_MC_ERROR_DISPLAY) {
					displayString = "\nRheoErr(cmp#"+i+").\t\t\t"+
							//GeneralUtils.formatThreeDecimal
							(iComp[0])+"\t"+
							//GeneralUtils.formatThreeDecimal
							(iComp[i])+"\t"+
							error;
				}else {
					displayString = "\nStep_Rheos(cmp#"+i+").\t\t\t"+
							//GeneralUtils.formatThreeDecimal
							(iComp[0])+"\t"+
							//GeneralUtils.formatThreeDecimal
							(iComp[i]);
				}
				
				
				System.out.print(displayString);
			}	
		}
		return error;
	}
	/*
	 * from external simulator
	 */
	private float dendriticExcitabilityError() {
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
	
	/*
	 * Input Resistance - relative
	 */
	private float dendriticIRError(float I, float iDur, float v_at_dur) {
		float error = 0;		
		float[] vDeflection = model.determineVDeflections(I, iDur, v_at_dur);
		
		for(int i=1;i<vDeflection.length;i++){
			if(vDeflection[i] > vDeflection[0]){
				error += 0;
			}else{
				error += (float) StatUtil.calculateObsNormalizedError(vDeflection[0], vDeflection[i]);
			}	
			if(display) {
				String displayString="";
				if(!TURN_OFF_MC_ERROR_DISPLAY) {
					displayString = "\nIRErr(cmp#"+i+").\t("+
							I
							//GeneralUtils.formatTwoDecimal(GeneralUtils.findMin(model.getRheoBases())-10)
							+"pA)\t"+
							GeneralUtils.formatTwoDecimal(vDeflection[0])+"\t"+
							GeneralUtils.formatTwoDecimal(vDeflection[i])+"\t"+
							error;
				}else {
					displayString = "\nIR_Vdefs(cmp#"+i+").\t("+
							I
							//GeneralUtils.formatTwoDecimal(GeneralUtils.findMin(model.getRheoBases())-10)
							+"pA)\t"+
							GeneralUtils.formatTwoDecimal(vDeflection[0])+"\t"+
							GeneralUtils.formatTwoDecimal(vDeflection[i]);
				}				
				System.out.print(displayString);
			}	
		}
			
		//}		
			
		return error;
	}
	/*
	 * from external simulator
	 */
	private float dendriticIRError() {
		float enforcedDiff = 0f;
		float error = 0;		
		double[] absVs = carlMcSimData.getVdefs();
		
		for(int i=1;i<absVs.length;i++){
			double somaVDef = Math.abs(absVs[0] - model.getvR()[0]);
			double dendVdef = Math.abs(absVs[i] - model.getvR()[0]);
			if(dendVdef > (somaVDef+enforcedDiff)){
				error += 0;
			}else{
				error += (float) StatUtil.calculateObsNormalizedError((somaVDef+enforcedDiff), dendVdef);
			}			
			if(display) {
				String displayString = "\nIRErr(cmp#"+i+").\t("+
										"-100"
										//GeneralUtils.formatTwoDecimal(GeneralUtils.findMin(model.getRheoBases())-10)
										+"pA)\t"+
										GeneralUtils.formatTwoDecimal(somaVDef)+"\t"+
										GeneralUtils.formatTwoDecimal(dendVdef)+"\t"+
										error;
				System.out.print(displayString);
			}	
		}
			
		//}		
			
		return error;
	}
	public float forwardSpikePropagationError(float dend_curr_min,
											float dend_curr_max,
											float dend_current_time_min,
											float dend_current_duration,
											float dend_current_step,
											float dend_target_spike_freq,
											float spike_prop_rate_min) {
		float error = 0;	
		
		for(int i=1;i<model.getNCompartments();i++){
			float[] spikeCounts = propagatedSpikeCounts(i, forwardConnectionIdcs[i], 
														dend_curr_min, 
														dend_curr_max,
														dend_current_time_min,
														dend_current_duration,
														dend_current_step,
														dend_target_spike_freq);
			
			float dendFreq = spikeCounts[0]/(dend_current_duration/1000);			
			float proprate = spikeCounts[1]/spikeCounts[0];
			float targetProprate=0.5f;
			
			if(Float.isNaN(proprate)) {
				error += 100;
			}else {			
				
				if(dendFreq<25) {
					targetProprate=1f-(dendFreq/100f);
				}					
				
				if(proprate >= targetProprate) {
					error +=0;
				}else {
					error += targetProprate-proprate;
				}
			}
			
			/*if(spikeCounts[1]<1) {
				error +=  1;
				//return error;
			}*/
			if(display) {
				String displayString = "";
				if(!TURN_OFF_MC_ERROR_DISPLAY) {
					displayString = "\nfrwSpkPrpErr(cmp#"+i+"->"+forwardConnectionIdcs[i]+").\t"+spikeCounts[0]+"(I:"+spikeCounts[2]+")\t"+spikeCounts[1]+"\t"+
							"("+targetProprate+"-"+proprate+")\t"+error;
				}else {
					displayString = "\nfrwSpkPrp(cmp#"+i+"->"+forwardConnectionIdcs[i]+").\t"+spikeCounts[0]+"(I:"+spikeCounts[2]+")\t"+spikeCounts[1];
				}
				
				System.out.print(displayString);
			}
			if(saveSP_Is) {
				this.addSP_Is_hold(spikeCounts[2]);
			}
			
		}
		
		return error;	
	}
	
	public float forwardSpikePropagationError() {
		float Toterror = 0;			
		float[] spikePropRates = carlMcSimData.getSpikePropRates();
		for(int i=0; i<spikePropRates.length;i++){
			double error = StatUtil.calculateObsNormalizedError(1, spikePropRates[i]);		
			Toterror += error;
			if(display) {
				String displayString = "\nfrwSpkPrpErr(FrmCmp#"+(i+1)+").\t"+spikePropRates[i]+"\t"+error;
				System.out.print(displayString);
			}
		}		
		return Toterror;	
}
	public float[] propagatedSpikeCounts(int fromCompIdx, int toCompIdx,
			float dend_curr_min,
			float dend_curr_max,
			float dend_current_time_min,
			float dend_current_duration,
			float dend_current_step,
			float dend_target_spike_freq){
		
		float[] spikes = null;
		if(!display)
			spikes= new float[2];
		else
			spikes = new float[3];        //to append the current corresponding to the event!
			
		float I=0;
		double[] appCurrent = new double[model.getNCompartments()];
		for(I=dend_curr_min; I<=dend_curr_max;I+=dend_current_step){
			appCurrent[fromCompIdx] = I;
			this.model.setInputParameters(appCurrent, dend_current_time_min, dend_current_duration);
			IzhikevichSolverMC solver = new IzhikevichSolverMC(model);
			solver.setsS(0.1);
			SpikePatternAdapting[] model_spike_pattern = solver.solveAndGetSpikePatternAdapting();				
			if(model_spike_pattern==null) {			
				continue;
			}
			for(int i=0;i<model_spike_pattern.length;i++){
				if(model_spike_pattern[i]==null) 
					continue;
			}
			if(model_spike_pattern[fromCompIdx].getFiringFrequencyBasedOnSpikesCount() >= dend_target_spike_freq){
				spikes[0] = model_spike_pattern[fromCompIdx].getNoOfSpikes();
				spikes[1] = model_spike_pattern[toCompIdx].getNoOfSpikes();
				if(display || saveSP_Is)
					spikes[2] = I;
			//	model_spike_pattern[Izhikevich9pModel2CA.DEND_IDX].getSpikePatternData().displayForPlot();
			//	System.out.println(I);
				break;
			}			
		}
		return spikes;
	}
	
	private float synapticallyStimulatedEpspError(float expEPSPmin, float expEPSPmax, float timeConst, float simDur) {
		float error = 0;		
		
		Izhikevich9pModelMCwSyn modelwSyn = getRightInstanceForModelWSyn(); 
		modelwSyn.setK(model.getK());
		modelwSyn.setA(model.getA());
		modelwSyn.setB(model.getB());
		modelwSyn.setD(model.getD());	
		modelwSyn.setcM(model.getcM());
		modelwSyn.setvR(model.getvR()[0]);
		modelwSyn.setvT(model.getvT());		
		modelwSyn.setvMin(model.getvMin());//		
		modelwSyn.setvPeak(model.getvPeak());
		modelwSyn.setG(model.getG()); 
		modelwSyn.setP(model.getP());        
		float stepSize = (float) IzhikevichSolver.SS;
		double[] appCurrent = new double[model.getNCompartments()];
		modelwSyn.setInputParameters(appCurrent, 0, simDur);		
		//should timeConstant be different?// if so, should be added from input file
		float[] timeConstant = new float[synWeight.length];	
		for(int i=0;i<timeConstant.length;i++)
			timeConstant[i]=timeConst;		
		modelwSyn.setTau_ampa(timeConstant);
		
		for(int i=0;i<synWeight.length;i++){
			double[] weight = new double[synWeight.length];
				//change RHS  to [0]	
			weight[i]=synWeight[i];
			modelwSyn.setWeight(weight);
			
			
			IzhikevichSolverMC solver = new IzhikevichSolverMC(modelwSyn);			
			solver.setsS(stepSize);
			SpikePatternAdapting[] model_spike_pattern = solver.solveAndGetSpikePatternAdapting();		
			if(model_spike_pattern!=null) {	
				for(SpikePatternAdapting modelSpikePattern: model_spike_pattern){
					if(modelSpikePattern==null)
						return Float.MAX_VALUE;
					}
				}
			else
				return Float.MAX_VALUE;		
				
			double peakVolt = model_spike_pattern[0].getSpikePatternData().getPeakVoltage(stepSize,simDur,stepSize);
			double somaEpsp = (double)peakVolt - model.getvR()[0];
			
		//	model_spike_pattern[Izhikevich9pModel2CAwSyn.SOMA_IDX].getSpikePatternData().displayForPlot();
			
			error += (float) StatUtil.calculateObsNormalizedError(expEPSPmin, expEPSPmax, somaEpsp);
			if(display) {
				String displayString ="";
				if(!TURN_OFF_MC_ERROR_DISPLAY) {
					 displayString = "\nSynStimEpspErr(syn#"+i+").\t("+expEPSPmin+","+expEPSPmax+")\t"+
								GeneralUtils.formatTwoDecimal(somaEpsp)+"\t"+error;
				}else {
					 displayString = "\nSynStimEpsp(syn#"+i+").\t("+expEPSPmin+","+expEPSPmax+")\t"+
								GeneralUtils.formatTwoDecimal(somaEpsp);
				}
				
				System.out.print(displayString);
			}	
		}
		
		return error;		
	}
	
	private float synapticallyStimulatedEpspError(float expEPSPmin, float expEPSPmax) {
		float error = 0;		
		double[] absEPSPS = carlMcSimData.getEpsps();
		
		for(int i=0;i<absEPSPS.length;i++){
			double relEPSP = Math.abs(absEPSPS[i] - model.getvR()[0]);			
			error += (float) StatUtil.calculateObsNormalizedError(expEPSPmin, expEPSPmax, relEPSP);
					
			if(display) {
				String displayString = "\nSynStimEpspErr(syn#"+i+").\t("+expEPSPmin+","+expEPSPmax+")\t"+
									GeneralUtils.formatTwoDecimal(relEPSP)+"\t"+error;
				System.out.print(displayString);
			}	
		}
			
		//}		
			
		return error;
	}
	private Izhikevich9pModelMCwSyn getRightInstanceForModelWSyn(){
		if(model.getNCompartments()==2){
			return new Izhikevich9pModelMCwSyn(2);
		}
		if(model.getNCompartments()==3){
			if(MultiCompConstraintEvaluator.forwardConnectionIdcs[2]==0)
				return new Izhikevich9pModel3CwSyn(3);
			else
				return new Izhikevich9pModel3CwSyn_L2(3);
		}
		if(model.getNCompartments()==4){
			return new Izhikevich9pModel4CwSyn(4);
		}
		System.out.println("rightModelwSyn needs to be instantiated!!");
		return null;	
	}

	/*
	 * older 0-1 norm errors
	 */
	private float dendriticExcitabilityError01norm(float rheoDiff, float iMin, float iMax, float iDur, float iStep) {
		float error = 0;				
		model.determineRheobases(iDur, iMin, iMax, iStep);
		double[] iComp = model.getRheoBases();
			
	/*	if(iDend > iSoma) {
			error = 0;
		}else{	*/	
		double expRateForNorm = 100; // more sensitive to differences below 100 
		for(int i=1;i<iComp.length;i++){
			error += (float) StatUtil.calculate0to1NormalizedErrorExp(iComp[0], iComp[0]+rheoDiff,
					iComp[i], 
					expRateForNorm);
			if(display) {
				String displayString = "\nRheoErr(cmp#"+i+").\t\t\t"+
										GeneralUtils.formatTwoDecimal(iComp[0])+"\t"+
										GeneralUtils.formatTwoDecimal(iComp[i])+"\t"+
										error;
				System.out.print(displayString);
			}	
		}
		return error;
	}

	private float dendriticIRError01Norm(float I, float iDur, float v_at_dur) {
		float error = 0;		
		float[] vDeflection = model.determineVDeflections(I, iDur, v_at_dur);
			
	
		double expRateForNorm = 10; // more sensitive to differences below 100 
		for(int i=1;i<vDeflection.length;i++){
			error += (float) StatUtil.calculate0to1NormalizedErrorExp(vDeflection[0], vDeflection[0]+model.getvPeak()[i],
					vDeflection[i], 
					expRateForNorm); //2nd argument is just an arbitrary high value to allow for not adding additional check!
			if(display) {
				String displayString = "\nIRErr(cmp#"+i+").\t("+
										"-100"
										//GeneralUtils.formatTwoDecimal(GeneralUtils.findMin(model.getRheoBases())-10)
										+"pA)\t"+
										GeneralUtils.formatTwoDecimal(vDeflection[0])+"\t"+
										GeneralUtils.formatTwoDecimal(vDeflection[i])+"\t"+
										error;
				System.out.print(displayString);
			}	
		}
			
		//}		
			
		return error;
	}

	public float forwardSpikePropagationError01Morm(float dend_curr_min,
			float dend_curr_max,
			float dend_current_time_min,
			float dend_current_duration,
			float dend_current_step,
			float dend_target_spike_freq,
			float spike_prop_rate_min) {
		float error = 0;	
		
		for(int i=1;i<model.getNCompartments();i++){
		float[] spikeCounts = propagatedSpikeCounts(i, forwardConnectionIdcs[i], 
								dend_curr_min, 
								dend_curr_max,
								dend_current_time_min,
								dend_current_duration,
								dend_current_step,
								dend_target_spike_freq);
		
		if(spikeCounts[0]<1) {
		error =  Float.MAX_VALUE;
		if(display) {
		String displayString = "\nfrwSpkPrpErr(cmp#"+i+"->"+forwardConnectionIdcs[i]+").\t"+spikeCounts[0]+"(I:"+spikeCounts[2]+")\t"+spikeCounts[1]+"\t"+error;
		System.out.print(displayString);
		}
		return error;
		}
		
		float rate = spikeCounts[1]/spikeCounts[0];
		if(rate >= spike_prop_rate_min)
		error += 0;
		else
		error += (float)StatUtil.calculate0to1NormalizedError(spike_prop_rate_min, rate, 0, 1);		
		if(display) {
		String displayString = "\nfrwSpkPrpErr(cmp#"+i+"->"+forwardConnectionIdcs[i]+").\t"+spikeCounts[0]+"(I:"+spikeCounts[2]+")\t"+spikeCounts[1]+"\t"+error;
		System.out.print(displayString);
		}
		}
		return error;	
		}
	
	private float synapticallyStimulatedEpspError01norm(float expEPSPmin, float expEPSPmax, float timeConst, float simDur) {
		float error = 0;		
		
		Izhikevich9pModelMCwSyn modelwSyn = getRightInstanceForModelWSyn(); 
		modelwSyn.setK(model.getK());
		modelwSyn.setA(model.getA());
		modelwSyn.setB(model.getB());
		modelwSyn.setD(model.getD());	
		modelwSyn.setcM(model.getcM());
		modelwSyn.setvR(model.getvR()[0]);
		modelwSyn.setvT(model.getvT());		
		modelwSyn.setvMin(model.getvMin());//		
		modelwSyn.setvPeak(model.getvPeak());
		modelwSyn.setG(model.getG()); 
		modelwSyn.setP(model.getP());        
		float stepSize = (float) IzhikevichSolver.SS;
		double[] appCurrent = new double[model.getNCompartments()];
		modelwSyn.setInputParameters(appCurrent, 0, simDur);		
		//should timeConstant be different?// if so, should be added from input file
		float[] timeConstant = new float[synWeight.length];	
		for(int i=0;i<timeConstant.length;i++)
			timeConstant[i]=timeConst;		
		modelwSyn.setTau_ampa(timeConstant);
		
		for(int i=0;i<synWeight.length;i++){
			double[] weight = new double[synWeight.length];
					
			weight[i]=synWeight[i];
			modelwSyn.setWeight(weight);
			
			
			IzhikevichSolverMC solver = new IzhikevichSolverMC(modelwSyn);			
			solver.setsS(stepSize);
			SpikePatternAdapting[] model_spike_pattern = solver.solveAndGetSpikePatternAdapting();		
			if(model_spike_pattern!=null) {	
				for(SpikePatternAdapting modelSpikePattern: model_spike_pattern){
					if(modelSpikePattern==null)
						return Float.MAX_VALUE;
					}
				}
			else
				return Float.MAX_VALUE;		
				
			double peakVolt = model_spike_pattern[0].getSpikePatternData().getPeakVoltage(stepSize,simDur,stepSize);
			double somaEpsp = (double)peakVolt - model.getvR()[0];
			
//			System.out.println(somaEpsp);
		//	model_spike_pattern[Izhikevich9pModel2CAwSyn.SOMA_IDX].getSpikePatternData().displayForPlot();
			
			error += (float) StatUtil.calculate0to1NormalizedError(expEPSPmin, expEPSPmax, somaEpsp, 0, 
															//(model.getvT()[0] - model.getvR()[0])
															5);
			if(display) {
				String displayString = "\nSynStimEpspErr(syn#"+i+").\t("+expEPSPmin+","+expEPSPmax+")\t"+
									GeneralUtils.formatTwoDecimal(somaEpsp)+"\t"+error;
				System.out.print(displayString);
			}	
		}
		
		return error;		
	}
	
	public void setSaveSP_Is_hold(boolean save) {
		this.saveSP_Is=save;
	}
	public List<Float> getSP_Is_Hold() {
		return this.SP_Is_hold;
	}

	public void addSP_Is_hold(float I) {
		if(this.SP_Is_hold==null) {
			this.SP_Is_hold=new ArrayList<Float>();
		}
		this.SP_Is_hold.add(I);
	}
}
