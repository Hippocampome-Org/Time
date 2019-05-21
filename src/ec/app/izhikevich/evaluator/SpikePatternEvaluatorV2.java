package ec.app.izhikevich.evaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import ec.app.izhikevich.evaluator.qualifier.ClassificationParameterID;
import ec.app.izhikevich.evaluator.qualifier.SolverResultsStat;
import ec.app.izhikevich.evaluator.qualifier.SpikePatternClassifier;
import ec.app.izhikevich.inputprocess.InputSpikePatternConstraint;
import ec.app.izhikevich.inputprocess.labels.BurstFeatureID;
import ec.app.izhikevich.inputprocess.labels.PatternFeatureID;
import ec.app.izhikevich.spike.BurstFeature;
import ec.app.izhikevich.spike.PatternFeature;
import ec.app.izhikevich.spike.PatternType;
import ec.app.izhikevich.spike.SpikePatternAdapting;
import ec.app.izhikevich.spike.labels.SpikePatternComponent;
import ec.app.izhikevich.starter.ECJStarterV2;
import ec.app.izhikevich.util.GeneralUtils;
import ec.app.izhikevich.util.StatUtil;

public class SpikePatternEvaluatorV2{
/*
 * Remember, all error methods here return weighted 0-1 normed errors
 */
	SpikePatternAdapting modelSpikePattern;	//This will also receive the child AdaptingSpikePatterns, therefore define sfaError here itself.
	InputSpikePatternConstraint expSpikePatternData;
	double[] patternRepairWeights;
	double modelVmin;
	double modelVr;
	double modelVt;
	double test;
	
	private boolean checkForPatternValidity;
	private boolean display;
	
	public SpikePatternClassifier patternClassifier;
	
	public static final double SFA_SCALING_FOR_EXT_REF = 50;
	
	public SpikePatternEvaluatorV2(SpikePatternAdapting modelSpikePattern, 
			InputSpikePatternConstraint expSpikePatternData, double[] patternRepWeights, 
			double modelVmin, double modelVr, double modelVt,
			boolean display,
			boolean displayOnlyClass,
			boolean externalSimUsed) {
		this.modelSpikePattern = modelSpikePattern;
		this.expSpikePatternData = expSpikePatternData;	
		this.patternRepairWeights = patternRepWeights;
		this.modelVmin = modelVmin;
		this.modelVr = modelVr;
		this.modelVt = modelVt;
		this.display = display;
		
		/*if(display){
			StatAnalyzer.display_stats = true;
		}*/
		patternClassifier = new SpikePatternClassifier(modelSpikePattern);
		if(externalSimUsed){ //HACK!!! no voltage for swa
			modelSpikePattern.setSwa(1);
		}else{
			modelSpikePattern.setSwa(measureSWA());
		}
		
		double swa = modelSpikePattern.getSwa(); //1;//
		
		patternClassifier.classifySpikePattern(swa, true);//1);//
		if(display || displayOnlyClass){
			patternClassifier.getSpikePatternClass().display();
		}
		
		if(!ECJStarterV2.TURN_OFF_CLASSIFIER){
			patternClassifier.determineWeightsForFeatures(expSpikePatternData.getSpikePatternClass());
		}else{
			patternClassifier.populateNullWeights();
		}		
	}
	
	public SpikePatternEvaluatorV2(SpikePatternAdapting modelSpikePattern, 
			InputSpikePatternConstraint expSpikePatternData, double[] patternRepWeights
			) {
		this.modelSpikePattern = modelSpikePattern;
		this.expSpikePatternData = expSpikePatternData;	
		this.patternRepairWeights = patternRepWeights;
		patternClassifier = new SpikePatternClassifier(modelSpikePattern);
		
		double swa =1;// modelSpikePattern.getSwa(); //1;//
		
		patternClassifier.classifySpikePattern(swa, true);//1);//
		
		
		if(!ECJStarterV2.TURN_OFF_CLASSIFIER){
			patternClassifier.determineWeightsForFeatures(expSpikePatternData.getSpikePatternClass());
		}else{
			patternClassifier.populateNullWeights();
		}		
	}
	
	private double measureSWA(){
		double swa = 0;
		double[] ISIs = modelSpikePattern.getISIs();
		if(ISIs==null || ISIs.length ==0) {
			return swa;
		}			
		double timeMin=modelSpikePattern.getTimeMin()+modelSpikePattern.getFSL();
		double timeMax=modelSpikePattern.getTimeMin() + modelSpikePattern.getDurationOfCurrentInjection()-5;		
		double minVolt = modelSpikePattern.getModelSpikePatternData().getMinVoltage(timeMin, timeMax, 1);
		swa= modelVmin - minVolt;
		return swa;
	}
	private void displayRoutine(PatternFeatureID featureID, PatternFeature feature, double modelValue, double error) {
		if(display) {
			String displayString = "\t"+featureID;
			if(feature.isRange()) {
				displayString += "\t("+GeneralUtils.formatTwoDecimal(feature.getValueMin())+", "
									+GeneralUtils.formatTwoDecimal(feature.getValueMax())+")";
			}else{
				displayString += "\t"+GeneralUtils.formatTwoDecimal(feature.getValue());
			}				
			displayString += "\t"+GeneralUtils.formatTwoDecimal(modelValue);
			displayString += "\t"+GeneralUtils.formatThreeDecimal(error)+"\t/";
			System.out.println(displayString);
		}
	}
	
	private void displayRoutineForRASP(PatternFeature[] exp, double[] model, 
			double error) {		
		String displayString = "RASP: ";
		
		displayString = "(RASP: )\ty=";
			
		displayString +=GeneralUtils.formatThreeDecimal(exp[0].getValue())
				+"x+"+GeneralUtils.formatThreeDecimal(exp[1].getValue());	
		displayString += "\tnISIs: "+GeneralUtils.formatThreeDecimal(exp[2].getValue());
			
			
		displayString +="\n\ty="+GeneralUtils.formatThreeDecimal(model[0])+"x+"+GeneralUtils.formatThreeDecimal(model[1]);	
		displayString += "\tnISIs: "+model[2];
		displayString += "\t"+GeneralUtils.formatThreeDecimal(error)+"\t/";
		System.out.println(displayString);
		
	}
	
	private void displayRoutineForSFA(PatternFeature[] exp, double[] model, 
			double error) {		
		String displayString = "";
		
			displayString = "\ty=";
			if(exp[0].isRange()){
				displayString += "("+GeneralUtils.formatThreeDecimal(exp[0].getValueMin())+", "+GeneralUtils.formatThreeDecimal(exp[0].getValueMax())+")"
								+"x+"+"("+GeneralUtils.formatThreeDecimal(exp[1].getValueMin())+", "+GeneralUtils.formatThreeDecimal(exp[1].getValueMax())+")";	
				displayString += "\tnISIs: "+"("+GeneralUtils.formatThreeDecimal(exp[2].getValueMin())+", "+GeneralUtils.formatThreeDecimal(exp[2].getValueMax())+")";
			}else{
				displayString +=GeneralUtils.formatThreeDecimal(exp[0].getValue())
						+"x+"+GeneralUtils.formatThreeDecimal(exp[1].getValue());	
				displayString += "\tnISIs: "+GeneralUtils.formatThreeDecimal(exp[2].getValue());
			}
				
			displayString += "\ty=";
			if(exp[3].isRange()){
				displayString += "("+GeneralUtils.formatThreeDecimal(exp[3].getValueMin())+", "+GeneralUtils.formatThreeDecimal(exp[3].getValueMax())+")"
								+"x+"+"("+GeneralUtils.formatThreeDecimal(exp[4].getValueMin())+", "+GeneralUtils.formatThreeDecimal(exp[4].getValueMax())+")";	
				displayString += "\tnISIs: "+"("+GeneralUtils.formatThreeDecimal(exp[5].getValueMin())+", "+GeneralUtils.formatThreeDecimal(exp[5].getValueMax())+")";
			}else{
				displayString +=GeneralUtils.formatThreeDecimal(exp[3].getValue())
						+"x+"+GeneralUtils.formatThreeDecimal(exp[4].getValue());	
				displayString += "\tnISIs: "+GeneralUtils.formatThreeDecimal(exp[5].getValue());
			}
			
			
			displayString +="\n\ty="+GeneralUtils.formatThreeDecimal(model[0])+"x+"+GeneralUtils.formatThreeDecimal(model[1]);	
			displayString += "\tnISIs: "+model[2];
			displayString +="\ty="+GeneralUtils.formatThreeDecimal(model[3])+"x+"+GeneralUtils.formatThreeDecimal(model[4]);	
			displayString += "\tnISIs: "+model[5];
			displayString += "\t"+GeneralUtils.formatThreeDecimal(error)+"\t/";
			System.out.println(displayString);
		
	}
	
	public float calculatePatternError() {		
		float patternError = Float.MAX_VALUE; 			
		float patternValidityErrorVmin = 0;
		float patternValidityErrorVrest =0;		
	        
	        if(modelSpikePattern == null) {   	 if(display) {System.out.println("**NULL compartment SPIKE PATTERN** SPEvaluatorV2:calculatePatternError()");  	 }
	        	return patternError;
	        }
	        //&& Math.abs(Float.MAX_VALUE - modelError)>10.0f
	        
	        boolean bypassValidityCheck=false;
	        if(expSpikePatternData.getFeaturesToEvaluate().contains(PatternFeatureID.n_spikes) 
       			 && 
       		expSpikePatternData.getFeatures().get(PatternFeatureID.n_spikes).getValue()<2){
	        	bypassValidityCheck=true;
	        }
	        if(checkForPatternValidity && !bypassValidityCheck){
		        if(expSpikePatternData.getType() == PatternType.SPIKES ){  			       
					    //if(!modelSpikePattern.isValidSpikesPattern(modelVmin, modelVr)) { if(display) {  System.out.println("**Invalid SPIKEs Pattern**");	   }					    	
		        	if(!modelSpikePattern.isValidVoltageTrace(-120, 100)) 
		        	{ if(display) {  System.out.println("**Invalid voltage ** SPEvaluatorV2:calculatePatternError()");	   }					    	
			       	return patternError;				    	
		        	}  
		        	if(!modelSpikePattern.isValidSpikesPattern()) { 
		        		if(display) {  System.out.println("**Invalid SPIKEs Pattern** SPEvaluatorV2:calculatePatternError()");	   }					    	
					       	return patternError;				    	
					    } 
					    /*else{
				//	    	if(!expSpikePatternData.getFeaturesToEvaluate().contains(PatternFeatureID.bursts))
					    	{
					    		patternValidityErrorVmin = nonBurstBelowVminError(modelVmin);
					    	}				    		
					        patternValidityErrorVrest = spikesBelowVrestError(modelVr);
					    }*/
		        	/*
		        	 * I-F fitting nspikes: temporary
		        	 */
		        	//if(!ECJStarterV2.IF_FITTING) {
		        	
		        		 if(!(modelSpikePattern.getCurrentInjected()<0)) {
		        		 if(modelSpikePattern.getISIs() == null ) { if(display) {System.out.println("**Null ISIs** SPEvaluatorV2:calculatePatternError()");      }	
			        		return patternError;
			        	}}
		        	 
		        	//}
			        			        					         	
				 }
		        	   
	        }/*else{
	        	
	        	if(modelSpikePattern.getSpikeTimes().length < 1 
	        			||
	        		(modelSpikePattern.getSpikeTimes()[modelSpikePattern.getSpikeTimes().length-1] - modelSpikePattern.getTimeMin()) > (modelSpikePattern.getDurationOfCurrentInjection()+10))  {
	    			return patternError;
	    		}	
	        	if(modelSpikePattern.getISIs() == null ) { if(display) {System.out.println("**Null ISIs**");      }	
        			return patternError;
	        	}	
	        }*/
	        //System.out.println("SPE:: patternWeighted Avg Error will..");
		        
	        patternError = patternWeightedAvgError();  
	       // System.out.println("SPE:: patternWeighted Avg Error done..");
		       		        
	        float avgPatternError;	        
	        if(expSpikePatternData.getType() == PatternType.SPIKES || expSpikePatternData.getType() == PatternType.RBASE) {
	              avgPatternError = (float) (patternRepairWeights[0]*patternError + 
	            		  patternRepairWeights[1] * patternValidityErrorVmin + 
		        		patternRepairWeights[2] * patternValidityErrorVrest);		         
	        }else {
	        	avgPatternError = patternError;	
	        }
	        if(display) {
	   	 		System.out.print("\nPatternFeatError\t"+GeneralUtils.formatThreeDecimal(patternError)+
	   	 						"\nPatternValidityErrorVmin\t"+GeneralUtils.formatThreeDecimal(patternValidityErrorVmin)+
	   	 						"\nPatternValidityErrorVrest\t"+GeneralUtils.formatThreeDecimal(patternValidityErrorVrest));
	   	 	}  
	        return avgPatternError * expSpikePatternData.getPatternWeight();
	}
	
	private float patternWeightedAvgError() {
		float weightedError = 0;
		int count=0;
		//SpikePatternEvaluator evaluator = new SpikePatternEvaluator(modelSpikePattern, expSpikePatternData, display);
		Set<PatternFeatureID> patternFeatureIDs = expSpikePatternData.getFeaturesToEvaluate();
		if(patternFeatureIDs.contains(PatternFeatureID.bursts) || 
			patternFeatureIDs.contains(PatternFeatureID.nbursts) ||
			patternFeatureIDs.contains(PatternFeatureID.stuts)){
			modelSpikePattern.initBurstPattern(2d);
		}
		for (PatternFeatureID feature: patternFeatureIDs){
			/*
			 * non spiking
			 */
			if(feature==PatternFeatureID.fsl) {
				weightedError += FSLErrorObsNormed();
				//System.out.println("SPE:: fsl Error done..");
				count++;
			}
			if(feature==PatternFeatureID.pss) {
				weightedError += PSSErrorObsNormed();
				//System.out.println("SPE:: pss Error done..");
				count++;
			}		
			
			/*
			 * regular / Adaptation
			 */
			if(feature==PatternFeatureID.n_sfa_isis1) {
				weightedError += sfaErrorObsNormed();
				//System.out.println("SPE:: n_sfa1 Error done.."+weightedError);
				count++;
			}	
			
			if(feature==PatternFeatureID.n_spikes) {
				weightedError += NSpikesErrorObsNormed();
				//System.out.println("SPE:: nspikes Error done..");
				count++;
			}			
			/*
			 * stut
			 */
			if(feature==PatternFeatureID.nbursts) {				
				weightedError += NburstsError();
				count++;
			}
			if(feature==PatternFeatureID.bursts || feature==PatternFeatureID.stuts ) {				
				weightedError += burstFeatureError();
				count++;
			}
			if(feature==PatternFeatureID.vmin_offset) {
				weightedError += vMinOffsetError(modelVmin-modelVr);
				count++;
			}
			if(feature==PatternFeatureID.rebound_VMax) {
				weightedError += reboundVmaxErrorObsNormed();
				count++;
			}
			if(feature==PatternFeatureID.swa) {
				weightedError += swaErrorObsNormed();
				count++;
			}
		}
		
		return weightedError;
	//	return (weightedError/(count*1.0f));
	}
	
	
	public double FSLError() {
	//	System.out.println("SPE:: fsl Error entry..");
		PatternFeature feature = expSpikePatternData.getFsl();
		double minError = 0;
		double maxError = expSpikePatternData.getCurrentDuration();
		double error = patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.fsl)
							*	NormalizedError(feature, modelSpikePattern.getFSL(), minError, maxError);
		displayRoutine(PatternFeatureID.fsl,feature, modelSpikePattern.getFSL(), error);
		return error;
	}
	public double reboundVmaxErrorObsNormed() {
			PatternFeature feature = expSpikePatternData.getReboundVmax();
			double timeStart = expSpikePatternData.getTimeMin() + expSpikePatternData.getCurrentDuration();
			double timeEnd = (expSpikePatternData.getTimeMin() *2 ) + expSpikePatternData.getCurrentDuration();
			double modelReboundVmax = modelSpikePattern.getSpikePatternData().getPeakVoltage(timeStart, timeEnd, 10);
			modelReboundVmax = modelReboundVmax - modelVr;
			double rBoundFeatWeight = 1;
			
			if(modelReboundVmax <= 0){
				rBoundFeatWeight += 1;
			}
			
			double error = rBoundFeatWeight
					*	NormalizedErrorObsNormed(feature, modelReboundVmax, false);
			displayRoutine(PatternFeatureID.rebound_VMax,feature, modelReboundVmax, error);
			return error;
		}
	public double FSLErrorObsNormed() {
		//	System.out.println("SPE:: fsl Error entry..");
			PatternFeature feature = expSpikePatternData.getFsl();
			//double minError = 0;
			//double maxError = expSpikePatternData.getCurrentDuration();
			double error = patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.fsl)
								*	NormalizedErrorObsNormed(feature, modelSpikePattern.getFSL(), false);
			displayRoutine(PatternFeatureID.fsl,feature, modelSpikePattern.getFSL(), error);
			return error;
		}
	public double swaErrorObsNormed() {
			PatternFeature feature = expSpikePatternData.getSwa();
			//double minError = 0;
			//double maxError = expSpikePatternData.getCurrentDuration();
			double error = patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.swa)
								*	NormalizedErrorObsNormed(feature, modelSpikePattern.getSwa(), false);
			displayRoutine(PatternFeatureID.swa,feature,modelSpikePattern.getSwa(), error);
			return error;
		}
	public double NSpikesErrorObsNormed() {
			PatternFeature feature = expSpikePatternData.getnSpikes();
			//double minError = 0;
			//double maxError = expSpikePatternData.getCurrentDuration();
			double error = //patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.n_spikes)
								//*	
					NormalizedErrorObsNormed(feature, modelSpikePattern.getNoOfSpikes(), false);
			displayRoutine(PatternFeatureID.n_spikes,feature, modelSpikePattern.getNoOfSpikes(), error);
			return error;
		}
	
	
	public double PSSErrorObsNormed() {
		PatternFeature feature = expSpikePatternData.getPss();
	//	double minError = 0;
	//	double maxError = expSpikePatternData.getCurrentDuration();
		double error = patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.pss)
				*	NormalizedErrorObsNormed(feature, modelSpikePattern.getPSS(), false);	
		displayRoutine(PatternFeatureID.pss,feature, modelSpikePattern.getPSS(), error);
		return error;
	}
	public double PSSError() {
		PatternFeature feature = expSpikePatternData.getPss();
		double minError = 0;
		double maxError = expSpikePatternData.getCurrentDuration();
		double error = patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.pss)
				*	NormalizedError(feature, modelSpikePattern.getPSS(), minError, maxError);	
		displayRoutine(PatternFeatureID.pss,feature, modelSpikePattern.getPSS(), error);
		return error;
	}
	
	public double NburstsError() {
		PatternFeature feature = expSpikePatternData.getnBursts();
		double error = patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.nbursts)
				*	NormalizedErrorObsNormed(feature, modelSpikePattern.getBurstPattern().getNBursts(), false);
		displayRoutine(PatternFeatureID.nbursts,feature, modelSpikePattern.getBurstPattern().getNBursts(), error);
		return error;
	}
	
	
	/*
	 * //classify should hve been invoked;
	 */
	
	public double sfaError() {		
		
		int nPieceWiseParmsExp = expSpikePatternData.getSpikePatternClass().getnPieceWiseParms();
		int nPieceWiseParmsModel = patternClassifier.getSpikePatternClass().getnPieceWiseParms(); 
		double possibleWeightForFutureNEAR = nPieceWiseParmsExp - nPieceWiseParmsModel; //coarse!!
		
		double error = 0;
		double minError = 0;
		double maxMerror = 1;		
		double maxCerror = 2;
		double maxNsfaError = expSpikePatternData.getCurrentDuration() * 0.3;
		
		SolverResultsStat modelStats = patternClassifier.getSolverResultsStats()[nPieceWiseParmsExp-1];
		int modelNsfaISIs1 = modelStats.getBreakPoint();
		int modelNsfaISIs2 = modelSpikePattern.getISIs().length - modelNsfaISIs1;
		double[] model = new double[] {modelStats.getM1(), modelStats.getM2(), 
				modelStats.getC1(), modelStats.getC2(),
				modelNsfaISIs1, modelNsfaISIs2
				};
		double[] exp = new double[] {expSpikePatternData.getSfaLinearM1().getValue(),				
				expSpikePatternData.getSfaLinearM2().getValue(),
				expSpikePatternData.getSfaLinearb1().getValue(),
				expSpikePatternData.getSfaLinearb2().getValue(),
				expSpikePatternData.getNSfaISIs1().getValue(),
				expSpikePatternData.getNSfaISIs2().getValue(),
				};
		
		for(int i=0;i<6; i++){
			if(i<2){//slopes
				error += StatUtil.calculate0to1NormalizedError(exp[i], 
						model[i]
								, 
						minError, 
						maxMerror
						);
			}else{
				if(i<4){//intercepts
				error += StatUtil.calculate0to1NormalizedError(exp[i], 
						model[i]
								, 
						minError, 
						maxCerror
								);
				}else{
					error += StatUtil.calculate0to1NormalizedError(exp[i], 
							model[i]
									, 
							minError, 
							maxNsfaError
									);
				}
			}			
		}		
		
		error = patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.n_sfa_isis2)
				* error;
		
		if(display) {
			displayRoutineForSFA(null,model,	error);
		}
		return error;
		
	}
	
public double sfaErrorObsNormed() {		
		
		//int nPieceWiseParmsExp = expSpikePatternData.getSpikePatternClass().getnPieceWiseParms();	    
		int nPieceWiseParmsModel = patternClassifier.getSpikePatternClass().getnPieceWiseParms(); 	
		double error = 0;
		SolverResultsStat modelStats = null;
		/*if(!(expSpikePatternData.getFeaturesToEvaluate().contains(PatternFeatureID.n_sfa_isis0) && 
				!(patternClassifier.getSpikePatternClass().contains(SpikePatternComponent.ASP)|| 
						patternClassifier.getSpikePatternClass().contains(SpikePatternComponent.NASP))
				))*/
		//part of RASP.ASP fix
		if( !(expSpikePatternData.getFeaturesToEvaluate().contains(PatternFeatureID.n_sfa_isis0) && 
			!(expSpikePatternData.getFeaturesToEvaluate().contains(PatternFeatureID.n_sfa_isis1))
					))
			//System.out.println("********?????????********");
		{   //avoid calculating error when constraint only has RASP.
			
				if(nPieceWiseParmsModel>0){ // ASP, NASP, X
					modelStats = patternClassifier.getSolverResultsStats()[nPieceWiseParmsModel-1];
				}else{ // ASP error for other comps. for example, TSWB 
					patternClassifier.calculateAdaptationForNonSP(0);
					modelStats = patternClassifier.getSolverResultsStats()[1]; // idx 1 for linear regression
				}
				
				int modelNsfaISIs1 = 1 + modelStats.getBreakPoint(); //0-based idx ; hence +1
				int modelNsfaISIs2 = modelSpikePattern.getISIs().length - modelNsfaISIs1; // remaining
				
				if(patternClassifier.getSpikePatternClass().contains(SpikePatternComponent.RASP)){
					modelNsfaISIs1 = modelStats.getBreakPoint();
					modelNsfaISIs2 = modelSpikePattern.getISIs().length - modelNsfaISIs1;
					
					modelNsfaISIs2 = modelNsfaISIs2 - 
							(int)((double)patternClassifier.getSpikePatternClass().classificationParameters.get(ClassificationParameterID.N_ISI_cut_RASP));
				}
				
				double[] model = new double[] {modelStats.getM1(), modelStats.getC1(), modelNsfaISIs1,
						modelStats.getM2(), modelStats.getC2(), modelNsfaISIs2
						};
				PatternFeature[] exp = new PatternFeature[] {expSpikePatternData.getSfaLinearM1(),					
						expSpikePatternData.getSfaLinearb1(),
						expSpikePatternData.getNSfaISIs1(),
						
						expSpikePatternData.getSfaLinearM2(),
						expSpikePatternData.getSfaLinearb2(),				
						expSpikePatternData.getNSfaISIs2()
						};
				
				//System.out.println(patternClassifier.getDynamicFeatWeightMatrix().size());
				float sfa1Weight = patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.n_sfa_isis1);
				float sfa2weight = patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.n_sfa_isis2);
				//System.out.println("******,,,,,"+sfa1Weight+"-----"+sfa2weight);
				for(int i=0;i<6; i++){
					//System.out.println(""+error);
					if(sfa1Weight>500f) {
						//this means pattern class has stut/swb for a pure SP target class
						error +=sfa1Weight;
						break;
					}
					//SFA 1
					if(i==0){//m1
						
						error += sfa1Weight * NormalizedErrorObsNormed(exp[i], model[i], true);
						//System.out.print(error+"\t");	
						continue;
					}
					if(i==1 ){//b1
						error += sfa1Weight *  NormalizedErrorObsNormed(exp[i], model[i], false);
						continue;
					}			
					if(i==2){//nsfaisis1				
						error += sfa1Weight *	 NormalizedErrorObsNormed(exp[i], model[i], false);
						continue;
					}
					// SFA 2
					if(i==3){//m2
						error += sfa2weight * NormalizedErrorObsNormed(exp[i], model[i], true);
						continue;
					}
					if(i==4 ){//b2
						error += sfa2weight *  NormalizedErrorObsNormed(exp[i], model[i], false);
						continue;
					}
					if(i==5){//nsfaisis 2
						error += sfa2weight *	 NormalizedErrorObsNormed(exp[i], model[i], false);
						continue;
					}
						
				}
				//System.out.println();
				if(display) {
					displayRoutineForSFA(exp,model,	error);
				}
		}
		if(expSpikePatternData.getFeaturesToEvaluate().contains(PatternFeatureID.n_sfa_isis0)){
			PatternFeature[] exp_rasp = new PatternFeature[] {
								expSpikePatternData.getSfaLinearM0(), 
								expSpikePatternData.getSfaLinearb0(), 
								expSpikePatternData.getNSfaISIs0()
								};
			
			double[] model_rasp = new double[] {0,0,0};
			if(patternClassifier.getSpikePatternClass().contains(SpikePatternComponent.RASP)){
				model_rasp = new double[] {
						patternClassifier.getSpikePatternClass().classificationParameters.get(ClassificationParameterID.M_RASP),
						patternClassifier.getSpikePatternClass().classificationParameters.get(ClassificationParameterID.B_RASP),
						patternClassifier.getSpikePatternClass().classificationParameters.get(ClassificationParameterID.N_ISI_cut_RASP)
						};
			}
			
			for(int i=0;i<3;i++){
				//System.out.println(""+error);
				if(i<1) {
					error += patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.n_sfa_isis0)
							*	 
						NormalizedErrorObsNormed(exp_rasp[i], model_rasp[i], true);
				}else {
					error += patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.n_sfa_isis0)
							*	 
						NormalizedErrorObsNormed(exp_rasp[i], model_rasp[i], false);
				}
				
			}	
			if(display) {
				displayRoutineForRASP(exp_rasp,model_rasp,	error);
			}
		}
		
		
		
		return error;
		
	}
	
public double sfa_m0() {
	return patternClassifier.getSpikePatternClass().classificationParameters.get(ClassificationParameterID.M_RASP);
}

public double sfa_b0() {
	return patternClassifier.getSpikePatternClass().classificationParameters.get(ClassificationParameterID.B_RASP);
}

public double sfa_nISI0() {
	return patternClassifier.getSpikePatternClass().classificationParameters.get(ClassificationParameterID.N_ISI_cut_RASP);
}
public double sfa_m1() {		
	
	//int nPieceWiseParmsExp = expSpikePatternData.getSpikePatternClass().getnPieceWiseParms();	    
	int nPieceWiseParmsModel = patternClassifier.getSpikePatternClass().getnPieceWiseParms(); 	
	SolverResultsStat modelStats = null;
	if(nPieceWiseParmsModel>0){ // ASP, NASP, X
		modelStats = patternClassifier.getSolverResultsStats()[nPieceWiseParmsModel-1];
	}else{ // ASP error for other comps. for example, TSWB 
		modelStats = patternClassifier.getSolverResultsStats()[nPieceWiseParmsModel];
	}
	
	return modelStats.getM1();	
}
public double sfa_c1() {		
	
	//int nPieceWiseParmsExp = expSpikePatternData.getSpikePatternClass().getnPieceWiseParms();	    
	int nPieceWiseParmsModel = patternClassifier.getSpikePatternClass().getnPieceWiseParms(); 	
	SolverResultsStat modelStats = null;
	if(nPieceWiseParmsModel>0){ // ASP, NASP, X
		modelStats = patternClassifier.getSolverResultsStats()[nPieceWiseParmsModel-1];
	}else{ // ASP error for other comps. for example, TSWB 
		return 0;
	}
	
	return modelStats.getC1();	
}
public double sfa_c2() {		
	
	//int nPieceWiseParmsExp = expSpikePatternData.getSpikePatternClass().getnPieceWiseParms();	    
	int nPieceWiseParmsModel = patternClassifier.getSpikePatternClass().getnPieceWiseParms(); 	
	SolverResultsStat modelStats = null;
	if(nPieceWiseParmsModel>0){ // ASP, NASP, X
		modelStats = patternClassifier.getSolverResultsStats()[nPieceWiseParmsModel-1];
	}else{ // ASP error for other comps. for example, TSWB 
		return 0;
	}
	
	return modelStats.getC2();	
}

public double sfa_m2() {	
	
	//int nPieceWiseParmsExp = expSpikePatternData.getSpikePatternClass().getnPieceWiseParms();	    
	int nPieceWiseParmsModel = patternClassifier.getSpikePatternClass().getnPieceWiseParms(); 	
	SolverResultsStat modelStats = null;
	if(nPieceWiseParmsModel>0){ // ASP, NASP, X
		modelStats = patternClassifier.getSolverResultsStats()[nPieceWiseParmsModel-1];
	}else{ // ASP error for other comps. for example, TSWB 
		return 0;
	}
	
	return modelStats.getM2();	
}


public double fsl() {
	return modelSpikePattern.getFSL();
}
public double pss() {
	return modelSpikePattern.getPSS();
}
public double nspikes() {
	return modelSpikePattern.getNoOfSpikes();
}
public int n_ISIs(int component, boolean containsRASP){
	int nPieceWiseParmsModel = patternClassifier.getSpikePatternClass().getnPieceWiseParms(); 	
	SolverResultsStat modelStats = null;
	if(nPieceWiseParmsModel>0){ // ASP, NASP, X
		modelStats = patternClassifier.getSolverResultsStats()[nPieceWiseParmsModel-1];
	}else{ // ASP error for other comps. for example, TSWB 
		return 0;
	}
	
	int modelNsfaISIs1 = 1 + modelStats.getBreakPoint(); //0-based idx ; hence +1
	int modelNsfaISIs2 = modelSpikePattern.getISIs().length - modelNsfaISIs1; // remaining	
		
	if(containsRASP){
		modelNsfaISIs1 = modelStats.getBreakPoint();
		modelNsfaISIs2 = modelSpikePattern.getISIs().length - modelNsfaISIs1;
		
		modelNsfaISIs2 = modelNsfaISIs2 - 
				(int)((double)patternClassifier.getSpikePatternClass().classificationParameters.get(ClassificationParameterID.N_ISI_cut_RASP));
	}
	
	if(component == 1)
		return modelNsfaISIs1;
	if(component == 2)
		return modelNsfaISIs2;
	return -7;
}

public double getNBursts() {
	if(modelSpikePattern.getBurstPattern()==null) {
		return 0;
	}
	return modelSpikePattern.getBurstPattern().getNBursts();
}

public double getBW(int idx) {	
	return	modelSpikePattern.getBurstPattern().getBW(idx);	
}

public double getPBI(int idx) {	
	return	modelSpikePattern.getBurstPattern().getIBI(idx);	
}

public double getBurstNspikes(int idx) {	
	return	modelSpikePattern.getBurstPattern().getNSpikes(idx);
}

public double burstFeatureError() {
		BurstFeature expBurstFeature = expSpikePatternData.getBurstFeatures();
		ArrayList<HashMap<BurstFeatureID, Double>> expBurstFeatures = expBurstFeature.getValue();
	
		
		double error = 0;
	/*	if(display) {
			System.out.print("\tBursts:");
			modelSpikePattern.getBurstPattern().displayBursts();
			System.out.println("\n");
		}*/
		for(int i=0;i<expBurstFeatures.size();i++){
			error += singleBurstError(i, expBurstFeatures.get(i));
		}
		error = expBurstFeature.getTotalWeight() * (error/(1.0f*expBurstFeatures.size()));
		
		if(display) {	
			System.out.print("\t"+GeneralUtils.formatThreeDecimal(error)+"\n");
		}
		
		return error;
	}
	
	
	private double singleBurstError(int burstIdx, HashMap<BurstFeatureID, Double> expSingleBurstFeatures){
		if(burstIdx>=modelSpikePattern.getBurstPattern().getNBursts()){
			return 1;
		}
		
		double errorBW = 0;
		double errorIBI = 0;
		double errorNspikes = 0;		
		
		//2. BW error		
		double bw = -1;
		if(expSingleBurstFeatures.containsKey(BurstFeatureID.b_w)){
			 bw = expSingleBurstFeatures.get(BurstFeatureID.b_w);
			 double modelBw = 	modelSpikePattern.getBurstPattern().getBW(burstIdx);
			 errorBW = StatUtil.calculateObsNormalizedError(bw,	modelBw	);
		}		
			
		//3. IBI error
		double pbi = -1;
		if(expSingleBurstFeatures.containsKey(BurstFeatureID.pbi)){
			 pbi = expSingleBurstFeatures.get(BurstFeatureID.pbi);	
			 double modelPbi = modelSpikePattern.getBurstPattern().getIBI(burstIdx);
			 errorIBI = StatUtil.calculateObsNormalizedError(pbi,modelPbi);		
		}		
		
		// 4. N spikes error
		double nspikes = expSingleBurstFeatures.get(BurstFeatureID.nspikes);
		errorNspikes = StatUtil.calculateObsNormalizedError(nspikes, 
				modelSpikePattern.getBurstPattern().getNSpikes(burstIdx));
		
		/*
		 * display 
		 */
		if(display) {
			System.out.print("\t");
			if(expSingleBurstFeatures.containsKey(BurstFeatureID.b_w)){
			String displayString = "\tb_w\t"+GeneralUtils.formatTwoDecimal(bw);						
			displayString += "\t"+GeneralUtils.formatTwoDecimal(modelSpikePattern.getBurstPattern().getBW(burstIdx));
			displayString += "\t"+GeneralUtils.formatThreeDecimal(errorBW)+"\t/";
			System.out.print(displayString);
			}
			
			if(expSingleBurstFeatures.containsKey(BurstFeatureID.pbi)){
				String displayString = "\tibi\t"+GeneralUtils.formatTwoDecimal(pbi);						
			displayString += "\t"+GeneralUtils.formatTwoDecimal(modelSpikePattern.getBurstPattern().getIBI(burstIdx));
			displayString += "\t"+GeneralUtils.formatThreeDecimal(errorIBI)+"\t/";
			System.out.print(displayString);
			}
			
			String displayString = "\tnspikes\t"+GeneralUtils.formatTwoDecimal(nspikes);						
			displayString += "\t"+GeneralUtils.formatTwoDecimal(modelSpikePattern.getBurstPattern().getNSpikes(burstIdx));
			displayString += "\t"+GeneralUtils.formatThreeDecimal(errorNspikes)+"\t/";
			System.out.print(displayString+"\n");
			
			
		}
		
	
		
		
		
		return (
				(expSpikePatternData.getBurstFeatures().getFeatureWeight(BurstFeatureID.b_w)  		*  	errorBW)+
				(expSpikePatternData.getBurstFeatures().getFeatureWeight(BurstFeatureID.pbi)   		*	errorIBI)+
				(expSpikePatternData.getBurstFeatures().getFeatureWeight(BurstFeatureID.nspikes)	*	errorNspikes)
				);

				
	}
	/*
	 * 
	 */

	public double vMinOffsetError(double modelVminOffset) {	
		PatternFeature feature = expSpikePatternData.getvMinOffset();
		double minError = 0;
		double maxError;
		/*
		 * the following holds only when constraining the model vminoffset within (0, expVmin)
		 * probably have to get the min gene value and calculate it!
		 */
		if(!feature.isRange()) {
			maxError= feature.getValue();
		}else{
			maxError = feature.getValueMin();
		}
		double error = feature.getWeight() * NormalizedError(feature, modelVminOffset, minError, maxError);
		displayRoutine(PatternFeatureID.vmin_offset,feature, modelVminOffset, error);
		return error;
	}
	
	
	
	private double NormalizedError(PatternFeature feature, 
			double modelValue, double minError, double maxError) {		
		if(feature.isRange()) {
			return StatUtil.calculate0to1NormalizedError(feature.getValueMin(), 
																	feature.getValueMax(), 
																	modelValue
																	, 
																	minError, 
																	maxError
																	);
		}else {
			return StatUtil.calculate0to1NormalizedError(feature.getValue(), 
					modelValue
					, 
					minError, 
					maxError
					);
		}
	}
	
	private double NormalizedErrorObsNormed(PatternFeature feature, 
			double modelValue, boolean sfa) {
		/*
		 * the w here is manual weight assigned in JSON. only for special cases!! like 3-000 to get D.ASP.
		 * shouldn't be confused with dynamic W calculated in spikepatternclassifier
		 * Typically, w is 0 in JSON, so that this manual weight is ignored!
		 */
		double w = feature.getWeight();
		double scale = 1;
		
		if(GeneralUtils.isCloseEnough(w, 0, 0.001)){
			w=1;
		}
		if(ECJStarterV2.TURN_OFF_CLASSIFIER==true) { //this overrides all!
			w=1;
		}
		if(sfa) {
			scale = SFA_SCALING_FOR_EXT_REF;
			if(w<2.5) {// to scale m and c
				w=2;
			}
		}
		
		if(feature.isRange()) {
			return w*StatUtil.calculateObsNormalizedError(scale*feature.getValueMin(), 
																	scale*feature.getValueMax(), 
																	scale*modelValue
																	);
		}else {
			return w*StatUtil.calculateObsNormalizedError(scale*feature.getValue(), 
					modelValue
					);
		}
	}
	
	/*
	 * other non-feature errors; like avoid bursting...
	 */
	private float nonBurstBelowVminError(float vMin) {	
		double avgBelowVminOffset = modelSpikePattern.calculateAvgBelowVminAfterLastSpike(vMin, 200);
		return 
				(float) StatUtil.calculateObsNormalizedError(0, avgBelowVminOffset);
	}
	private float spikesBelowVrestError(float vR) {		
		double avgBelowVrestOffset = modelSpikePattern.calculateAvgBelowVrest(vR);		
		return 
				(float) StatUtil.calculateObsNormalizedError(0, avgBelowVrestOffset);
	}
	public boolean isCheckForPatternValidity() {
		return checkForPatternValidity;
	}
	public void setCheckForPatternValidity(boolean checkForPatternValidity) {
		this.checkForPatternValidity = checkForPatternValidity;
	}
	
}
