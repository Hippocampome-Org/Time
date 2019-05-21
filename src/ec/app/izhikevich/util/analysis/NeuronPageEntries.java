package ec.app.izhikevich.util.analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import ec.app.izhikevich.ModelEvaluatorWrapper;
import ec.app.izhikevich.evaluator.ModelEvaluatorMC;
import ec.app.izhikevich.evaluator.SpikePatternEvaluatorV2;
import ec.app.izhikevich.inputprocess.InputMCConstraint;
import ec.app.izhikevich.inputprocess.InputSpikePatternConstraint;
import ec.app.izhikevich.inputprocess.PhenotypeConstraint;
import ec.app.izhikevich.inputprocess.labels.BurstFeatureID;
import ec.app.izhikevich.inputprocess.labels.PatternFeatureID;
import ec.app.izhikevich.model.neurontypes.mc.EAGenes;
import ec.app.izhikevich.outputprocess.CarlSpikePattern;
import ec.app.izhikevich.spike.labels.Phenotype;
import ec.app.izhikevich.spike.labels.SpikePatternClass;
import ec.app.izhikevich.spike.labels.SpikePatternComponent;
import ec.app.izhikevich.starter.ECJStarterV2;
import ec.app.izhikevich.util.GeneralFileWriter;
import ec.app.izhikevich.util.PyInvoker;
import ec.app.izhikevich.util.StatUtil;
import ec.app.izhikevich.util.forportal.ModelDataStructure;
import ec.app.izhikevich.util.forportal.PortalInterface;

public class NeuronPageEntries {
	private static final String MODEL_DEF_URL = "http://hippocampome.org/csv2db/Help_Model_Definition.php";
	private static final String MODEL_FIT_URL = "http://hippocampome.org/csv2db/Help_Model_Fitting.php";
	private static final String MODEL_SIM_URL = "http://hippocampome.org/csv2db/Help_Model_Simulation.php";
	
	public static final String fitFileDir="C:\\Users\\sivav\\Dropbox\\HCO\\OnPortal\\FitFiles\\";

	public static List<ModelDataStructure> mdsList_sc;
	public static List<ModelDataStructure> mdsList_mc;
	
	static void evaluate(ModelDataStructure mds, boolean displayClassMismatch, boolean displayQuant) {
		if(mds.model.isIso_comp()) {
			ECJStarterV2.iso_comp = true;
			ModelEvaluatorWrapper.ISO_COMPS = true;
			EAGenes.iso=true;
		}
		InputSpikePatternConstraint[] matchedConstraints = new InputSpikePatternConstraint[mds.property.getInputSpikePatternConstraints().length];
		mds.featErrors = new HashMap[mds.property.getInputSpikePatternConstraints().length];
		
		for(int i=0;i<mds.Is.length;i++) {
			matchedConstraints[i] = findMatchingConstraint(mds.property.getInputSpikePatternConstraints(), mds.Is[i]);			
			/*
			 * 
			 */
			if(mds.neuronSubtypeID.equals("1-041-2")&& mds.Is[i]<300)// to allow it to find mathcing constraint ecj file was changed! -- refer to light green notes in xlsx
				mds.Is[i]=602;
			
			if(matchedConstraints[i] == null) {
				System.out.println(mds.neuronSubtypeID);
			}
		}
		
		PhenotypeConstraint[] phenotypeConstraintData = null;
		double[] patternRepWeights = new double[] {1,0,0};
		InputMCConstraint[] mcConstraintData = null;
		double[] weight = null;		
		
		for(int i=0;i<mds.Is.length;i++) {
			mds.featErrors[i]=new HashMap<>();
			/*System.out.print(mds.Is[i] +
					"\t["+ matchedConstraints[i].getCurrent().getValueMin()+
					","+matchedConstraints[i].getCurrent().getValueMax()+"]\t\t");
			*/
			ModelEvaluatorMC evaluator = new ModelEvaluatorMC(mds.model, new InputSpikePatternConstraint[] {matchedConstraints[i]}, 
					phenotypeConstraintData, patternRepWeights, mcConstraintData, new double[] {mds.Is[i]}, weight);
			
			evaluator.setDisplayAll(displayQuant);			
			evaluator.setSaveModelPattern(true);
			
			float feat_count = 0;
			mds.errorSC[i] = evaluator.getFitness();	
			SpikePatternEvaluatorV2 evaluator2 = evaluator.getSpEvalHolder();
			/*
			 * fill up features here..
			 */
			mds.property.getFitFileStructure()[i] = new FitFileStructure();
			mds.property.getFitFileStructure()[i].InputCurrent = mds.Is[i];
			mds.property.getFitFileStructure()[i].currentDuration = matchedConstraints[i].getCurrentDuration();
			
			double local_rel_errors = 0;
			if(matchedConstraints[i].getFeaturesToEvaluate().contains(PatternFeatureID.fsl)) {
				//mds.property.getFitFileStructure()[i].has = true;
				mds.property.getFitFileStructure()[i].fsl[0] =  matchedConstraints[i].getFsl().getValue();
				mds.property.getFitFileStructure()[i].fsl[1] = evaluator2.fsl();
				feat_count = feat_count +1;
				local_rel_errors = StatUtil.calculateRelativeError(mds.property.getFitFileStructure()[i].fsl[0], mds.property.getFitFileStructure()[i].fsl[1]);
				
				if(local_rel_errors > 2.26) {
					System.out.println(mds.neuronSubtypeID+"\t"+mds.Is[i]);
				}
				
				mds.featErrors[i].put(PatternFeatureID.fsl, local_rel_errors);
			}			
			
			if(matchedConstraints[i].getFeaturesToEvaluate().contains(PatternFeatureID.n_spikes)) {
				mds.property.getFitFileStructure()[i].hasNSpikes = true;
				mds.property.getFitFileStructure()[i].nSpikes[0] =  (int) matchedConstraints[i].getnSpikes().getValue();
				mds.property.getFitFileStructure()[i].nSpikes[1] = (int) evaluator2.nspikes();
				feat_count = feat_count +1;
				
				local_rel_errors =  StatUtil.calculateRelativeError(mds.property.getFitFileStructure()[i].nSpikes[0], mds.property.getFitFileStructure()[i].nSpikes[1]);
				
			//	if(mds.neuronSubtypeID.equals("6-008-2")) {
			//		System.out.println(local_rel_errors/3d+"  "+i);
			//	}
				
				//mds.featErrors[i].put(PatternFeatureID.n_spikes, local_rel_errors);
				
			}
			
			if(matchedConstraints[i].getFeaturesToEvaluate().contains(PatternFeatureID.pss)) {
				mds.property.getFitFileStructure()[i].hasPSS = true;
				mds.property.getFitFileStructure()[i].pss[0] =  matchedConstraints[i].getPss().getValue();
				mds.property.getFitFileStructure()[i].pss[1] = evaluator2.pss();
				feat_count = feat_count +1;
				local_rel_errors =  StatUtil.calculateRelativeError(mds.property.getFitFileStructure()[i].pss[0],
						mds.property.getFitFileStructure()[i].pss[1]);
				
				
				mds.featErrors[i].put(PatternFeatureID.pss, local_rel_errors);
			}			
			
			if(matchedConstraints[i].getFeaturesToEvaluate().contains(PatternFeatureID.swa)) {
				feat_count = feat_count +1;
				
				
			}
				
			SpikePatternClass patternClass = evaluator2.patternClassifier.getSpikePatternClass();
			mds.property.getFitFileStructure()[i].spikePatternClass = patternClass.toString();
			
			if(matchedConstraints[i].getFeaturesToEvaluate().contains(PatternFeatureID.nbursts))  {
				mds.property.getFitFileStructure()[i].hasBURST = true;
				
				mds.property.getFitFileStructure()[i].nBursts[0] = (int) matchedConstraints[i].getnBursts().getValue();
				mds.property.getFitFileStructure()[i].nBursts[1] = (int) evaluator2.getNBursts();
				feat_count = feat_count +1;
				
				int expNbursts = mds.property.getFitFileStructure()[i].nBursts[0];
				int modelNbursts = mds.property.getFitFileStructure()[i].nBursts[1];
				
				local_rel_errors =  StatUtil.calculateRelativeError(expNbursts,
						modelNbursts);
				mds.featErrors[i].put(PatternFeatureID.nbursts, local_rel_errors);
				
				int smallerNbursts = (expNbursts<modelNbursts) ? expNbursts : modelNbursts;
				
				double loc_err_bw = 0;
				double loc_err_pbi = 0;
				double loc_err_b_nspikes = 0;
				
				for(int j=0;j<smallerNbursts;j++) {
					double bw_exp = matchedConstraints[i].getBurstFeatures().getValue().get(j).get(BurstFeatureID.b_w);
					mds.property.getFitFileStructure()[i].bw[0].add(bw_exp);
					double bw_model = evaluator2.getBW(j);
					mds.property.getFitFileStructure()[i].bw[1].add(bw_model);
					feat_count = feat_count +1;
					loc_err_bw += StatUtil.calculateRelativeError(bw_exp, bw_model);
					
					double nspikes_exp = matchedConstraints[i].getBurstFeatures().getValue().get(j).get(BurstFeatureID.nspikes);
					mds.property.getFitFileStructure()[i].burst_n_spikes[0].add(nspikes_exp);
					double nspikes_model = evaluator2.getBurstNspikes(j);
					mds.property.getFitFileStructure()[i].burst_n_spikes[1].add(nspikes_model);
					feat_count = feat_count +1;
					loc_err_b_nspikes += StatUtil.calculateRelativeError(nspikes_exp, nspikes_model);
							
					if(j!=smallerNbursts-1) {
						double pbi_exp = matchedConstraints[i].getBurstFeatures().getValue().get(j).get(BurstFeatureID.pbi);
						mds.property.getFitFileStructure()[i].pbi[0].add(pbi_exp);
						double pbi_model = evaluator2.getPBI(j);
						mds.property.getFitFileStructure()[i].pbi[1].add(pbi_model);
						feat_count = feat_count +1;
						loc_err_pbi += StatUtil.calculateRelativeError(pbi_exp, pbi_model);
					}
				}
				mds.featErrors[i].put(PatternFeatureID.b_w, loc_err_bw/(smallerNbursts*1.0d));
				mds.featErrors[i].put(PatternFeatureID.pbi, loc_err_pbi/((smallerNbursts-1)*1.0d));
				mds.featErrors[i].put(PatternFeatureID.b_nspikes, loc_err_b_nspikes/(smallerNbursts*1.0d));
			}
			
			if(matchedConstraints[i].getFeaturesToEvaluate().contains(PatternFeatureID.n_sfa_isis0)) {
				mds.property.getFitFileStructure()[i].hasRASP = true;
				
				mds.property.getFitFileStructure()[i].sfa_m0[0]=matchedConstraints[i].getSfaLinearM0().getValue();
				mds.property.getFitFileStructure()[i].sfa_m0[1]=evaluator2.sfa_m0();
				feat_count = feat_count +1;
				
				mds.property.getFitFileStructure()[i].sfa_c0[0]=matchedConstraints[i].getSfaLinearb0().getValue();
				mds.property.getFitFileStructure()[i].sfa_c0[1]=evaluator2.sfa_b0();
				feat_count = feat_count +1;
				
				mds.property.getFitFileStructure()[i].nISIs_0[0]=(int) matchedConstraints[i].getNSfaISIs0().getValue();
				mds.property.getFitFileStructure()[i].nISIs_0[1]=(int) evaluator2.sfa_nISI0();	
				feat_count = feat_count +1;
				
				local_rel_errors = StatUtil.calculateRelativeError(SpikePatternEvaluatorV2.SFA_SCALING_FOR_EXT_REF * mds.property.getFitFileStructure()[i].sfa_m0[0], 
						SpikePatternEvaluatorV2.SFA_SCALING_FOR_EXT_REF * mds.property.getFitFileStructure()[i].sfa_m0[1]);
				//local_rel_errors += StatUtil.calculateRelativeError(mds.property.getFitFileStructure()[i].sfa_c0[0], mds.property.getFitFileStructure()[i].sfa_c0[1]);
				//local_rel_errors += StatUtil.calculateRelativeError(mds.property.getFitFileStructure()[i].nISIs_0[0], mds.property.getFitFileStructure()[i].nISIs_0[1]);
			
				//StatUtil.calculateObsNormalizedError(observed, model);
				
				mds.featErrors[i].put(PatternFeatureID.n_sfa_isis0, local_rel_errors);
				
			}
			
			if(matchedConstraints[i].getFeaturesToEvaluate().contains(PatternFeatureID.n_sfa_isis1)) {				
				mds.property.getFitFileStructure()[i].hasSP = true;
				mds.property.getFitFileStructure()[i].sfa_m1[0]=matchedConstraints[i].getSfaLinearM1().getValue();
				mds.property.getFitFileStructure()[i].sfa_m1[1]=evaluator2.sfa_m1();
				feat_count = feat_count +1;
				
				mds.property.getFitFileStructure()[i].sfa_c1[0]=matchedConstraints[i].getSfaLinearb1().getValue();
				mds.property.getFitFileStructure()[i].sfa_c1[1]=evaluator2.sfa_c1();
				feat_count = feat_count +1;
				
				mds.property.getFitFileStructure()[i].sfa_m2[0]=matchedConstraints[i].getSfaLinearM2().getValue();
				mds.property.getFitFileStructure()[i].sfa_m2[1]=evaluator2.sfa_m2();
				feat_count = feat_count +1;
				
				mds.property.getFitFileStructure()[i].sfa_c2[0]=matchedConstraints[i].getSfaLinearb2().getValue();
				mds.property.getFitFileStructure()[i].sfa_c2[1]=evaluator2.sfa_c2();
				feat_count = feat_count +1;
				
				mds.property.getFitFileStructure()[i].nISIs_1[0]=(int) matchedConstraints[i].getNSfaISIs1().getValue();
				mds.property.getFitFileStructure()[i].nISIs_1[1]=evaluator2.n_ISIs(1, patternClass.contains(SpikePatternComponent.RASP));
				feat_count = feat_count +1;
				
				mds.property.getFitFileStructure()[i].nISIs_2[0]=(int) matchedConstraints[i].getNSfaISIs2().getValue();
				mds.property.getFitFileStructure()[i].nISIs_2[1]=evaluator2.n_ISIs(2, patternClass.contains(SpikePatternComponent.RASP));
				feat_count = feat_count +1;
				
				local_rel_errors = StatUtil.calculateRelativeError(SpikePatternEvaluatorV2.SFA_SCALING_FOR_EXT_REF * mds.property.getFitFileStructure()[i].sfa_m1[0], 
						SpikePatternEvaluatorV2.SFA_SCALING_FOR_EXT_REF * mds.property.getFitFileStructure()[i].sfa_m1[1]);
				
				//System.out.println(mds.property.getFitFileStructure()[i].sfa_m1[0] +" :::: " +mds.property.getFitFileStructure()[i].sfa_m1[1] +" :::: "+ local_rel_errors);
				
				//local_rel_errors += StatUtil.calculateRelativeError(mds.property.getFitFileStructure()[i].sfa_c1[0], mds.property.getFitFileStructure()[i].sfa_c1[1]);
				//local_rel_errors += StatUtil.calculateRelativeError(mds.property.getFitFileStructure()[i].nISIs_1[0], mds.property.getFitFileStructure()[i].nISIs_1[1]);
				local_rel_errors += StatUtil.calculateRelativeError(SpikePatternEvaluatorV2.SFA_SCALING_FOR_EXT_REF * mds.property.getFitFileStructure()[i].sfa_m2[0], 
						SpikePatternEvaluatorV2.SFA_SCALING_FOR_EXT_REF * mds.property.getFitFileStructure()[i].sfa_m2[1]);
				
				//local_rel_errors += StatUtil.calculateRelativeError(mds.property.getFitFileStructure()[i].sfa_c2[0], mds.property.getFitFileStructure()[i].sfa_c2[1]);
				//local_rel_errors += StatUtil.calculateRelativeError(mds.property.getFitFileStructure()[i].nISIs_2[0], mds.property.getFitFileStructure()[i].nISIs_2[1]);
				
				if(mds.property.getFitFileStructure()[i].sfa_m1[0]>0.00001 && mds.property.getFitFileStructure()[i].sfa_m1[1]<0.00001)
					local_rel_errors = 1.5*local_rel_errors;
				//System.out.println(2*local_rel_errors);

				
				mds.featErrors[i].put(PatternFeatureID.n_sfa_isis1, local_rel_errors);
				
				
				// add nspikes
				
				local_rel_errors = StatUtil.calculateRelativeError(mds.property.getFitFileStructure()[i].nISIs_1[0]+mds.property.getFitFileStructure()[i].nISIs_2[0], 
						mds.property.getFitFileStructure()[i].nISIs_1[1]+mds.property.getFitFileStructure()[i].nISIs_2[1]);
				
				mds.featErrors[i].put(PatternFeatureID.n_spikes, local_rel_errors);

			}
			
			
			/*
			 * end feature fill-ups
			 */
			if(displayClassMismatch)
			if(!matchedConstraints[i].getSpikePatternClass().toString().equals(mds.property.getFitFileStructure()[i].spikePatternClass)) {
				System.out.print("class_mismatch:\tsc: "+mds.neuronSubtypeID+"\t"+mds.name+"\t"
								+matchedConstraints[i].getCurrent().getValueMin()+","+matchedConstraints[i].getCurrent().getValueMax()+"\t"
								+matchedConstraints[i].getSpikePatternClass().toString()+"\t:\t");		
				System.out.print(mds.Is[i]+"\t"+mds.property.getFitFileStructure()[i].spikePatternClass+"\n");
			}
			
			//if(matchedConstraints[i].getSpikePatternClass().toString().equals(mds.property.getFitFileStructure()[i].spikePatternClass))
			
			
			mds.errorSC[i] = -mds.errorSC[i]/feat_count;
				mds.featCount[i] = feat_count;
				//Weighted average feature error
				mds.property.getFitFileStructure()[i].patternError = mds.errorSC[i];
			//else
			//	mds.error[i] = Float.MAX_VALUE;
		}
		
		//
		
		
	/*	for(int i=0;i<mds.Is.length;i++) {
			System.out.print(mds.Is[i]+"\t");
		}
	    System.out.print(evaluator2.patternClassifier.getSpikePatternClass().toString());
		*/
	}
	
	//should be invoked after evaluate (_SC)? 
	static void evaluate_MC(ModelDataStructure mds_mc, ModelDataStructure mds_sc, boolean displayClassMismatch, boolean displayQuant) {
		//System.out.println("evaluating MC for :"+mds_mc.neuronSubtypeID);

		InputSpikePatternConstraint[] matchedConstraints = new InputSpikePatternConstraint[mds_mc.property.getInputSpikePatternConstraints().length];
		mds_mc.featErrors = new HashMap[mds_mc.property.getInputSpikePatternConstraints().length];
		
		for(int i=0;i<mds_mc.Is.length;i++) {
			mds_mc.featErrors[i]=new HashMap<>();
			matchedConstraints[i] = findMatchingConstraint(mds_mc.property.getInputSpikePatternConstraints(), mds_mc.Is[i]);
			if(matchedConstraints[i] == null) {
				System.out.println("matching constraint not found!"+mds_mc.neuronSubtypeID);
			}
		}		
		
		double[] patternRepWeights = new double[] {1,0,0};
		InputMCConstraint[] mcConstraintData = null;
		double[] weight = null;		
		
		for(int i=0;i<mds_mc.Is.length;i++) {
			/*System.out.print(mds_mc.Is[i] +
					"\t["+ matchedConstraints[i].getCurrent().getValueMin()+
					","+matchedConstraints[i].getCurrent().getValueMax()+"]\t\t");
			*/
			ModelEvaluatorMC evaluator = null;
			if(mds_mc.getCarlOutputParser()!=null) { // ext sim
				evaluator = new ModelEvaluatorMC(mds_mc.model, 
						new InputSpikePatternConstraint[] {matchedConstraints[i]}, 
						patternRepWeights, 
						mcConstraintData, 
						new double[] {mds_mc.Is[i]}, 
						weight,
						new CarlSpikePattern[]{mds_mc.getCarlOutputParser().extractCarlSomaPatterns()[i]},
						mds_mc.getCarlOutputParser().extractCarlMcSimData());
			}else { // int sim
				evaluator = new ModelEvaluatorMC(mds_mc.model, new InputSpikePatternConstraint[] {matchedConstraints[i]}, 
						null, patternRepWeights, mcConstraintData, new double[] {mds_mc.Is[i]}, weight);
			}
			
			
			evaluator.setDisplayAll(displayQuant);			
			evaluator.setSaveModelPattern(true);
			mds_mc.errorMC[i] = evaluator.getFitness();	
			SpikePatternEvaluatorV2 evaluator2 = evaluator.getSpEvalHolder();
			
			float feat_count = 0;
			//mds_mc.errorMC[i] = -mds_mc.errorMC[i]/mds_sc.featCount[i];
			//mds_mc.featCount[i] = mds_sc.featCount[i];
			
			/*
			 * fill up features here..
			 */
			mds_mc.property.getFitFileStructure()[i] = new FitFileStructure();
			mds_mc.property.getFitFileStructure()[i].InputCurrent = mds_mc.Is[i];
			mds_mc.property.getFitFileStructure()[i].currentDuration = matchedConstraints[i].getCurrentDuration();
			
			double local_rel_errors = 0;
			if(matchedConstraints[i].getFeaturesToEvaluate().contains(PatternFeatureID.fsl)) {
				mds_mc.property.getFitFileStructure()[i].fsl[0] =  matchedConstraints[i].getFsl().getValue();
				mds_mc.property.getFitFileStructure()[i].fsl[1] = evaluator2.fsl();
				feat_count = feat_count +1;
				local_rel_errors = StatUtil.calculateRelativeError(mds_mc.property.getFitFileStructure()[i].fsl[0], mds_mc.property.getFitFileStructure()[i].fsl[1]);
				
				//StatUtil.calculateObsNormalizedError(observed, model);
				
				mds_mc.featErrors[i].put(PatternFeatureID.fsl, local_rel_errors);
			}			
			
			if(matchedConstraints[i].getFeaturesToEvaluate().contains(PatternFeatureID.n_spikes)) {
				mds_mc.property.getFitFileStructure()[i].hasNSpikes = true;
				mds_mc.property.getFitFileStructure()[i].nSpikes[0] =  (int) matchedConstraints[i].getnSpikes().getValue();
				mds_mc.property.getFitFileStructure()[i].nSpikes[1] = (int) evaluator2.nspikes();
				feat_count = feat_count +1;
				
				local_rel_errors =  StatUtil.calculateRelativeError(mds_mc.property.getFitFileStructure()[i].nSpikes[0], mds_mc.property.getFitFileStructure()[i].nSpikes[1]);
				
				//StatUtil.calculateObsNormalizedError(observed, model);
				
				//mds_mc.featErrors[i].put(PatternFeatureID.n_spikes, local_rel_errors);
				
			}
			
			if(matchedConstraints[i].getFeaturesToEvaluate().contains(PatternFeatureID.pss)) {
				mds_mc.property.getFitFileStructure()[i].hasPSS = true;
				mds_mc.property.getFitFileStructure()[i].pss[0] =  matchedConstraints[i].getPss().getValue();
				mds_mc.property.getFitFileStructure()[i].pss[1] = evaluator2.pss();
				feat_count = feat_count +1;
				local_rel_errors =  StatUtil.calculateRelativeError(mds_mc.property.getFitFileStructure()[i].pss[0],
						mds_mc.property.getFitFileStructure()[i].pss[1]);
				
				
				mds_mc.featErrors[i].put(PatternFeatureID.pss, local_rel_errors);
			}			
			
			if(matchedConstraints[i].getFeaturesToEvaluate().contains(PatternFeatureID.swa)) {
				feat_count = feat_count +1;
				
				
			}
				
			SpikePatternClass patternClass = evaluator2.patternClassifier.getSpikePatternClass();
			mds_mc.property.getFitFileStructure()[i].spikePatternClass = patternClass.toString();
			
			if(matchedConstraints[i].getFeaturesToEvaluate().contains(PatternFeatureID.nbursts))  {
				mds_mc.property.getFitFileStructure()[i].hasBURST = true;
				
				mds_mc.property.getFitFileStructure()[i].nBursts[0] = (int) matchedConstraints[i].getnBursts().getValue();
				mds_mc.property.getFitFileStructure()[i].nBursts[1] = (int) evaluator2.getNBursts();
				feat_count = feat_count +1;
				
				int expNbursts = mds_mc.property.getFitFileStructure()[i].nBursts[0];
				int modelNbursts = mds_mc.property.getFitFileStructure()[i].nBursts[1];
				
				local_rel_errors =  StatUtil.calculateRelativeError(expNbursts,
						modelNbursts);
				mds_mc.featErrors[i].put(PatternFeatureID.nbursts, local_rel_errors);
				
				int smallerNbursts = (expNbursts<modelNbursts) ? expNbursts : modelNbursts;
				
				double loc_err_bw = 0;
				double loc_err_pbi = 0;
				double loc_err_b_nspikes = 0;
				
				for(int j=0;j<smallerNbursts;j++) {
					double bw_exp = matchedConstraints[i].getBurstFeatures().getValue().get(j).get(BurstFeatureID.b_w);
					mds_mc.property.getFitFileStructure()[i].bw[0].add(bw_exp);
					double bw_model = evaluator2.getBW(j);
					mds_mc.property.getFitFileStructure()[i].bw[1].add(bw_model);
					feat_count = feat_count +1;
					loc_err_bw += StatUtil.calculateRelativeError(bw_exp, bw_model);
					
					double nspikes_exp = matchedConstraints[i].getBurstFeatures().getValue().get(j).get(BurstFeatureID.nspikes);
					mds_mc.property.getFitFileStructure()[i].burst_n_spikes[0].add(nspikes_exp);
					double nspikes_model = evaluator2.getBurstNspikes(j);
					mds_mc.property.getFitFileStructure()[i].burst_n_spikes[1].add(nspikes_model);
					feat_count = feat_count +1;
					loc_err_b_nspikes += StatUtil.calculateRelativeError(nspikes_exp, nspikes_model);
							
					if(j!=smallerNbursts-1) {
						double pbi_exp = matchedConstraints[i].getBurstFeatures().getValue().get(j).get(BurstFeatureID.pbi);
						mds_mc.property.getFitFileStructure()[i].pbi[0].add(pbi_exp);
						double pbi_model = evaluator2.getPBI(j);
						mds_mc.property.getFitFileStructure()[i].pbi[1].add(pbi_model);
						feat_count = feat_count +1;
						loc_err_pbi += StatUtil.calculateRelativeError(pbi_exp, pbi_model);
					}
				}
				
				
				mds_mc.featErrors[i].put(PatternFeatureID.b_w, loc_err_bw/(smallerNbursts*1.0d));
				mds_mc.featErrors[i].put(PatternFeatureID.pbi, loc_err_pbi/((smallerNbursts-1)*1.0d));
				mds_mc.featErrors[i].put(PatternFeatureID.b_nspikes, loc_err_b_nspikes/(smallerNbursts*1.0d));
			}
			
			if(matchedConstraints[i].getFeaturesToEvaluate().contains(PatternFeatureID.n_sfa_isis0)) {
				mds_mc.property.getFitFileStructure()[i].hasRASP = true;
				
				mds_mc.property.getFitFileStructure()[i].sfa_m0[0]=matchedConstraints[i].getSfaLinearM0().getValue();
				mds_mc.property.getFitFileStructure()[i].sfa_m0[1]=evaluator2.sfa_m0();
				feat_count = feat_count +1;
				
				mds_mc.property.getFitFileStructure()[i].sfa_c0[0]=matchedConstraints[i].getSfaLinearb0().getValue();
				mds_mc.property.getFitFileStructure()[i].sfa_c0[1]=evaluator2.sfa_b0();
				feat_count = feat_count +1;
				
				mds_mc.property.getFitFileStructure()[i].nISIs_0[0]=(int) matchedConstraints[i].getNSfaISIs0().getValue();
				mds_mc.property.getFitFileStructure()[i].nISIs_0[1]=(int) evaluator2.sfa_nISI0();	
				feat_count = feat_count +1;
				
				local_rel_errors = StatUtil.calculateRelativeError(SpikePatternEvaluatorV2.SFA_SCALING_FOR_EXT_REF * mds_mc.property.getFitFileStructure()[i].sfa_m0[0], 
						SpikePatternEvaluatorV2.SFA_SCALING_FOR_EXT_REF * mds_mc.property.getFitFileStructure()[i].sfa_m0[1]);
				//local_rel_errors += StatUtil.calculateRelativeError(mds_mc.property.getFitFileStructure()[i].sfa_c0[0], mds_mc.property.getFitFileStructure()[i].sfa_c0[1]);
				//local_rel_errors += StatUtil.calculateRelativeError(mds_mc.property.getFitFileStructure()[i].nISIs_0[0], mds_mc.property.getFitFileStructure()[i].nISIs_0[1]);
				
				//StatUtil.calculateObsNormalizedError(observed, model);
				
				mds_mc.featErrors[i].put(PatternFeatureID.n_sfa_isis0, local_rel_errors);
				
			}
			
			if(matchedConstraints[i].getFeaturesToEvaluate().contains(PatternFeatureID.n_sfa_isis1)) {				
				mds_mc.property.getFitFileStructure()[i].hasSP = true;
				mds_mc.property.getFitFileStructure()[i].sfa_m1[0]=matchedConstraints[i].getSfaLinearM1().getValue();
				mds_mc.property.getFitFileStructure()[i].sfa_m1[1]=evaluator2.sfa_m1();
				feat_count = feat_count +1;
				
				mds_mc.property.getFitFileStructure()[i].sfa_c1[0]=matchedConstraints[i].getSfaLinearb1().getValue();
				mds_mc.property.getFitFileStructure()[i].sfa_c1[1]=evaluator2.sfa_c1();
				feat_count = feat_count +1;
				
				mds_mc.property.getFitFileStructure()[i].sfa_m2[0]=matchedConstraints[i].getSfaLinearM2().getValue();
				mds_mc.property.getFitFileStructure()[i].sfa_m2[1]=evaluator2.sfa_m2();
				feat_count = feat_count +1;
				
				mds_mc.property.getFitFileStructure()[i].sfa_c2[0]=matchedConstraints[i].getSfaLinearb2().getValue();
				mds_mc.property.getFitFileStructure()[i].sfa_c2[1]=evaluator2.sfa_c2();
				feat_count = feat_count +1;
				
				mds_mc.property.getFitFileStructure()[i].nISIs_1[0]=(int) matchedConstraints[i].getNSfaISIs1().getValue();
				mds_mc.property.getFitFileStructure()[i].nISIs_1[1]=evaluator2.n_ISIs(1, patternClass.contains(SpikePatternComponent.RASP));
				feat_count = feat_count +1;
				
				mds_mc.property.getFitFileStructure()[i].nISIs_2[0]=(int) matchedConstraints[i].getNSfaISIs2().getValue();
				mds_mc.property.getFitFileStructure()[i].nISIs_2[1]=evaluator2.n_ISIs(2, patternClass.contains(SpikePatternComponent.RASP));
				feat_count = feat_count +1;
				
				local_rel_errors = StatUtil.calculateRelativeError(SpikePatternEvaluatorV2.SFA_SCALING_FOR_EXT_REF * mds_mc.property.getFitFileStructure()[i].sfa_m1[0], 
						SpikePatternEvaluatorV2.SFA_SCALING_FOR_EXT_REF * mds_mc.property.getFitFileStructure()[i].sfa_m1[1]);
				//local_rel_errors += StatUtil.calculateRelativeError(mds_mc.property.getFitFileStructure()[i].sfa_c1[0], mds_mc.property.getFitFileStructure()[i].sfa_c1[1]);
				//local_rel_errors += StatUtil.calculateRelativeError(mds_mc.property.getFitFileStructure()[i].nISIs_1[0], mds_mc.property.getFitFileStructure()[i].nISIs_1[1]);
				local_rel_errors += StatUtil.calculateRelativeError(SpikePatternEvaluatorV2.SFA_SCALING_FOR_EXT_REF * mds_mc.property.getFitFileStructure()[i].sfa_m2[0], 
						SpikePatternEvaluatorV2.SFA_SCALING_FOR_EXT_REF * mds_mc.property.getFitFileStructure()[i].sfa_m2[1]);
				//local_rel_errors += StatUtil.calculateRelativeError(mds_mc.property.getFitFileStructure()[i].sfa_c2[0], mds_mc.property.getFitFileStructure()[i].sfa_c2[1]);
				//local_rel_errors += StatUtil.calculateRelativeError(mds_mc.property.getFitFileStructure()[i].nISIs_2[0], mds_mc.property.getFitFileStructure()[i].nISIs_2[1]);
				
				mds_mc.featErrors[i].put(PatternFeatureID.n_sfa_isis1, local_rel_errors);
				
				local_rel_errors = StatUtil.calculateRelativeError(mds_mc.property.getFitFileStructure()[i].nISIs_1[0]+mds_mc.property.getFitFileStructure()[i].nISIs_2[0], 
						mds_mc.property.getFitFileStructure()[i].nISIs_1[1]+mds_mc.property.getFitFileStructure()[i].nISIs_2[1]);

				mds_mc.featErrors[i].put(PatternFeatureID.n_spikes, local_rel_errors);
			}
			
			mds_mc.errorMC[i] = -mds_mc.errorMC[i]/feat_count;
			mds_mc.featCount[i] = feat_count;
			
			if(displayClassMismatch)
				if(!matchedConstraints[i].getSpikePatternClass().toString().equals(evaluator2.patternClassifier.getSpikePatternClass().toString())) {
					System.out.print("mc: "+mds_mc.neuronSubtypeID+"\t"
									+matchedConstraints[i].getCurrent().getValueMin()+","+matchedConstraints[i].getCurrent().getValueMax()+"\t"
									+matchedConstraints[i].getSpikePatternClass().toString()+"\t");		
					System.out.print(mds_mc.Is[i]+"\t"+evaluator2.patternClassifier.getSpikePatternClass()+"\n");
				}
		}		

	}
	
	static ModelDataStructure getModelDataStructure(List<ModelDataStructure> mdsList, String neuronSubTypeID, boolean ignoreMissing) {
		ModelDataStructure mds = null;
		for(ModelDataStructure mdst: mdsList) {
			if(mdst.neuronSubtypeID.equals(neuronSubTypeID)) {
				mds=mdst;
				break;
			}
		}
		if(!ignoreMissing)
		if(mds==null) {
			System.out.println("ModelDataStructure not found for "+neuronSubTypeID);
			System.exit(-1);
		}
		return mds;
	}

	public static ModelDataStructure getModelDataStructure(List<ModelDataStructure> mdsList, String neuronSubTypeID) {
		ModelDataStructure mds = null;
		for(ModelDataStructure mdst: mdsList) {			
			if(mdst.neuronSubtypeID.equals(neuronSubTypeID)) {
				mds=mdst;
				break;
			}
		}
		if(mds==null) {
			System.out.println("ModelDataStructure not found for "+neuronSubTypeID);
			System.exit(-1);
		}
		return mds;
	}
	public static InputSpikePatternConstraint findMatchingConstraint(InputSpikePatternConstraint[] constraints, double I) {
		InputSpikePatternConstraint constraint = null;
		int testCount = 0;
		for(int i=0;i<constraints.length;i++) {
			if(I >= constraints[i].getCurrent().getValueMin() && I <= constraints[i].getCurrent().getValueMax()) {
				testCount++;
				constraint = constraints[i];
			}
		}
		if(constraint == null) {
			System.out.println("constraint not found for "+ I);
			System.exit(-1);
		}
		if(testCount>1) {
			System.out.println("test count more than 1 for "+I);
			constraint = null;
		}
		return constraint;
	}
	
	/*
	 * used for SC vs MC mapping of feature error mapping.
	 *  - because SC and MC dont always use the same ordering with input constraints
	 *  - use exp I to identify exp constraint idx
	 */
	static int findMatchingConstraintIdx(InputSpikePatternConstraint[] constraints, double I) {
		int idx = -1;
		int testCount = 0;
		for(int i=0;i<constraints.length;i++) {
			if(I >= constraints[i].getCurrent().getValueMin() && I <= constraints[i].getCurrent().getValueMax()) {
				testCount++;
				idx = i;
			}
		}
		if(testCount>1) {
			System.out.println("test count more than 1 for "+I);
		}
		return idx;
	}
	static void displayModelDataStructure(ModelDataStructure mds) {		
		System.out.print(mds.neuronSubtypeID+"\t");
		
		for(int i=0;i<mds.Is.length;i++) {
			InputSpikePatternConstraint ips = findMatchingConstraint(mds.property.getInputSpikePatternConstraints(), mds.Is[i]);
			System.out.print(ips.getCurrent().getValueMin()+","+ips.getCurrent().getValueMax()+"\t"+ips.getSpikePatternClass().toString()+"\t");
		}
		System.out.println();
	}
	static void displayAll() {
		for(ModelDataStructure mds:mdsList_sc) {
			displayModelDataStructure(mds);
			
		}			
	}
	
	static void writeJSON(ModelDataStructure mds) {
		JSONObject jObjectParent = new JSONObject();
		jObjectParent.put("model_ID", mds.neuronSubtypeID);
		for(int i=0;i<mds.Is.length;i++) {
			JSONObject jObject = mds.property.getFitFileStructure()[i].getJObject();
			jObjectParent.put("Pattern_"+(i+1),jObject);
		}	
		
		JSONObject jObjectGrandParent = new JSONObject();
		jObjectGrandParent.put(mds.name,jObjectParent);
		
		//jObjectGrandParent.put("model_definition",MODEL_DEF_URL);
		jObjectGrandParent.put("info",MODEL_FIT_URL);
		
		try {
			FileWriter fw = new FileWriter(fitFileDir+"fitfile_"+mds.neuronSubtypeID+".json");
			fw.write(jObjectGrandParent.toString(4));
			fw.flush();
			fw.close();
		} catch (IOException e) {			
			e.printStackTrace();
		}
		
	}
	
	private static void runModelAndPlot(ModelDataStructure mds) {		
		
		int[] color_code_idcs = new int[mds.Is.length];
		for(int i=0;i<color_code_idcs.length;i++) {
			color_code_idcs[i] = 2;
		}
		String doShow = "0";
		runModelAndPlot(mds, color_code_idcs, doShow);
	}
	public static void runModelAndPlot(ModelDataStructure mds, int[] color_code_idcs, String doShow) {
		//for portal
		String VOLTAGE_FILENAME_PFX = "C:\\Users\\sivav\\Dropbox\\HCO\\OnPortal\\PyPlots\\"; 
		//for manuscript
		//String VOLTAGE_FILENAME_PFX = "C:\\Users\\sivav\\Dropbox\\HCO\\Manuscript_II\\Fig1\\model_traces\\";  
		
		String opFile=VOLTAGE_FILENAME_PFX+mds.neuronSubtypeID;
		
		double modelvr = mds.model.getvR()[0];
		double modelvpk =  mds.model.getvPeak()[0];		
		int nComp = 1;
		int nScens = mds.Is.length;	
		System.out.println(nScens);
		double[] currents = mds.Is;
		
		PyInvoker invoker = new PyInvoker(opFile, nScens, nComp, modelvr, modelvpk, currents );			
		invoker.setDisplayErrorStream(true);	
		invoker.setColorCodeIdcs(color_code_idcs);
		//for ModelEvaluaorMC call...
		InputSpikePatternConstraint[] matchedConstraints = new InputSpikePatternConstraint[mds.property.getInputSpikePatternConstraints().length];
		for(int i=0;i<mds.Is.length;i++) {
			matchedConstraints[i] = findMatchingConstraint(mds.property.getInputSpikePatternConstraints(), mds.Is[i]);
			if(matchedConstraints[i] == null) {
				System.out.println(mds.neuronSubtypeID);
			}
		}
		PhenotypeConstraint[] phenotypeConstraintData = null;
		double[] patternRepWeights = new double[] {1,0,0};
		InputMCConstraint[] mcConstraintData = null;
		double[] weight = null;		
			
		
		for(int i=0;i<mds.Is.length;i++) {
			if(mds.neuronSubtypeID.equals("1-041-2")&& mds.Is[i]<300)// to allow it to find mathcing constraint ecj file was changed! -- refer to light green notes in xlsx
				mds.Is[i]=602;
			
			
			ModelEvaluatorMC evaluator = new ModelEvaluatorMC(mds.model, new InputSpikePatternConstraint[] {matchedConstraints[i]}, 
					phenotypeConstraintData, patternRepWeights, mcConstraintData, new double[] {mds.Is[i]}, weight);
			
			evaluator.setDisplayAll(false);			
			evaluator.setSaveModelPattern(true);
			
			
			float fitness = evaluator.getFitness();	
			//SpikePatternEvaluatorV2 evaluator2 = evaluator.getSpEvalHolder();
			
			SpikePatternClass _class = evaluator.getSpEvalHolder().patternClassifier.getSpikePatternClass();
			
			/*
			 * format class
			 */
			String _cls = _class.toString();
			if(_class.contains(SpikePatternComponent.X) || _class.contains(SpikePatternComponent.EMPTY)) {
				_cls = " ";
			}			
			if(mds.Is[i]<0) {
				_cls = "RBS";
			}
			invoker.addClass(_cls);

			double[]  times = evaluator.getModelSpikePatternHolder().get(0)[0].getModelSpikePatternData().getTime();
			File tempFileT = new File(opFile+"_t"+i);
			GeneralFileWriter.write(tempFileT.getAbsolutePath(), times);
			
			double[]  vs = evaluator.getModelSpikePatternHolder().get(0)[0].getModelSpikePatternData().getVoltage();				
			File tempFileV = new File(opFile+"_v"+i);				
			GeneralFileWriter.write(tempFileV.getAbsolutePath(), vs);	
		}	
		
		invoker.invokeForSC(opFile, doShow);
		
		for(int i=0;i<nScens;i++) {
			File tempFileT = new File(opFile+"_t"+i);
			tempFileT.delete();			
			
			File tempFileV = new File(opFile+"_v"+i);				
			tempFileV.delete();						
		}		
	}
	
	/*
	 * error figure A
	 */
	public static void Fig3A() throws IOException {
		
		List<String> ignoreIDs = new ArrayList<>(); // cuz of class mismatch, remove id's after fix!!
		ignoreIDs.add("2-019-1");
		ignoreIDs.add("2-013-2");
		ignoreIDs.add("4-000-4"); // sub-optimal classification
		ignoreIDs.add("1-013-1"); //only rebound
		
		String dir = "C:\\Users\\sivav\\Dropbox\\HCO\\Manuscript_II\\Fig3\\";
		
		FileWriter fw = new FileWriter(dir+"avgFeatErrorsForClasses.csv");
		
		boolean displayClassMismatch = false;		
		boolean displayQuant = false;		
		for(ModelDataStructure mds:mdsList_sc)
		{
			if(ignoreIDs.contains(mds.neuronSubtypeID)) {
				continue;
			}
			evaluate(mds, displayClassMismatch, displayQuant);				
			for(int i=0;i<mds.Is.length;i++) {
				if(mds.Is[i]<0) {// Do not write D.X for rebound constraints
					continue;
				}
				Phenotype phenType = Phenotype.getPhenotype(mds.property.getFitFileStructure()[i].spikePatternClass);
				if(phenType.equals(Phenotype.UNIDENTIFIED)) {
					continue;
				}
				fw.write(mds.neuronSubtypeID+","+mds.errorSC[i] +","+phenType.getPhenotypeName()+"\n");				
			}			
		}
		fw.close();
	}
	/*
	 * like fig3A, but the whole phenotype
	 */
	public static void Fig3A_phen() throws IOException {
		
		List<String> ignoreIDs = new ArrayList<>(); // cuz of class mismatch, remove id's after fix!!
		ignoreIDs.add("2-019-1");
		ignoreIDs.add("2-013-2");
		ignoreIDs.add("4-000-4"); // sub-optimal classification
		ignoreIDs.add("1-013-1"); //only rebound
		
		String dir = "C:\\Users\\sivav\\Dropbox\\HCO\\Manuscript_II\\Fig5\\";
		
		FileWriter fw = new FileWriter(dir+"avgFeatErrorsForClasses_phen_v2.csv");
		
		boolean displayClassMismatch = false;		
		boolean displayQuant = false;		
		for(ModelDataStructure mds:mdsList_sc)
		{
			if(ignoreIDs.contains(mds.neuronSubtypeID)) {
				continue;
			}
			evaluate(mds, displayClassMismatch, displayQuant);
			
			List<String> patternClasses = new ArrayList<>();
			float error = 0;
			float count = 0;
			for(int i=0;i<mds.Is.length;i++) {
				if(mds.Is[i]<0) {// Do not write D.X for rebound constraints
					continue;
				}				
				error = error + mds.errorSC[i];
				count = count +1;
				patternClasses.add(mds.property.getFitFileStructure()[i].spikePatternClass);	
			}
			
			Phenotype phenType = Phenotype.getPhenotype(patternClasses);
			if(phenType.equals(Phenotype.UNIDENTIFIED)) {
				continue;
			}
			fw.write(mds.neuronSubtypeID+","+ (error/count) +","+phenType.getPhenotypeName()+"\n");
		}
		fw.close();
	}
	
	/*
	 * 8 phenotypes
	 */
public static void Fig3A_phen_with_MC() throws IOException {
		
		List<String> ignoreIDs = new ArrayList<>(); // cuz of class mismatch, remove id's after fix!!
		ignoreIDs.add("2-019-1");
		ignoreIDs.add("2-013-2");
		ignoreIDs.add("4-000-4"); // sub-optimal classification
		ignoreIDs.add("1-013-1"); //only rebound
		
		
		
		
		String dir = "C:\\Users\\sivav\\Dropbox\\HCO\\Manuscript_II\\Fig5\\";
		
		FileWriter fw = new FileWriter(dir+"avgFeatErrorsForClasses_phen_with_mc_v2.csv");
		
		boolean displayClassMismatch = false;		
		boolean displayQuant = false;		
		for(ModelDataStructure mds_mc:mdsList_mc)
		{
			//System.out.println(mds_mc.neuronSubtypeID);
			if(ignoreIDs.contains(mds_mc.neuronSubtypeID)) {
				continue;
			}
			ModelDataStructure mds_sc = getModelDataStructure(mdsList_sc, mds_mc.neuronSubtypeID);
			
			evaluate(mds_sc, displayClassMismatch, displayQuant);
			//System.out.println();
			evaluate_MC(mds_mc, mds_sc, displayClassMismatch, displayQuant);
			//System.out.println();
			
			
			List<String> patternClasses = new ArrayList<>();
			float error_sc = 0;
			float error_mc = 0;
			float count = 0;			
			for(int i=0;i<mds_sc.Is.length;i++) {
				if(mds_sc.Is[i]<0) {// Do not write D.X for rebound constraints
					continue;
				}				
				error_sc = error_sc + mds_sc.errorSC[i];
				error_mc = error_mc + mds_mc.errorMC[i];
				count = count +1;
				//patternClasses.add(mds_sc.property.getFitFileStructure()[i].spikePatternClass);	
				
				InputSpikePatternConstraint matchedConstraint = findMatchingConstraint(mds_mc.property.getInputSpikePatternConstraints(), mds_mc.Is[i]);
				if(matchedConstraint == null) {
					System.out.println("matching constraint not found for "+ mds_mc.neuronSubtypeID);
				}				
				patternClasses.add(matchedConstraint.getSpikePatternClass().toString());
				
				
				
			}
			
			Phenotype phenType = Phenotype.getPhenotype(patternClasses);			
			if(phenType.equals(Phenotype.UNIDENTIFIED)) {
				System.out.print("unidentified phenotype for"+mds_mc.neuronSubtypeID+"\t"+patternClasses+"\n");
				continue;
			}
			fw.write(mds_mc.neuronSubtypeID+","+ (error_sc/count) +","+ (error_mc/count) +","+phenType.getPhenotypeName()+"\n");
		}
		fw.close();
	}
/*
 * 2 phenotype: cont vs int
 */
public static void Fig3A_phen_with_MC_v2(ModelDataStructure mds_mc) throws IOException {
	
	
	String dir = "C:\\Users\\sivav\\Dropbox\\HCO\\Manuscript_II\\Fig5\\";
		
	boolean displayClassMismatch = true;		
	boolean displayQuant = true;		
		
		ModelDataStructure mds_sc = getModelDataStructure(mdsList_sc, mds_mc.neuronSubtypeID);
		
		evaluate(mds_sc, displayClassMismatch, displayQuant);
		//System.out.println();
		evaluate_MC(mds_mc, mds_sc, displayClassMismatch, displayQuant);
		//System.out.println();
		
		
		List<String> patternClasses = new ArrayList<>();
		float error_sc = 0;
		float error_mc = 0;
		float count = 0;			
		for(int i=0;i<mds_sc.Is.length;i++) {
			if(mds_sc.Is[i]<0) {// Do not write D.X for rebound constraints
				continue;
			}				
			error_sc = error_sc + mds_sc.errorSC[i];
			error_mc = error_mc + mds_mc.errorMC[i];
			count = count +1;
			//patternClasses.add(mds_sc.property.getFitFileStructure()[i].spikePatternClass);	
			
			InputSpikePatternConstraint matchedConstraint = findMatchingConstraint(mds_mc.property.getInputSpikePatternConstraints(), mds_mc.Is[i]);
			if(matchedConstraint == null) {
				System.out.println("matching constraint not found for "+ mds_mc.neuronSubtypeID);
			}				
			patternClasses.add(matchedConstraint.getSpikePatternClass().toString());
			
			
			
		}
		
		Phenotype phenType = Phenotype.getPhenotype(patternClasses);			
		if(phenType.equals(Phenotype.UNIDENTIFIED)) {
			System.out.print("unidentified phenotype for"+mds_mc.neuronSubtypeID+"\t"+patternClasses+"\n");
			
		}
		
		if(phenType.getIdentifier()==1 || phenType.getIdentifier()==2 ) {
			System.out.print((error_sc/count) +","+ (error_mc/count)+"\n");
		}else {
			System.out.print((error_sc/count) +","+ (error_mc/count)+"\n");
		}		

	
}

public static void Fig3A_phen_with_MC_v2() throws IOException {
	
	List<String> ignoreIDs = new ArrayList<>(); // cuz of class mismatch, remove id's after fix!!
	ignoreIDs.add("2-019-1");
	ignoreIDs.add("2-013-2");
	ignoreIDs.add("4-000-4"); // sub-optimal classification
	ignoreIDs.add("1-013-1"); //only rebound
	
	
	String dir = "C:\\Users\\sivav\\Dropbox\\HCO\\Manuscript_II\\Fig5\\";
	
	FileWriter fw_cont = new FileWriter(dir+"continuous_sc_mc.csv");
	FileWriter fw_int = new FileWriter(dir+"interrupted_sc_mc.csv");
	
	boolean displayClassMismatch = false;		
	boolean displayQuant = false;		
	for(ModelDataStructure mds_mc:mdsList_mc)
	{
		//System.out.println(mds_mc.neuronSubtypeID);
		if(ignoreIDs.contains(mds_mc.neuronSubtypeID)) {
			continue;
		}
		ModelDataStructure mds_sc = getModelDataStructure(mdsList_sc, mds_mc.neuronSubtypeID);
		
		evaluate(mds_sc, displayClassMismatch, displayQuant);
		//System.out.println();
		evaluate_MC(mds_mc, mds_sc, displayClassMismatch, displayQuant);
		//System.out.println();
		
		
		List<String> patternClasses = new ArrayList<>();
		float error_sc = 0;
		float error_mc = 0;
		float count = 0;			
		for(int i=0;i<mds_sc.Is.length;i++) {
			if(mds_sc.Is[i]<0) {// Do not write D.X for rebound constraints
				continue;
			}				
			error_sc = error_sc + mds_sc.errorSC[i];
			error_mc = error_mc + mds_mc.errorMC[i];
			count = count +1;
			//patternClasses.add(mds_sc.property.getFitFileStructure()[i].spikePatternClass);	
			
			InputSpikePatternConstraint matchedConstraint = findMatchingConstraint(mds_mc.property.getInputSpikePatternConstraints(), mds_mc.Is[i]);
			if(matchedConstraint == null) {
				System.out.println("matching constraint not found for "+ mds_mc.neuronSubtypeID);
			}				
			patternClasses.add(matchedConstraint.getSpikePatternClass().toString());
			
			
			
		}
		
		Phenotype phenType = Phenotype.getPhenotype(patternClasses);			
		if(phenType.equals(Phenotype.UNIDENTIFIED)) {
			System.out.print("unidentified phenotype for"+mds_mc.neuronSubtypeID+"\t"+patternClasses+"\n");
			continue;
		}
		if(phenType.getIdentifier()==1 || phenType.getIdentifier()==2 ) {
			fw_int.write((error_sc/count) +","+ (error_mc/count)+"\n");
		}else {
			fw_cont.write((error_sc/count) +","+ (error_mc/count)+"\n");
		}		
	}
	fw_cont.close();
	fw_int.close();
}

public static void MC_SC_error_comparison() throws IOException {
	
	List<String> ignoreIDs = new ArrayList<>(); // cuz of class mismatch, remove id's after fix!!
	ignoreIDs.add("2-019-1");
	//ignoreIDs.add("2-013-2");
	ignoreIDs.add("4-078-1"); ignoreIDs.add("1-004-1"); ignoreIDs.add("2-000-4"); ignoreIDs.add("6-047-1"); ignoreIDs.add("1-000-1"); 
	ignoreIDs.add("2-001-1"); ignoreIDs.add("2-013-2"); ignoreIDs.add("6-024-1");
	
	ignoreIDs.add("4-000-4"); // sub-optimal classification
	ignoreIDs.add("1-013-1"); //only rebound
	
	
	String dir = "C:\\Users\\sivav\\Dropbox\\HCO\\Manuscript_II\\Fig4\\feature_wise_v2\\";
	
	FileWriter fw_fsl = new FileWriter(dir+"fsl_sc_mc.csv");
	FileWriter fw_pss = new FileWriter(dir+"pss_sc_mc.csv");
	FileWriter fw_sfa0 = new FileWriter(dir+"sfa0_sc_mc.csv");
	FileWriter fw_sfa1 = new FileWriter(dir+"sfa1_sc_mc.csv");
	FileWriter fw_nspikes = new FileWriter(dir+"nspikes_sc_mc.csv");
	
	FileWriter fw_nbs = new FileWriter(dir+"nbs_sc_mc.csv");
	FileWriter fw_bw = new FileWriter(dir+"bw_sc_mc.csv");
	FileWriter fw_pbi = new FileWriter(dir+"pbi_sc_mc.csv");
	FileWriter fw_b_nspikes = new FileWriter(dir+"b_nspikes_sc_mc.csv");
	
	boolean displayClassMismatch = true;		
	boolean displayQuant = false;		
	for(ModelDataStructure mds_mc:mdsList_mc)
	{
		//System.out.println(mds_mc.neuronSubtypeID);
		if(ignoreIDs.contains(mds_mc.neuronSubtypeID)) {
			continue;
		}
		ModelDataStructure mds_sc = getModelDataStructure(mdsList_sc, mds_mc.neuronSubtypeID);
		
		evaluate(mds_sc, displayClassMismatch, displayQuant);
		//System.out.println();
		evaluate_MC(mds_mc, mds_sc, displayClassMismatch, displayQuant);
		//System.out.println();
		
		
		//List<String> patternClasses = new ArrayList<>();
		float error_sc = 0;
		float error_mc = 0;
		float count = 0;			
		for(int i=0;i<mds_sc.Is.length;i++) {
			if(mds_sc.Is[i]<0) {// Do not write D.X for rebound constraints
				continue;
			}		
			if(mds_sc.neuronSubtypeID.equals("1-041-2")&& mds_sc.Is[i]==602)// change it back after evaluate() invoke. refer to evlauate and light green notes
				mds_sc.Is[i]=209;
			
			error_sc = error_sc + mds_sc.errorSC[i];
			error_mc = error_mc + mds_mc.errorMC[i];
			count = count +1;
			//patternClasses.add(mds_sc.property.getFitFileStructure()[i].spikePatternClass);	
			
			InputSpikePatternConstraint matchedConstraint = findMatchingConstraint(mds_mc.property.getInputSpikePatternConstraints(), mds_mc.Is[i]);
			if(matchedConstraint == null) {
				System.out.println("matching constraint not found for "+ mds_mc.neuronSubtypeID);
			}				
			if(matchedConstraint.getSpikePatternClass().contains(SpikePatternComponent.X) ||
					matchedConstraint.getSpikePatternClass().contains(SpikePatternComponent.EMPTY)) {
				continue;
			}
			
			//if(matchedConstraint.getFeaturesToEvaluate().contains(PatternFeatureID.n_sfa_isis0) && mds_sc.relError[i].get(PatternFeatureID.n_sfa_isis0)==null) {
			//	System.out.println(mds_sc.neuronSubtypeID);				
			//}
			/*
			 * findMatchingConstraint() for mc doesnt mean sc idx is matched too!
			 * hence separate mapping for sc idx
			 */
			int sc_idx = findMatchingConstraintIdx(mds_sc.property.getInputSpikePatternConstraints(), mds_sc.Is[i]);
			
			if(matchedConstraint.getFeaturesToEvaluate().contains(PatternFeatureID.n_spikes) && mds_sc.featErrors[sc_idx].get(PatternFeatureID.n_spikes)==null) {
				System.out.println(mds_sc.neuronSubtypeID+" "+i+" "+sc_idx);				
			}
			if(!mds_sc.neuronSubtypeID.equals(mds_mc.neuronSubtypeID)) {
				System.out.println("sc -- mc subtype IDs dont match! "+mds_sc.neuronSubtypeID+" -- "+mds_mc.neuronSubtypeID);
			}
			if(matchedConstraint.getFeaturesToEvaluate().contains(PatternFeatureID.fsl))
				fw_fsl.write(mds_sc.flatSubtypeID()+","+mds_sc.featErrors[sc_idx].get(PatternFeatureID.fsl) +","+mds_mc.featErrors[i].get(PatternFeatureID.fsl)+"\n");
			if(matchedConstraint.getFeaturesToEvaluate().contains(PatternFeatureID.pss))
				fw_pss.write(mds_sc.flatSubtypeID()+","+mds_sc.featErrors[sc_idx].get(PatternFeatureID.pss) +","+mds_mc.featErrors[i].get(PatternFeatureID.pss)+"\n");
			if(matchedConstraint.getFeaturesToEvaluate().contains(PatternFeatureID.n_sfa_isis0))
				fw_sfa0.write(mds_sc.flatSubtypeID()+","+mds_sc.featErrors[sc_idx].get(PatternFeatureID.n_sfa_isis0) +","+mds_mc.featErrors[i].get(PatternFeatureID.n_sfa_isis0)+"\n");
			if(matchedConstraint.getFeaturesToEvaluate().contains(PatternFeatureID.n_sfa_isis1)) {
				fw_sfa1.write(mds_sc.flatSubtypeID()+","+mds_sc.featErrors[sc_idx].get(PatternFeatureID.n_sfa_isis1) +","+mds_mc.featErrors[i].get(PatternFeatureID.n_sfa_isis1)+"\n");
				fw_nspikes.write(mds_sc.flatSubtypeID()+","+mds_sc.featErrors[sc_idx].get(PatternFeatureID.n_spikes) +","+mds_mc.featErrors[i].get(PatternFeatureID.n_spikes)+"\n");
			}
		//	if(matchedConstraint.getFeaturesToEvaluate().contains(PatternFeatureID.n_spikes))
		//		fw_nspikes.write(mds_sc.flatSubtypeID()+","+mds_sc.featErrors[sc_idx].get(PatternFeatureID.n_spikes) +","+mds_mc.featErrors[i].get(PatternFeatureID.n_spikes)+"\n");
			
			if(matchedConstraint.getFeaturesToEvaluate().contains(PatternFeatureID.nbursts)) {
				fw_nbs.write(mds_sc.flatSubtypeID()+","+mds_sc.featErrors[sc_idx].get(PatternFeatureID.nbursts) +","+mds_mc.featErrors[i].get(PatternFeatureID.nbursts)+"\n");
				fw_bw.write(mds_sc.flatSubtypeID()+","+mds_sc.featErrors[sc_idx].get(PatternFeatureID.b_w) +","+mds_mc.featErrors[i].get(PatternFeatureID.b_w)+"\n");
				fw_pbi.write(mds_sc.flatSubtypeID()+","+mds_sc.featErrors[sc_idx].get(PatternFeatureID.pbi) +","+mds_mc.featErrors[i].get(PatternFeatureID.pbi)+"\n");
				fw_b_nspikes.write(mds_sc.flatSubtypeID()+","+mds_sc.featErrors[sc_idx].get(PatternFeatureID.b_nspikes) +","+mds_mc.featErrors[i].get(PatternFeatureID.b_nspikes)+"\n");
			}			
		}
		
		
		
		
			
	}
	fw_fsl.close();
	fw_pss.close();
	fw_sfa0.close();
	fw_sfa1.close();
	fw_nspikes.close();
	
	fw_nbs.close();
	fw_bw.close();
	fw_pbi.close();
	fw_b_nspikes.close();
}
/*
 * 2 phenotypes: cont vs int, n_comp-wise
 */
public static void Fig3A_phen_with_MC_v3() throws IOException {
	
	List<String> ignoreIDs = new ArrayList<>(); // cuz of class mismatch, remove id's after fix!!
	ignoreIDs.add("2-019-1");
	ignoreIDs.add("2-013-2");
	ignoreIDs.add("4-000-4"); // sub-optimal classification
	ignoreIDs.add("1-013-1"); //only rebound
	
	
	String dir = "C:\\Users\\sivav\\Dropbox\\HCO\\Manuscript_II\\Fig5\\";
	
	FileWriter fw_cont_2 = new FileWriter(dir+"continuous_sc_mc_2c.csv");
	FileWriter fw_cont_3 = new FileWriter(dir+"continuous_sc_mc_3c.csv");
	FileWriter fw_cont_4 = new FileWriter(dir+"continuous_sc_mc_4c.csv");
	
	FileWriter fw_int_2 = new FileWriter(dir+"interrupted_sc_mc_2c.csv");
	FileWriter fw_int_3 = new FileWriter(dir+"interrupted_sc_mc_3c.csv");
	FileWriter fw_int_4 = new FileWriter(dir+"interrupted_sc_mc_4c.csv");
	
	boolean displayClassMismatch = false;		
	boolean displayQuant = false;		
	for(ModelDataStructure mds_mc:mdsList_mc)
	{
		//System.out.println(mds_mc.neuronSubtypeID);
		if(ignoreIDs.contains(mds_mc.neuronSubtypeID)) {
			continue;
		}
		ModelDataStructure mds_sc = getModelDataStructure(mdsList_sc, mds_mc.neuronSubtypeID);
		
		evaluate(mds_sc, displayClassMismatch, displayQuant);
		//System.out.println();
		evaluate_MC(mds_mc, mds_sc, displayClassMismatch, displayQuant);
		//System.out.println();
		
		
		List<String> patternClasses = new ArrayList<>();
		float error_sc = 0;
		float error_mc = 0;
		float count = 0;			
		for(int i=0;i<mds_sc.Is.length;i++) {
			if(mds_sc.Is[i]<0) {// Do not write D.X for rebound constraints
				continue;
			}				
			error_sc = error_sc + mds_sc.errorSC[i];
			error_mc = error_mc + mds_mc.errorMC[i];
			count = count +1;
			//patternClasses.add(mds_sc.property.getFitFileStructure()[i].spikePatternClass);	
			
			InputSpikePatternConstraint matchedConstraint = findMatchingConstraint(mds_mc.property.getInputSpikePatternConstraints(), mds_mc.Is[i]);
			if(matchedConstraint == null) {
				System.out.println("matching constraint not found for "+ mds_mc.neuronSubtypeID);
			}				
			patternClasses.add(matchedConstraint.getSpikePatternClass().toString());
			
			
			
		}
		
		Phenotype phenType = Phenotype.getPhenotype(patternClasses);			
		if(phenType.equals(Phenotype.UNIDENTIFIED)) {
			System.out.print("unidentified phenotype for"+mds_mc.neuronSubtypeID+"\t"+patternClasses+"\n");
			continue;
		}
		if(phenType.getIdentifier()==1 || phenType.getIdentifier()==2 ) {
			if(mds_mc.model.getNCompartments()==2)
				fw_int_2.write((error_sc/count) +","+ (error_mc/count)+"\n");
			if(mds_mc.model.getNCompartments()==3)
				fw_int_3.write((error_sc/count) +","+ (error_mc/count)+"\n");
			if(mds_mc.model.getNCompartments()==4)
				fw_int_4.write((error_sc/count) +","+ (error_mc/count)+"\n");
		}else {
			if(mds_mc.model.getNCompartments()==2)
				fw_cont_2.write((error_sc/count) +","+ (error_mc/count)+"\n");
			if(mds_mc.model.getNCompartments()==3)
				fw_cont_3.write((error_sc/count) +","+ (error_mc/count)+"\n");
			if(mds_mc.model.getNCompartments()==4)
				fw_cont_4.write((error_sc/count) +","+ (error_mc/count)+"\n");
		}		
	}
	fw_cont_2.close();
	fw_cont_3.close();
	fw_cont_4.close();
	fw_int_2.close();
	fw_int_3.close();
	fw_int_4.close();
}

public static void Fig1() {	
	/*
	 * FIG. 1
	 * 
	 * DG Total Molecular Layer, 
	 * CA1 Trilaminar, 						(LW:0.5)
	 * CA3 LMR-Targeting (i)-33200, 		(LW:0.5)
	 * CA1 Basket,  						(LW: grey-0.25)
	 * MEC LV Pyramidal (e)331131p 			(LW: 0.75)
	 * DG Mossy
	 */

	/*
	 * FIG.2
	 * 
	 * 	CA1 Bistratified
	 * 	CA2 Basket
		EC LV Deep Pyramidal
		CA1 Pyramidal
		DG Hilar Ectopic Granule
		DG Granule
	 *	
	 */
	int plot_idx = 10;
	
	
	String[] subtypeIDs = {"1-004-1", "4-035-1", "2-005-1", "4-078-1", "6-002-1", "6-052-1", //fig 1
							"4-080-1", "3-006-1", "6-021-3", "4-000-3", "1-041-2", "1-000-3"}; //fig 2
	//1-002-1 - removed from fig 1 (D.NASP
	//colors code order in python module: 0-black, 1-grey, 2-red, 3-blue
	int[][] colorCodeIdces = new int[][] {
		{0,2,0,1}, {0,2,1}, {0}, {2,1}, {2,1}, {2,1},
		{2,1}, {2,1}, {0}, {0}, {2,1}, {2,0}
	};
	
	//for(plot_idx=0;plot_idx<subtypeIDs.length;plot_idx++ ) 
	{
		plot_idx = 5;
		System.out.print(subtypeIDs[plot_idx]+"\t");
		ModelDataStructure mds = getModelDataStructure(mdsList_sc, subtypeIDs[plot_idx]);
		evaluate(mds, true, true);	
		
		for(int i=0;i<mds.errorSC.length;i++) {
			System.out.print(mds.errorSC[i]+"\t");
		}
		System.out.println();
		runModelAndPlot(mds, colorCodeIdces[plot_idx], "1" );
	}
	
	
}

public static void Fig_Gen(String subtypeID) {	
		
		System.out.print(subtypeID+"\t");
		ModelDataStructure mds = getModelDataStructure(mdsList_sc, subtypeID);
		evaluate(mds, true, true);	
		
		for(int i=0;i<mds.errorSC.length;i++) {
			System.out.print(mds.errorSC[i]+"\t");
		}
		System.out.println();
		runModelAndPlot(mds, new int[] {2,0}, "1" );
	
}

public static void generateCARLparmFile(ModelDataStructure mds_sc, ModelDataStructure mds_mc) {
	String carl_parm_dir = "C:\\Users\\sivav\\Dropbox\\HCO\\OnPortal\\carl_parms\\"; 
	String filler = "";
	String filename = "";
	
	if(mds_mc!=null) {
		filler = "these models";
		filename = carl_parm_dir+mds_mc.neuronSubtypeID+"."+mds_mc.getMcLayoutCode();
	}else {
		filename = carl_parm_dir+mds_sc.neuronSubtypeID+".1c";
		filler = "this model";	
	}
	
	
	try {
		
		FileWriter fw = new FileWriter(filename);
		fw.write("#\n");
		fw.write("#\tNeuron type:\t\t"+mds_sc.name+"\n");		
		fw.write("#\tModel ID:\t\t\t"+mds_sc.neuronSubtypeID+"\n");
		fw.write("#\tplease find the instructions to run " + filler+" at "+MODEL_SIM_URL+"\n");
		fw.write("#\n");
		/*
		 * SC
		 */
		fw.write(mds_sc.model.getK()[0]+",");
		fw.write(mds_sc.model.getA()[0]+",");
		fw.write(mds_sc.model.getB()[0]+",");
		fw.write(mds_sc.model.getD()[0]+",");
		fw.write(mds_sc.model.getcM()[0]+",");
		fw.write(mds_sc.model.getvR()[0]+",");
		
		fw.write((mds_sc.model.getvT()[0] - mds_sc.model.getvR()[0]) +",");
		fw.write((mds_sc.model.getvMin()[0] - mds_sc.model.getvR()[0])+",");
		fw.write((mds_sc.model.getvPeak()[0] - mds_sc.model.getvR()[0])+",");
		
		for(int i=0;i<mds_sc.Is.length;i++) 
			fw.write(mds_sc.Is[i]+",");
		
		for(int i=0;i<mds_sc.Idurs.length;i++) {
			if(i!=mds_sc.Idurs.length-1) 
				fw.write(mds_sc.Idurs[i]+",");
			else
				fw.write(mds_sc.Idurs[i]+"\n");
		}
		
		
		/*
		 * MC
		 */
		if(mds_mc!=null) {
			for(int i=0;i<mds_mc.model.getK().length;i++) 
				fw.write(mds_mc.model.getK()[i]+",");
			for(int i=0;i<mds_mc.model.getA().length;i++) 
				fw.write(mds_mc.model.getA()[i]+",");
			for(int i=0;i<mds_mc.model.getB().length;i++) 
				fw.write(mds_mc.model.getB()[i]+",");
			for(int i=0;i<mds_mc.model.getD().length;i++) 
				fw.write(mds_mc.model.getD()[i]+",");
			for(int i=0;i<mds_mc.model.getcM().length;i++) 
				fw.write(mds_mc.model.getcM()[i]+",");
			
				fw.write(mds_mc.model.getvR()[0]+",");
				
			for(int i=0;i<mds_mc.model.getvT().length;i++) 
				fw.write((mds_mc.model.getvT()[i] - mds_mc.model.getvR()[0]) +",");
			for(int i=0;i<mds_mc.model.getvMin().length;i++) 
				fw.write((mds_mc.model.getvMin()[i] - mds_mc.model.getvR()[0])+",");
			
			for(int i=0;i<mds_mc.model.getvPeak().length;i++) {
				if(i==0) {
					fw.write((mds_mc.model.getvPeak()[0] - mds_mc.model.getvR()[0])+",");
				}else {
					fw.write((mds_mc.model.getvPeak()[0] - mds_mc.model.getvPeak()[i])+",");
				}
				
			}
				
			
			for(int i=0;i<mds_mc.model.getG().length;i++) 
				fw.write(mds_mc.model.getG()[i]+",");
			
			for(int i=0;i<mds_mc.model.getP().length;i++) 
				fw.write(mds_mc.model.getP()[i]+",");
			
			for(int i=0;i<mds_mc.model.getNCompartments()-1;i++)
				fw.write("0.00"+","); //dummy syn weights
			
			for(int i=0;i<mds_mc.Is.length;i++) 
				fw.write(mds_mc.Is[i]+",");
			
			for(int i=0;i<mds_mc.Idurs.length;i++) {
				if(i!=mds_mc.Idurs.length-1) 
					fw.write(mds_mc.Idurs[i]+",");
				else
					fw.write(mds_mc.Idurs[i]+"\n");
			}			
		}
		
		fw.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	
}

public static void generateXPPparmFile(ModelDataStructure mds_sc) {
	String carl_parm_dir = "C:\\Users\\sivav\\Dropbox\\HCO\\OnPortal\\xppFiles\\"; 
	String filler = "";
	String filename = carl_parm_dir+mds_sc.neuronSubtypeID+".ode";
	
	
	try {
		
		FileWriter fw = new FileWriter(filename);
		fw.write("#\n");
		fw.write("#\tNeuron type:\t\t\t\t"+mds_sc.name+"\n");		
		fw.write("#\tModel ID:\t\t\t\t\t"+mds_sc.neuronSubtypeID+"\n");
		fw.write("#\tDownloaded from:\t\t\thttp://hippocampome.org/\n");
		fw.write("#\tThis script runs on XPP:\thttp://www.math.pitt.edu/~bard/xpp/xpp.html\n");
		fw.write("#\n\n");
		
		fw.write("# model parameters\n");
		fw.write("par k="+mds_sc.model.getK()[0]+"\n");
		fw.write("par a="+mds_sc.model.getA()[0]+"\n");
		fw.write("par b="+mds_sc.model.getB()[0]+"\n");
		fw.write("par d="+mds_sc.model.getD()[0]+"\n");
		fw.write("par Cm="+mds_sc.model.getcM()[0]+"\n");
		fw.write("par vr="+mds_sc.model.getvR()[0]+"\n");
		
		fw.write("par vt="+mds_sc.model.getvT()[0]+"\n");
		fw.write("par vmin="+mds_sc.model.getvMin()[0]+"\n");
		fw.write("par vpeak="+mds_sc.model.getvPeak()[0]+"\n\n");
		
		fw.write("# input current\n");
		fw.write("par I="+mds_sc.Is[0]+"\n\n");
		
		fw.write("# model definition\n");
		fw.write("global 1 {v-vpeak}{v=vmin;u=u+d}\n");
		fw.write("v'=(k*(v-vr)*(v-vt)-u+I)/Cm\n");
		fw.write("u'=a*(b*(v-vr)-u)\n\n");
		
		fw.write("# initial condition\n");
		fw.write("v(0)="+mds_sc.model.getvR()[0]+"\n");
		fw.write("u(0)=0\n\n");		
		
		fw.write("# plotting variables\n");
		fw.write("@ xp=t, yp=v, xlo=0, xhi="+mds_sc.Idurs[0]+", ylo="+(mds_sc.model.getvR()[0]-10)+", yhi="+(mds_sc.model.getvPeak()[0]+10)+"\n\n");
		
		fw.write("# integration parameters\n");
		fw.write("@ meth=rk,dt=0.1, total="+mds_sc.Idurs[0]+", Bounds=1000\n");
		fw.write("\ndone");
		fw.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	
}
public static void main(String[] args) {
		String fileName = "C:\\Users\\sivav\\Dropbox\\HCO\\MCProgress_v3_15_18.xlsx";
		System.out.println("*********\nReading...");
		mdsList_sc = PortalInterface.readFromProgressSheet(fileName, 1);
		System.out.println("SC reading complete!");
		mdsList_mc = PortalInterface.readFromProgressSheet_mc(fileName, 0);		//
		System.out.println("*********\nMC Reading complete!");
	
		//ECJStarterV2.TURN_OFF_CLASSIFIER = true;		
		//Fig1();
		//System.exit(0);		
		//mdsList_sc = Analysis.removeDuplicates(mdsList_sc);
		//mdsList_mc = Analysis.removeDuplicates(mdsList_mc);		
		/*
		 * Man figures
		 */
		//Fig_Gen(neuronSubtypeID);
		
		
		/*
		 * Run one model
		 */
		
	/*	
		String neuronSubtypeID ="2-001-1";//"6-007-1";//"4-079-1";//"1-004-1";//"6-008-2";//"1-041-2";
		ModelDataStructure mds_scc = getModelDataStructure(mdsList_sc, neuronSubtypeID);
		//ModelDataStructure mds_mc = getModelDataStructure(mdsList_mc, neuronSubtypeID);
		
		
		//evaluate(mds_scc, true, false); 
		//writeJSON(mds_scc); //fitfile
		//runModelAndPlot(mds_scc);
		ModelDataStructure mds_mcc = getModelDataStructure(mdsList_mc, mds_scc.neuronSubtypeID, true);
		generateCARLparmFile(mds_scc, mds_mcc);//CARL parms file for mc			
		generateXPPparmFile(mds_scc);
		
		System.exit(0);
		*/
		
		/*
		 * Run all at once!
		 */
	for(ModelDataStructure mds_sc: mdsList_sc)
		{
			evaluate(mds_sc, true, false); //fitfile
			writeJSON(mds_sc); //fitfile
			runModelAndPlot(mds_sc); //pyplots
			
			ModelDataStructure mds_mc = getModelDataStructure(mdsList_mc, mds_sc.neuronSubtypeID, true);
			generateCARLparmFile(mds_sc, mds_mc);//CARL parms file for mc		
			
			generateXPPparmFile(mds_sc);
		}			
		System.exit(0);
		/*
		try {
			ModelEvaluatorMC.EVAL_MC_FOR_MC = false;
			
			//evaluate_MC(mds_mc,mds_sc,  false);
			//
			
			//Fig3A_phen_with_MC_v2(mds_mc);
			
			MC_SC_error_comparison();
			//Fig3A_phen();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}

}
