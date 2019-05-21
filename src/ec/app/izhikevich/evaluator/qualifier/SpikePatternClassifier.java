package ec.app.izhikevich.evaluator.qualifier;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import ec.app.izhikevich.inputprocess.labels.PatternFeatureID;
import ec.app.izhikevich.spike.BurstPattern;
import ec.app.izhikevich.spike.SpikePatternAdapting;
import ec.app.izhikevich.spike.labels.SpikePatternClass;
import ec.app.izhikevich.spike.labels.SpikePatternComponent;
import ec.app.izhikevich.util.GeneralUtils;
import ec.app.izhikevich.util.StatUtil;

public class SpikePatternClassifier {
	public static boolean DISABLED = true;
	public static float SHADOW_FITNESS = 1;
	
	private SpikePatternAdapting somaSpikePattern;
//	private float fitness;	
	private SpikePatternClass patternClass;
	private SolverResultsStat[] solverStats;
	private Map<PatternFeatureID, Float> dynamicFeatWeightMatrix;
	
	private static final double DELAY_FACTOR = 2d;
	private static final double SLN_FACTOR = 2d;
	
	private static final double PSTUT_PRE_FACTOR = 2.5d;
	private static final double PSTUT_POST_FACTOR = 2d;
	private static final double PSTUT_FACTOR = 5d;
	
	private static final double SWA_FACTOR = 5d;
	private static final double TSTUT_PRE_FACTOR = 2.5d;
	private static final double TSTUT_POST_FACTOR = 1.5;//1.019d;
	private static final double MIN_TSTUT_FREQ = 25;
	
	private static final int TSTUT_ISI_CNT = 4;
	
	private static final double FAST_TRANSIENT_FACTOR = 1.5d;//2d;
	private static final int FAST_TRANS_ISI_CNT = 3;
	
	private static final double ADAPT_IDX = 0.28d;
	//private static final double ADAPT_IDX_FOR_SLOPE = 0.0d;
	
	private static final double SLOPE_THRESHOLD = 0.003d;
	private static final double SLOPE_THRESHOLD_FT = 0.2d;
	/* The following defines whether sfa1 (1) or sfa2 (2) or both (1,2) or none (0) 
	 * should be given high weight under different scenarios.
	 * 
	 * Observed / target	X (0parm)		NASP 	ASP		ASP.NASP	ASP.ASP	
				X (0 parm)		0			1		1		1,2			1,2			
				NASP			1			0		1		1,2			1,2
				ASP				1			1		0		2			2
				ASP.NASP		1,2			1,2		2		0			2
				ASP.ASP			1,2			1,2		2		2			0						
	 */
	private static final int[][] SFA_WEIGHT_DET_SCENARIOS = new int[][] {	{0,1,1,3,3},
																			{1,0,1,3,3},
																			{1,1,0,2,2},
																			{3,3,2,0,2},
																			{3,3,2,2,0},
																			{3,3,3,3,3}
																		  };
	
	/*public SpikePatternClassifier(float _fitness, SpikePatternAdapting _somaPattern){
		this.fitness = _fitness;
		this.somaSpikePattern = _somaPattern;
		this.patternClass = null;
	}*/
	public SpikePatternClassifier(SpikePatternAdapting _somaPattern){
		this();
		this.somaSpikePattern = _somaPattern;
		dynamicFeatWeightMatrix = new HashMap<>();
	}
	/*
	 * for experimental trace classifier
	 */
	public SpikePatternClassifier(){
		somaSpikePattern = null;
		patternClass = new SpikePatternClass();
		SolverResultsStat init = new SolverResultsStat(0, 0, 0, 0, new double[]{}, 0);
		solverStats = new SolverResultsStat[] {init,init,init,init};
	}
	
	public void classifySpikePattern(double swa, boolean isModel){	
		classifySpikePattern_EXP(swa, isModel);
	//	classifySpikePattern_OLD(0);
	}
	
	/*
	 *              tstut, pstut checks after NASP
	 */
	public void classifySpikePattern_EXP(double swa, boolean isModel){
		/*
		 * 0. Check for valid ISIs
		 */	
		if(somaSpikePattern.getISIs()==null){
			patternClass.addComponent(SpikePatternComponent.EMPTY);
			return;
		}	
		/*
		 * I. check for Delay
		 *   if no isis OR fsl > criterion, it's a delay
		 */
		if(somaSpikePattern.getISIs().length == 0){		
			patternClass.addComponent(SpikePatternComponent.EMPTY);
			return;
		}
		if(hasDelay()){
			patternClass.addComponent(SpikePatternComponent.D);
		}	
		
		
		/*
		 * II. Check TSTUT
		 */
		int startIdxForTstut = 0;
		if(somaSpikePattern.getISIsStartingFromIdx(startIdxForTstut).length >= 1){			
			startIdxForTstut = startIdxForTSTUT(swa);
			if(startIdxForTstut>1){
				if(swa>SWA_FACTOR){
					patternClass.addComponent(SpikePatternComponent.TSWB);
				}else
					patternClass.addComponent(SpikePatternComponent.TSTUT);						
			}			
		}
		
		/*
		 * NEW: FAST TRANSIENT ADDITION!!
		 *    //  - tstut not satisfied!, make sure you are not missing the fast adaptation!
		 */
		
	
		if(somaSpikePattern.getISIsStartingFromIdx(startIdxForTstut)!=null){
			if(somaSpikePattern.getISIsStartingFromIdx(startIdxForTstut).length == 1){
				patternClass.addComponent(SpikePatternComponent.X);
				/*
				 * SP-SR (i)-0302	TSTUT.	TSTUT.X.
					CA1 Neurogliaform (i) 3000	D.	D.X.
				 */
			}
			if(somaSpikePattern.getISIsStartingFromIdx(startIdxForTstut).length > 1){
				/*
				 * III. Classify - Stat Tests
				 */
				classifySpikes_AK(startIdxForTstut, isModel);			
				if(!patternClass.contains(SpikePatternComponent.ASP) 
						|| isModel){
					
					/*
					 * Check PSTUT
					 */
					int startIdxForPSTUT = 0;
					if(startIdxForTstut>0){
						startIdxForPSTUT = startIdxForTstut-1;
					}
					if(hasPSTUT(startIdxForPSTUT))
					//if(hasSTUT(startIdxForPSTUT))	
					{
						patternClass.removeLastAddedComponent(); //remove NASP
						if(patternClass.contains(SpikePatternComponent.TSTUT) ||
								patternClass.contains(SpikePatternComponent.TSWB)){
							patternClass.removeLastAddedComponent();//remove TSTUT
						}
						//if(isPSTUT())
						{
							if(swa>SWA_FACTOR){
								patternClass.addComponent(SpikePatternComponent.PSWB);
							}else
								patternClass.addComponent(SpikePatternComponent.PSTUT);
						}
						//else
						{
							//patternClass.addComponent(SpikePatternComponent.INT);
						}								
					}
					
					/*
					 * if no TSTUT, detect Fast transient and reclassify!
					 */
					if((!isModel || !patternClass.contains(SpikePatternComponent.ASP))&&
						!patternClass.contains(SpikePatternComponent.TSTUT) && 
							!patternClass.contains(SpikePatternComponent.TSWB)&&
								!patternClass.contains(SpikePatternComponent.PSTUT) && 
									!patternClass.contains(SpikePatternComponent.PSWB)&&										
									(somaSpikePattern.getISIsStartingFromIdx(startIdxForTstut).length >= FAST_TRANS_ISI_CNT)){			
						
						int fastTransIdx =classifyFastTransientStat(FAST_TRANS_ISI_CNT);// classifyFastTransientSimple();		//
						if(fastTransIdx>0 && somaSpikePattern.getISIsStartingFromIdx(fastTransIdx)!=null){								
							
							if(somaSpikePattern.getISIsStartingFromIdx(fastTransIdx-1).length > 2){
								classifySpikes_AK(fastTransIdx-1, isModel);
							}
						}							
					}					
				}
			}
		}
		
		/*
		 * III. check for SLN 
		 */
		if(hasPostSLN()){
			if(patternClass.steadyStateReached()){
				if(swa > SWA_FACTOR)
					patternClass.replaceWithPSWB();
				else
					patternClass.replaceWithPSTUT();
			}else{
				patternClass.addComponent(SpikePatternComponent.SLN);
			}			
		}
	}
	private void classifySpikePattern_OLD(double swa){
		/*
		 * 0. Check for valid ISIs
		 */	
		if(somaSpikePattern.getISIs()==null){
			patternClass.addComponent(SpikePatternComponent.D);
			return;
		}	
		/*
		 * I. check for Delay
		 *   if no isis OR fsl > criterion, it's a delay
		 */
		if(somaSpikePattern.getISIs().length == 0){		
			patternClass.addComponent(SpikePatternComponent.D);
			return;
		}
		if(hasDelay()){
			patternClass.addComponent(SpikePatternComponent.D);
		}
		
		/*
		 * II. TSTUT
		 * 
		 */
		/*int startIdxForTstut = startIdxForTSTUT();
		if(startIdxForTstut > 1){ //tstut present, 1 might not be possible, it's just 0, if no tstut!
			if(swa>SWA_FACTOR){
				patternClass.addComponent(SpikePatternComponent.TSWB);
			}else
				patternClass.addComponent(SpikePatternComponent.TSTUT);
		}
		
		if(somaSpikePattern.getISIs(startIdxForTstut)==null){			
			return;
		}
		if(somaSpikePattern.getISIs(startIdxForTstut).length == 0){	
			return;
		}*/
		
		if(somaSpikePattern.getISIs().length == 1){			
			patternClass.addComponent(SpikePatternComponent.X);
		}
		
		if(somaSpikePattern.getISIs().length > 1){			
				classifySpikes(0);						
		}
		/*
		 * III. check for SLN 
		 */
		if(hasPostSLN()){
			patternClass.addComponent(SpikePatternComponent.SLN);
		}
	}
	
	private int startIdxForTSTUT(double swa) {
		int startIdx = 0;
		double[] isis = somaSpikePattern.getISIs();
		double[] isisAndPss = new double[isis.length+1];
		for(int i=0;i<isis.length;i++){
			isisAndPss[i]=isis[i];
		}
		isisAndPss[isisAndPss.length-1] = somaSpikePattern.getPSS();
		int maxIdxForCheck = ((TSTUT_ISI_CNT>(isisAndPss.length-2))? (isisAndPss.length-2) : TSTUT_ISI_CNT);
		
		/*
		 * to avoid X.SLN, if only one ISI present, ignore 
		 */
		if(maxIdxForCheck == 0){
			if(isisAndPss[1]>isisAndPss[0]*TSTUT_PRE_FACTOR){
				double[] isisPre = GeneralUtils.getFirstNelements(isisAndPss, 1);
				double[] isisPost = GeneralUtils.getLastNelements(isisAndPss, 1);
				if(StatUtil.calculateMean(isisPost) > StatUtil.calculateMean(isisPre)*TSTUT_PRE_FACTOR &&
						somaSpikePattern.getFiringFrequencyBasedOnISIs(1)>MIN_TSTUT_FREQ){
					startIdx = 2;	
					return startIdx;
			}
		}
		}
		/*
		 * another sneak-in for tswb.sln (with 2 ISIs)-- without swa this would NOT be a tstut.
		 */
		if(swa>=SWA_FACTOR){
			if(isisAndPss.length<=5){
				if(isisAndPss[isisAndPss.length-1] > isisAndPss[isisAndPss.length-2]*TSTUT_PRE_FACTOR){
					return isisAndPss.length-1;
				}
			}
		}
		for(int i=1;i<=maxIdxForCheck;i++){
			if(isisAndPss[i]>isisAndPss[i-1]*TSTUT_PRE_FACTOR
					&& (isisAndPss[i] > isisAndPss[i+1]*TSTUT_POST_FACTOR 
							|| 
							swa >= SWA_FACTOR
						)
				){
				double[] isisPre = GeneralUtils.getFirstNelements(isisAndPss, i);
				double[] isisPost = GeneralUtils.getLastNelements(isisAndPss, i);
				if(StatUtil.calculateMean(isisPost) > StatUtil.calculateMean(isisPre)*TSTUT_PRE_FACTOR &&
						somaSpikePattern.getFiringFrequencyBasedOnISIs(i)>MIN_TSTUT_FREQ){
					startIdx = i+1;
					break;
				}				
			}
		}
		return startIdx;
	}
	
	public void determineWeightsForFeatures(SpikePatternClass targetClass){		
		/*
		 * 4 weights are determined here, depending on the cases	
		 */
				
		dynamicFeatWeightMatrix.put(PatternFeatureID.fsl, delayWeight(targetClass));
	
		addAdaptationWeight(targetClass);					
		
		
		dynamicFeatWeightMatrix.put(PatternFeatureID.pss, postSlnWeight(targetClass));
		
		
		dynamicFeatWeightMatrix.put(PatternFeatureID.nbursts, stutWeight(targetClass));
		
		//only used for rebound spikes;
		dynamicFeatWeightMatrix.put(PatternFeatureID.n_spikes, 1f);		
		
		dynamicFeatWeightMatrix.put(PatternFeatureID.swa, 0.001f);	
	}
	
	public void populateNullWeights(){
		dynamicFeatWeightMatrix.put(PatternFeatureID.fsl, 1f);	
		dynamicFeatWeightMatrix.put(PatternFeatureID.n_sfa_isis0, 1f);
		dynamicFeatWeightMatrix.put(PatternFeatureID.n_sfa_isis1, 1f);
		dynamicFeatWeightMatrix.put(PatternFeatureID.n_sfa_isis2, 1f);			
		dynamicFeatWeightMatrix.put(PatternFeatureID.pss,1f);		
		dynamicFeatWeightMatrix.put(PatternFeatureID.nbursts, 1f);		
		//only used for rebound spikes;
		dynamicFeatWeightMatrix.put(PatternFeatureID.n_spikes, 1f);			
		dynamicFeatWeightMatrix.put(PatternFeatureID.swa, 1f);	
	}
	/*
	 * Delay > 2*ISIavg(1,2)
	 */
	private boolean hasDelay(){
		double isiFilt=somaSpikePattern.getISI0();
		//System.out.println(isiFilt);	
		if(somaSpikePattern.getISIs().length>1){
			isiFilt = (somaSpikePattern.getISI0()+somaSpikePattern.getISI(1))/2d;
		}		
		//System.out.println(isiFilt);
		if(somaSpikePattern.getFSL() > DELAY_FACTOR*isiFilt){					
			return true;
		}else
			return false;
	}
	
	public double getDelayFactor() {
		double isiFilt=somaSpikePattern.getISI0();
		if(somaSpikePattern.getISIs().length>1){
			isiFilt = (somaSpikePattern.getISI0()+somaSpikePattern.getISI(1))/2d;
		}
		
		return somaSpikePattern.getFSL() / isiFilt;
	}
	
	public double getStutFactor() {
		double stutFactor = 0;
		
		double[] isis = somaSpikePattern.getISIs();
		int maxISIidx = GeneralUtils.findMaxIdx(isis);
		double maxISI = isis[maxISIidx];	
		
		if(maxISIidx == isis.length-1) {
			//last ISI is max ISI
			return 0;
		}		
		if(maxISIidx>0){
			double pre_maxISI = isis[maxISIidx-1];
			stutFactor += maxISI / pre_maxISI;			
		}else {//CA1 radiatum - fig 11G - NASP original - should be PSTUT
			return 0; //PSTUT_FACTOR = 4
		}
		
				
		double post_maxISI = isis[maxISIidx+1];		
		//System.out.print("\t"+startIdx+","+maxISI+","+post_maxISI+","+factor);
		stutFactor += maxISI/post_maxISI;
		return stutFactor;
	}
	
	private float delayWeight(SpikePatternClass targetClass){
		float fslWeight = 0;		
		if(targetClass.contains(SpikePatternComponent.D)){
			if(!patternClass.contains(SpikePatternComponent.D))
			{
				if(somaSpikePattern.getISIs()==null){
					fslWeight = 1f;
				}else{
					double fsl = somaSpikePattern.getFSL();
					
					double isiFilt=somaSpikePattern.getISI0();
					if(somaSpikePattern.getISIs().length>1){
						isiFilt = (somaSpikePattern.getISI0()+somaSpikePattern.getISI(1))/2d;
					}	
					fslWeight =  (float) (1 + StatUtil.calculateObsNormalizedError(DELAY_FACTOR, (fsl/isiFilt)));
				}				
			}else 
				fslWeight= 1f;
		}
		
		if(!targetClass.contains(SpikePatternComponent.D)){
			if(patternClass.contains(SpikePatternComponent.D)){
				double fsl = somaSpikePattern.getFSL();				
				double isiFilt=0;
				if(somaSpikePattern.getISIs() == null || somaSpikePattern.getISIs().length <1){
					isiFilt = 0;
				}else{
					isiFilt = somaSpikePattern.getISI0();
					if(somaSpikePattern.getISIs().length>1){
						isiFilt = (somaSpikePattern.getISI0()+somaSpikePattern.getISI(1))/2d;
					}	
				}
				fslWeight = (float) (1f + StatUtil.calculateObsNormalizedError(DELAY_FACTOR, (fsl/isiFilt)));
			}
		else fslWeight = 0.01f;
		}			
		return fslWeight;
	}
	
	/*
	 * look at last 2 isis, and max isis
	 */
	private boolean hasPostSLN(){
		double isiFilt=somaSpikePattern.getISILast();
		if(somaSpikePattern.getISIs().length>1){
			isiFilt = (somaSpikePattern.getISILast()+
					somaSpikePattern.getISI(somaSpikePattern.getISIs().length-2))
					/2d;
		}		
		if(somaSpikePattern.getPSS() > SLN_FACTOR * isiFilt &&
				somaSpikePattern.getPSS() > SLN_FACTOR * somaSpikePattern.getMaxISI()){					
			return true;
		}else
			return false;
	}
	private float postSlnWeight(SpikePatternClass targetClass){
		float pssWeight = 0;
		if(targetClass.contains(SpikePatternComponent.SLN)){
			if(!patternClass.contains(SpikePatternComponent.SLN)){
				
				double isiFilt=0;
				if(somaSpikePattern.getISIs() == null || somaSpikePattern.getISIs().length <1){
					isiFilt = 0;
				}else{
					isiFilt=somaSpikePattern.getISILast();
					if(somaSpikePattern.getISIs().length>1){
						isiFilt = (somaSpikePattern.getISILast()+
								somaSpikePattern.getISI(somaSpikePattern.getISIs().length-2))
								/2d;
					}
				}				
				double pss = somaSpikePattern.getPSS() ; 					
				 pssWeight  = (float) (1+ StatUtil.calculateObsNormalizedError(SLN_FACTOR, (pss/isiFilt)));
				// error += (float) (1+StatUtil.calculateObsNormalizedError(SLN_FACTOR, (pss/somaSpikePattern.getMaxISI())));					
				}
			else pssWeight = 1f;
			}
		
		if(!targetClass.contains(SpikePatternComponent.SLN)){
			if(patternClass.contains(SpikePatternComponent.SLN)){
				double isiFilt=somaSpikePattern.getISILast();
					if(somaSpikePattern.getISIs().length>1){
						isiFilt = (somaSpikePattern.getISILast()+
								somaSpikePattern.getISI(somaSpikePattern.getISIs().length-2))
								/2d;
					}
					double pss = somaSpikePattern.getPSS() ; 					
					 pssWeight  = (float) (1+ StatUtil.calculateObsNormalizedError(SLN_FACTOR, (pss/isiFilt)));
					// error += (float) (1+StatUtil.calculateObsNormalizedError(SLN_FACTOR, (pss/somaSpikePattern.getMaxISI())));					
				}
			else pssWeight = 0.01f;
			}		
			
		return pssWeight;	
	}
	
	private void classifySpikes_AK(int n, boolean isModel){
		//isModel=false;
		//System.out.println("called");
		solverStats = new SolverResultsStat[4];
		
		double[] latencies = somaSpikePattern.getISILatenciesToSecondSpike(n);
		double[] ISIs = somaSpikePattern.getISIsStartingFromIdx(n);
		
		/*
		 * to simplify -- so that stat tests match across ACM and CARLsim
		 * 	- because CARLsim only returns integer spike times
		 *  - stat tests are sensitive to small decimal values some times
		 */
		//latencies = GeneralUtils.roundOff(latencies);
		//ISIs = GeneralUtils.roundOff(ISIs);
		//System.out.println("ISIs before stat!");
		//GeneralUtils.displayArray(ISIs);
		double scaleFactor = GeneralUtils.findMin(ISIs);
		double[] X = StatUtil.shiftLeftToZeroAndScaleSimple(latencies, scaleFactor);
		double[] Y = StatUtil.scaleSimple(ISIs, scaleFactor);
		
		if(StatAnalyzer.display_stats){
			GeneralUtils.display2DArrayVerticalUnRounded(new double[][]{X,Y});
		}
		LeastSquareSolverUtil l = new LeastSquareSolverUtil(X, Y);
		
		/*
		 * 1. SSF
		 */
		
			solverStats[0] = l.solveFor1Parm(1);			
			solverStats[1] = l.solveFor2Parms(0, 1);	
			solverStats[2] = l.solveFor3Parms(solverStats[1].getM1(), 1, 1);
			solverStats[3] = l.solveFor4Parms(solverStats[2].getM1(), 1, 0, 1);
		
		populateSolverSlopes();
		
	
		//solverStats[4] = l.solveForLog(0);
		
	/*	if(StatAnalyzer.isSignificantImprovement(solverStats[0].getFitResidualsAbs(), solverStats[4].getFitResidualsAbs(), 1, 5)) {
			System.out.println("LOG FIT!!!");
		return;
		}
		*/
		double[] f_p_stats = new double[4];
		for(int i=0;i<4;i++){
			f_p_stats[i]=Double.NaN;
		}
		if(!StatAnalyzer.isSignificantImprovement(solverStats[0].getFitResidualsAbs(), solverStats[1].getFitResidualsAbs(), 1, 2, f_p_stats)) {	
			// model requires 1->3 parm check!
			if(!(isModel)||
					!StatAnalyzer.isSignificantImprovement(solverStats[0].getFitResidualsAbs(), solverStats[2].getFitResidualsAbs(), 1, 3, f_p_stats)){
				patternClass.addComponent(SpikePatternComponent.NASP);
				populateFPStats(f_p_stats, 1);
				return;
			}
			patternClass.addComponent(SpikePatternComponent.ASP);
			patternClass.addComponent(SpikePatternComponent.NASP);
			populateFPStats(f_p_stats, 3);
			return;
						
		}
		/*
		 * 2. ASP
		 */	
		populateFPStats(f_p_stats, 1);
		f_p_stats = new double[4];
		for(int i=0;i<4;i++){
			f_p_stats[i]=Double.NaN;
		}
		if(!StatAnalyzer.isSignificantImprovement(solverStats[1].getFitResidualsAbs(), solverStats[2].getFitResidualsAbs(), 2, 3, f_p_stats)) {
			if(solverStats[1].getM1() > SLOPE_THRESHOLD){				
				patternClass.addComponent(SpikePatternComponent.ASP);
			}else{
				patternClass.addComponent(SpikePatternComponent.NASP);
			}	
			populateFPStats(f_p_stats, 2);
			return;
		}			
		/*
		 * 3. ASP.NASP / ASP.ASP
		 */	
		populateFPStats(f_p_stats, 2);
		f_p_stats = new double[4];
		for(int i=0;i<4;i++){
			f_p_stats[i]=Double.NaN;
		}
		if(!StatAnalyzer.isSignificantImprovement(solverStats[2].getFitResidualsAbs(), solverStats[3].getFitResidualsAbs(), 3, 4, f_p_stats)){				
				patternClass.addComponent(SpikePatternComponent.ASP);
				patternClass.addComponent(SpikePatternComponent.NASP);	
				populateFPStats(f_p_stats, 3);
			return;
		}else{
			patternClass.addComponent(SpikePatternComponent.ASP);			
			if(solverStats[3].getM2() > SLOPE_THRESHOLD){
				patternClass.addComponent(SpikePatternComponent.ASP);
			}else{
				patternClass.addComponent(SpikePatternComponent.NASP);	
			}
			populateFPStats(f_p_stats, 3);
			return;
		}
	}
private void populateSolverSlopes(){	
	patternClass.addClassificationParameter(ClassificationParameterID.B_1p, solverStats[0].getC1());
	
	patternClass.addClassificationParameter(ClassificationParameterID.M_2p, solverStats[1].getM1());
	patternClass.addClassificationParameter(ClassificationParameterID.B_2p, solverStats[1].getC1());
	
	patternClass.addClassificationParameter(ClassificationParameterID.M_3p, solverStats[2].getM1());
	patternClass.addClassificationParameter(ClassificationParameterID.B1_3p, solverStats[2].getC1());
	patternClass.addClassificationParameter(ClassificationParameterID.B2_3p, solverStats[2].getC2());
	patternClass.addClassificationParameter(ClassificationParameterID.N_ISI_cut_3p, 
				solverStats[2].getBreakPoint()<0?0:(double)solverStats[2].getBreakPoint());
	
	patternClass.addClassificationParameter(ClassificationParameterID.M1_4p, solverStats[3].getM1());
	patternClass.addClassificationParameter(ClassificationParameterID.B1_4p, solverStats[3].getC1());
	patternClass.addClassificationParameter(ClassificationParameterID.M2_4p, solverStats[3].getM2());
	patternClass.addClassificationParameter(ClassificationParameterID.B2_4p, solverStats[3].getC2());
	patternClass.addClassificationParameter(ClassificationParameterID.N_ISI_cut_4p,
			solverStats[3].getBreakPoint()<0?0:(double)solverStats[3].getBreakPoint());	
}
private void populateFPStats(double[] f_p_stats, int level){
	/*F_12, F_crit_12, F_23, F_crit_23, F_34, F_crit_34,
	P_12, P_12_UV, P_23, P_23_UV, P_34, P_34_UV,
	*/
	if(level ==1){
		patternClass.addClassificationParameter(ClassificationParameterID.F_12, f_p_stats[0]);
		patternClass.addClassificationParameter(ClassificationParameterID.F_crit_12, f_p_stats[1]);
		patternClass.addClassificationParameter(ClassificationParameterID.P_12, f_p_stats[2]);
		patternClass.addClassificationParameter(ClassificationParameterID.P_12_UV, f_p_stats[3]);
	}
	if(level ==2){
		patternClass.addClassificationParameter(ClassificationParameterID.F_23, f_p_stats[0]);
		patternClass.addClassificationParameter(ClassificationParameterID.F_crit_23, f_p_stats[1]);
		patternClass.addClassificationParameter(ClassificationParameterID.P_23, f_p_stats[2]);
		patternClass.addClassificationParameter(ClassificationParameterID.P_23_UV, f_p_stats[3]);
	}
	if(level ==3){
		patternClass.addClassificationParameter(ClassificationParameterID.F_34, f_p_stats[0]);
		patternClass.addClassificationParameter(ClassificationParameterID.F_crit_34, f_p_stats[1]);
		patternClass.addClassificationParameter(ClassificationParameterID.P_34, f_p_stats[2]);
		patternClass.addClassificationParameter(ClassificationParameterID.P_34_UV, f_p_stats[3]);
	}
	
}
public void calculateAdaptationForNonSP(int n){
		solverStats = new SolverResultsStat[4];		
		double[] latencies = somaSpikePattern.getISILatenciesToSecondSpike(n);
		double[] ISIs = somaSpikePattern.getISIsStartingFromIdx(n);		
		
		double scaleFactor = GeneralUtils.findMin(ISIs);
		double[] X = StatUtil.shiftLeftToZeroAndScaleSimple(latencies, scaleFactor);
		double[] Y = StatUtil.scaleSimple(ISIs, scaleFactor);		
		
		LeastSquareSolverUtil l = new LeastSquareSolverUtil(X, Y);
		
		solverStats[0] = l.solveFor1Parm(1);			
		solverStats[1] = l.solveFor2Parms(0, 1);	
		solverStats[2] = l.solveFor3Parms(solverStats[1].getM1(), 1, 1);
		solverStats[3] = l.solveFor4Parms(solverStats[2].getM1(), 1, 0, 1);
				
		/*patternClass.m[0] = (float) solverStats[1].getM1();
		patternClass.m[1] = (float) solverStats[2].getM1();
		patternClass.m[2] = (float) solverStats[3].getM1();
		patternClass.m[3] = (float) solverStats[3].getM2();		*/
	}
	
	
	private int classifyFastTransientStat(int firstNIsIs){	
		int idx = 0;
		for(int i=firstNIsIs; i>=2; i--){
			double[][] latenciesAndISIs = somaSpikePattern.getFirstNISIsAndTheirLatenciesToSecondSpike(i);
			double[] latencies = latenciesAndISIs[0];
			double[] ISIs = latenciesAndISIs[1];				
			double scaleFactor = GeneralUtils.findMin(ISIs);
			double[] X = StatUtil.shiftLeftToZeroAndScaleSimple(latencies, scaleFactor);
			double[] Y = StatUtil.scaleSimple(ISIs, scaleFactor);
			
			LeastSquareSolverUtil l = new LeastSquareSolverUtil(X, Y);	
			SolverResultsStat solverStatLocal = l.solveFor2Parms(0, 1);
			//System.out.println("RASP slope:  "+solverStatLocal.getM1());
			if(solverStatLocal.getM1()>SLOPE_THRESHOLD_FT){
				patternClass.removeLastAddedComponent();				
				patternClass.addComponent(SpikePatternComponent.RASP);
				patternClass.addClassificationParameter(ClassificationParameterID.M_RASP, solverStatLocal.getM1());
				patternClass.addClassificationParameter(ClassificationParameterID.B_RASP, solverStatLocal.getC1());
				patternClass.addClassificationParameter(ClassificationParameterID.N_ISI_cut_RASP, (double)i);
				
				/*else{
					fastTransientComp = SpikePatternComponent.WASP;	
					fastTransientComp.properties.put(PatternFeatureID.sfa_linear_m1, solverStatLocal.getM1());
					fastTransientComp.properties.put(PatternFeatureID.sfa_linear_b1, solverStatLocal.getC1());
					fastTransientComp.properties.put(PatternFeatureID.n_sfa_isis1, (double)i);
					patternClass.addComponent(fastTransientComp);
				}*/
				
				idx = i;
				break;
			}
		}
		return idx;
	}
	
	private void addAdaptationWeight(SpikePatternClass targetClass){
		float constWeight = 3f;
		float defaultWeight = 1f;
		
		if(
				(targetClass.contains(SpikePatternComponent.RASP) && !patternClass.contains(SpikePatternComponent.RASP))
				||
				(!targetClass.contains(SpikePatternComponent.RASP) && patternClass.contains(SpikePatternComponent.RASP))
				
			){		
			dynamicFeatWeightMatrix.put(PatternFeatureID.n_sfa_isis0, constWeight);
			defaultWeight = 2f;
			}else{
				dynamicFeatWeightMatrix.put(PatternFeatureID.n_sfa_isis0, defaultWeight);
				defaultWeight = 1f;
			}
		
		if(targetClass.containsSP()){
			if(!targetClass.containsSTUT()&&!targetClass.containsSWB()) {
				if(patternClass.containsSTUT()||patternClass.containsSWB()) {
					dynamicFeatWeightMatrix.put(PatternFeatureID.n_sfa_isis1, 1000f);
					dynamicFeatWeightMatrix.put(PatternFeatureID.n_sfa_isis2, 1000f);
					return;
				}
			}
		//	if(patternClass.containsSTUT() || patternClass.cont)
			int modelNpwParms = patternClass.getnPieceWiseParms();
			int targetNpwParms = targetClass.getnPieceWiseParms();
			
			//System.out.println(modelNpwParms+" "+targetNpwParms);
			
			if(SFA_WEIGHT_DET_SCENARIOS[modelNpwParms][targetNpwParms] == 0){
				dynamicFeatWeightMatrix.put(PatternFeatureID.n_sfa_isis1, defaultWeight);
				dynamicFeatWeightMatrix.put(PatternFeatureID.n_sfa_isis2, defaultWeight);
			}
			if(SFA_WEIGHT_DET_SCENARIOS[modelNpwParms][targetNpwParms] == 1){
				dynamicFeatWeightMatrix.put(PatternFeatureID.n_sfa_isis1, constWeight);
				dynamicFeatWeightMatrix.put(PatternFeatureID.n_sfa_isis2, defaultWeight);
			}
			if(SFA_WEIGHT_DET_SCENARIOS[modelNpwParms][targetNpwParms] == 2){
				dynamicFeatWeightMatrix.put(PatternFeatureID.n_sfa_isis1, defaultWeight);
				dynamicFeatWeightMatrix.put(PatternFeatureID.n_sfa_isis2, constWeight);
			}
			if(SFA_WEIGHT_DET_SCENARIOS[modelNpwParms][targetNpwParms] == 3){
				dynamicFeatWeightMatrix.put(PatternFeatureID.n_sfa_isis1, constWeight);
				dynamicFeatWeightMatrix.put(PatternFeatureID.n_sfa_isis2, constWeight);
			}
		}else{
			dynamicFeatWeightMatrix.put(PatternFeatureID.n_sfa_isis1, defaultWeight);
			dynamicFeatWeightMatrix.put(PatternFeatureID.n_sfa_isis2, defaultWeight);
		}
		
	}
	
	private float stutWeight(SpikePatternClass targetClass){
		if(targetClass.containsSTUT()){
			if(patternClass.containsSP())
				return 3f;	
			if(patternClass.containsSWB())
				return 1.5f;
		}if(targetClass.containsSWB()){
			if(patternClass.containsSP())
				return 3f;	
			if(patternClass.containsSTUT())
				return 1.5f;
		}
		return 1f;
	}
	
	private boolean hasPSTUT(int startIdx){
		double[] isis = somaSpikePattern.getISIsStartingFromIdx(startIdx);
		
		int maxISIidx = GeneralUtils.findMaxIdx(isis);
		if(maxISIidx == isis.length-1) {
			//last ISI is max ISI
			return false;
		}
		double factor = 0;
		double maxISI = isis[maxISIidx];		
		if(maxISIidx>0){
			double pre_maxISI = isis[maxISIidx-1];
			factor += maxISI / pre_maxISI;			
		}else {//CA1 radiatum - fig 11G - NASP original - should be PSTUT
			factor =1; //PSTUT_FACTOR = 4
		}
		double post_maxISI = isis[maxISIidx+1];		
		//System.out.print("\t"+startIdx+","+maxISI+","+post_maxISI+","+factor);
		factor += maxISI/post_maxISI;
		if(factor>= PSTUT_FACTOR)
			return true;
		else 
			return false;
	}
	
	private boolean hasSTUT(int startIdx){
		double[] ISIs = somaSpikePattern.getISIsStartingFromIdx(startIdx);
		BurstPattern bp= new BurstPattern(ISIs, PSTUT_PRE_FACTOR, PSTUT_POST_FACTOR);
		
		if(bp.isBurst())
			return true;
		else
			return false;
	}
	
	private boolean isPSTUT(){
		double[] ISIs = somaSpikePattern.getISIs();
		BurstPattern bp= new BurstPattern(ISIs, PSTUT_PRE_FACTOR, PSTUT_POST_FACTOR);
		
		if((1d/bp.getAvgIntraBurstAvgISI())*1000 >= MIN_TSTUT_FREQ) 
			return true;
		else
			return false;
	}
	
	
	public Map<PatternFeatureID, Float> getDynamicFeatWeightMatrix(){
		return dynamicFeatWeightMatrix;
	}
	
	public SolverResultsStat[] getSolverResultsStats(){
		return solverStats;
	}
	public SpikePatternClass getSpikePatternClass(){
		return this.patternClass;
	}
	
	private void classifySpikes(int n){
		
		//System.out.println("called");
		solverStats = new SolverResultsStat[4];
		
		double[] latencies = somaSpikePattern.getISILatenciesToSecondSpike(n);
		double[] ISIs = somaSpikePattern.getISIsStartingFromIdx(n);
		
		//latencies = GeneralUtils.roundOff(latencies);
		//ISIs = GeneralUtils.roundOff(ISIs);
		
		double scaleFactor = GeneralUtils.findMin(ISIs);
		double[] X = StatUtil.shiftLeftToZeroAndScaleSimple(latencies, scaleFactor);
		double[] Y = StatUtil.scaleSimple(ISIs, scaleFactor);
		
		if(StatAnalyzer.display_stats){
			GeneralUtils.display2DArrayVerticalUnRounded(new double[][]{X,Y});
		}
		LeastSquareSolverUtil l = new LeastSquareSolverUtil(X, Y);
		
		/*
		 * 1. SSF
		 */
		
		solverStats[0] = l.solveFor1Parm(1);			
		solverStats[1] = l.solveFor2Parms(0, 1);	
		solverStats[2] = l.solveFor3Parms(solverStats[1].getM1(), 1, 1);
		solverStats[3] = l.solveFor4Parms(solverStats[2].getM1(), 1, 0, 1);
				
		if(isAdaptation(1,2)){
			if(isAdaptation(2,3)){
				if(isAdaptation(3,4)){
					if(solverStats[3].getM1() < 0)	
						patternClass.addComponent(SpikePatternComponent.NASP); //acceleration is NASP!
					else					
						patternClass.addComponent(SpikePatternComponent.ASP);
					
					if(solverStats[3].getM2() < 0)	
						patternClass.addComponent(SpikePatternComponent.NASP); //acceleration is NASP!
					else					
						patternClass.addComponent(SpikePatternComponent.ASP);
				}else{
					if(solverStats[2].getM1() < 0)	
						patternClass.addComponent(SpikePatternComponent.NASP); //acceleration is NASP!
					else					
						patternClass.addComponent(SpikePatternComponent.ASP);
					
					patternClass.addComponent(SpikePatternComponent.NASP);
				}
			}else{
				if(solverStats[1].getM1() < 0)	
					patternClass.addComponent(SpikePatternComponent.NASP); //acceleration is NASP!
				else					
					patternClass.addComponent(SpikePatternComponent.ASP);
			}
		}else{
			if(isAdaptation(1,3)){
				if(isAdaptation(3,4)){
					if(solverStats[3].getM1() < 0)	
						patternClass.addComponent(SpikePatternComponent.NASP); //acceleration is NASP!
					else					
						patternClass.addComponent(SpikePatternComponent.ASP);
					
					if(solverStats[3].getM2() < 0)	
						patternClass.addComponent(SpikePatternComponent.NASP); //acceleration is NASP!
					else					
						patternClass.addComponent(SpikePatternComponent.ASP);
				}else{
					if(solverStats[2].getM1() < 0)	
						patternClass.addComponent(SpikePatternComponent.NASP); //acceleration is NASP!
					else					
						patternClass.addComponent(SpikePatternComponent.ASP);
					
					patternClass.addComponent(SpikePatternComponent.NASP);
				}
			}else{
				patternClass.addComponent(SpikePatternComponent.NASP);
			}
		}
		
	}
	/*
	 * wrapper for stat significant improvement to include additional checks if necessary!
	 */
	private boolean isAdaptation(int fromNParm, int toNParm){
	
		double[] f_p_stats = new double[4];
		
		return StatAnalyzer.isSignificantImprovement(solverStats[fromNParm-1].getFitResidualsAbs(), 
				solverStats[toNParm-1].getFitResidualsAbs(), 
				fromNParm, 
				toNParm, f_p_stats);			
			
		
	}
	
	private int findFastTransientIndex(){
		int idx = 0;
		double[] ISIs = somaSpikePattern.getISIs();
		int maxIdxForCheck = ((FAST_TRANS_ISI_CNT>(ISIs.length-1))? (ISIs.length-1) : FAST_TRANS_ISI_CNT);
		for(int i=maxIdxForCheck;i>1;i--){
			double[] ISIsStartingFromIdx =// somaSpikePattern.getNisisStartingFromIdx(i, 2);
					somaSpikePattern.getISIsStartingFromIdx(i);
			double[] ISIsBeforeIdx = somaSpikePattern.getFirstNISIs(i);
			if(StatUtil.calculateMean(ISIsStartingFromIdx) >
					FAST_TRANSIENT_FACTOR * StatUtil.calculateMean(ISIsBeforeIdx)){
				idx=i;
				break;
			}				
		}		
		return idx;
	}
	
	public static void main_org(String[] args) {
		//String fileName = args[0];
		try {
			BufferedReader br = new BufferedReader(new FileReader("spike.csv"));
			String str = br.readLine();
			if(str==null){
				System.out.println("Empty input(s)! - line 1");
				System.exit(-1);
			}
			StringTokenizer st = new StringTokenizer(str, ",");
			if(st.countTokens()!=2){
				System.out.println("line 1 requires 2 tokens!");
				System.exit(-1);
			}
			double current = Double.parseDouble(st.nextToken());
			double duration = Double.parseDouble(st.nextToken());
			
			str = br.readLine();
			if(str==null){
				System.out.println("Empty input(s)! - line 2");
				System.exit(-1);
			}
			st = new StringTokenizer(str, ",");
			List<Double> sptimes = new ArrayList<>();			
			while(st.hasMoreTokens()){
				sptimes.add(Double.parseDouble(st.nextToken()));
			}
			
			str=br.readLine();
			if(str==null){
				System.out.println("Empty input! - line 3");
				System.exit(-1);
			}
			double swa = Double.parseDouble(str);			
			br.close();
			double[] spikeTimes = new double[sptimes.size()];
			for(int i=0;i<spikeTimes.length;i++){
				spikeTimes[i] = sptimes.get(i);
			}
		
			SpikePatternAdapting sp = new SpikePatternAdapting(spikeTimes, current, 0, duration);
			SpikePatternClassifier classifier = new SpikePatternClassifier(sp);
			StatAnalyzer.display_stats = true;
			classifier.classifySpikePattern_EXP(swa, false);
			classifier.getSpikePatternClass().display();
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	public static void main(String[] args) {		
			
			double current =250;
			double duration=100;
					
			double swa =2;			
		
			//ca1pyr-IF
			//double[] spikeTimes = new double[] {15.98,30.75,48.3,71.38,97.7,129.7,176.2,211.1,260.5,304.2,368.15,432};
			//dggran-IF
			double[] spikeTimes = new double[] {8.3,14.8,24.07,	37,55.56,72.22,	91.67};

			SpikePatternAdapting sp = new SpikePatternAdapting(spikeTimes, current, 0, duration);
			SpikePatternClassifier classifier = new SpikePatternClassifier(sp);
			StatAnalyzer.display_stats = false;
			classifier.classifySpikePattern_EXP(swa, false);
			ClassificationParameterID[] parms = ClassificationParameterID.values();
			for(ClassificationParameterID parm:parms)
				System.out.print("\t"+parm);
			System.out.println();
			classifier.getSpikePatternClass().display(parms);
		
		
	}
}
