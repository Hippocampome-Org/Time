package ec.app.izhikevich.evaluator;

import java.util.ArrayList;
import java.util.List;

import ec.app.izhikevich.ModelEvaluatorWrapper;
import ec.app.izhikevich.inputprocess.InputMCConstraint;
import ec.app.izhikevich.inputprocess.InputSpikePatternConstraint;
import ec.app.izhikevich.inputprocess.PhenotypeConstraint;
import ec.app.izhikevich.model.Izhikevich9pModelMC;
import ec.app.izhikevich.model.IzhikevichSolverMC;
import ec.app.izhikevich.outputprocess.CarlMcSimData;
import ec.app.izhikevich.outputprocess.CarlSpikePattern;
import ec.app.izhikevich.spike.PatternType;
import ec.app.izhikevich.spike.SpikePatternAdapting;
import ec.app.izhikevich.util.DisplayUtilMcwSyn;
import ec.app.izhikevich.util.GeneralUtils;

public class ModelEvaluatorMC{
	public static boolean EVAL_MC_FOR_MC = true;
	Izhikevich9pModelMC model;	
	InputSpikePatternConstraint[] expSpikePatternData;	
	InputMCConstraint[] mcConstraintData;	
	PhenotypeConstraint[] phenConstraintData;
	
	double[] patternRepairWeights;
	private boolean displayAll;
	private boolean displayStatus;
	private boolean displayForPlot;
	private int displayForPlotIdx;
	private boolean displayOnlyClass;
	
	private boolean externalSimulatorUsed;
	CarlSpikePattern[] carlSpikePattern;
	CarlMcSimData carlMcSimData;
	
	private MultiCompConstraintEvaluator mcEvalholder;
	private SpikePatternEvaluatorV2 spEvalHolder;
	
	public List<SpikePatternEvaluatorV2> spEvalHolders;
	
	private PhenotypeConstraintEvaluator ptEvalHolder;
	private List<SpikePatternAdapting[]> modelSpikePatternHolder;
	private boolean rampRheo;
	
	private boolean saveModelPattern;
	/*
	 * carlSpikePattern array only maps to the expSpikePatternData array
	 * -- meaning there are no spike pattern data for dendritic compartments
	 * -- another array will map to mcConstraintData -- should be retrived from CARLsim
	 */
	
	/*
	 * incase input current is part of genome
	 */
	double[] somaticCurrents;
	int somCurrIdx=0;
	double[] synWeight;
	
	/*
	 * Internal Simulator
	 */
	public ModelEvaluatorMC(Izhikevich9pModelMC model, 
			InputSpikePatternConstraint[] expSpikePatternData,
			PhenotypeConstraint[] phenotypeConstraintData,
			double[] patternRepWeights,
			InputMCConstraint[] mcConstraintData,
			double[] currents,
			double[] weight) {
		if(expSpikePatternData==null) {
			System.out.println("NULL expSpikePatternData!!!");
			throw new IllegalStateException("NULL expSpikePatternData!!!");
		}
		if(EVAL_MC_FOR_MC)
		if(mcConstraintData==null && model.getNCompartments()>1 && !model.isIso_comp()) {
			System.out.println("NULL mcConstraintData!!!");
			throw new IllegalStateException("NULL mcConstraintData!!!");
		}
		this.model = model;
		this.expSpikePatternData = expSpikePatternData;	
		this.patternRepairWeights = patternRepWeights;
		this.mcConstraintData = mcConstraintData;
		this.phenConstraintData = phenotypeConstraintData;
		this.somaticCurrents = currents;
		this.synWeight = weight;
		this.setDisplayAll(false);
		this.setDisplayForPlot(false);
		this.setDisplayOnlyClass(false);
		displayForPlotIdx = -1;
		this.externalSimulatorUsed=false;
		
		modelSpikePatternHolder = null;
		this.rampRheo = false; //by default false, later change it to true?
		saveModelPattern = false;
		
		spEvalHolders = new ArrayList<>();
	}
	
	
	/*
	 * External simulator -- carlsim
	 */
	public ModelEvaluatorMC(Izhikevich9pModelMC model, 
			InputSpikePatternConstraint[] expSpikePatternData,
			double[] patternRepWeights,
			InputMCConstraint[] mcConstraintData,
			double[] currents,
			double[] weight,
			CarlSpikePattern[] carlSpikePattern,
			CarlMcSimData carlMcSimData) {
		if(expSpikePatternData==null) {
			System.out.println("NULL expSpikePatternData!!!");
			throw new IllegalStateException("NULL expSpikePatternData!!!");
		}
		if(EVAL_MC_FOR_MC)
		if(mcConstraintData==null && model.getNCompartments()>1) {
			System.out.println("NULL mcConstraintData!!!");
			throw new IllegalStateException("NULL mcConstraintData!!!");
		}
		this.model = model;
		this.expSpikePatternData = expSpikePatternData;	
		this.patternRepairWeights = patternRepWeights;
		this.mcConstraintData = mcConstraintData;
		this.somaticCurrents = currents;
		this.synWeight = weight;
		this.setDisplayAll(false);
		this.setDisplayForPlot(false);
		this.setDisplayOnlyClass(false);
		displayForPlotIdx = -1;
		
		this.carlSpikePattern = carlSpikePattern;
		this.carlMcSimData = carlMcSimData;
		this.externalSimulatorUsed=true;
		
		modelSpikePatternHolder = null;
		saveModelPattern=false;
		
		spEvalHolders = new ArrayList<>();
	}
	
public boolean measureFeatures(int minSpikes) {
	model.setDurationOfCurrent(500);
	
	IzhikevichSolverMC solver = new IzhikevichSolverMC(model);
    if(displayStatus) {solver.setDisplayStatus(true);} 
    SpikePatternAdapting[] model_spike_pattern = solver.solveAndGetSpikePatternAdapting();
    
    if(model_spike_pattern!=null){
    	for(SpikePatternAdapting modelSpikePatternAdapting: model_spike_pattern)
    		if(modelSpikePatternAdapting == null){    			
    			return false;
    		}
    }else{
    	return false;
    }    
    
    if(model_spike_pattern[0].getNoOfSpikes() < minSpikes){
    	return false;
    }
	SpikePatternEvaluatorV2 evaluator = new SpikePatternEvaluatorV2(model_spike_pattern[expSpikePatternData[0].getCompartment()], 
			expSpikePatternData[0], 
			this.patternRepairWeights,
			this.model.getvMin()[expSpikePatternData[0].getCompartment()], 
			this.model.getvR()[expSpikePatternData[0].getCompartment()], 
			this.model.getvT()[expSpikePatternData[0].getCompartment()], 
			displayAll,
			displayOnlyClass,
			false);
	if(!model_spike_pattern[0].isValidSpikesPattern()){
		return false;
	}
   	
	this.setSpEvalHolder(evaluator);
	
	PhenotypeConstraintEvaluator pcEvaluator = new PhenotypeConstraintEvaluator(model, displayAll);		
	this.setPtEvalHolder(pcEvaluator);	
	
	return true;
}
	public float getFitness() {	
		
		float fitness=0;
		float totPatternError = 0;
		float patternError = 0;
		float totMcError = 0;
		float mcError = 0;
		float phenTypeError = 0;
		float totPhenTypeError = 0;
		//float count =0;
				
		if(!isKValid() || !isAValid() || !isCmValid() || !isGValid()){// || !isDValid()) {
			if(displayAll) {     	 System.out.println("Invalid Gene!");		}
			return -Float.MAX_VALUE;
		}
		
		/*
		 * 1. pattern constraints
		 */
		for(int i = 0; i < expSpikePatternData.length; i++) {			
			if(displayStatus) {System.out.println("Start pattern data point....");}			
			patternError = calculatePatternError(expSpikePatternData[i], i);			
			if(patternError < Float.MAX_VALUE && totPatternError < Float.MAX_VALUE) {
				totPatternError += patternError;				
			}else{
				return -Float.MAX_VALUE;
			}						
			if(displayStatus) {System.out.println("End pattern data point...");}
		}		
		if(displayAll) {String displayString = "\n\nTotPatternConsError.\t"+totPatternError;System.out.print(displayString+"\n***********");}	
		if(!(totPatternError<Float.MAX_VALUE)) return -Float.MAX_VALUE;
		
		/*
		 * 1.b: phenotype constraints
		 */
		if(phenConstraintData!=null){
			PhenotypeConstraintEvaluator pcEvaluator = new PhenotypeConstraintEvaluator(model, displayAll);		
			//this.setPtEvalHolder(pcEvaluator);
			for(int i=0;i<phenConstraintData.length;i++ ){				
				if(displayStatus) {System.out.println("Start phenotype data point....");}			
				phenTypeError = pcEvaluator.calculatePhenotypeConstraintError(phenConstraintData[i]);			
				if(phenTypeError < Float.MAX_VALUE && totPhenTypeError < Float.MAX_VALUE) {
					totPhenTypeError += phenTypeError;				
				}else{
					return -Float.MAX_VALUE;
				}						
				if(displayStatus) {System.out.println("End phenotype data point...");}
			}
			if(displayAll) {String displayString = "\n\nTotPhenConsError.\t"+totPhenTypeError;System.out.print(displayString+"\n***********");}	
			if(!(totPhenTypeError<Float.MAX_VALUE)) return -Float.MAX_VALUE;
			pcEvaluator = null;
		}
		/*
		 * 2. mcCompartment constraints
		 */
		if(EVAL_MC_FOR_MC)
		if(model.getNCompartments()>1 && !model.isIso_comp()){
			MultiCompConstraintEvaluator mcEvaluator;
			if(externalSimulatorUsed){
				mcEvaluator = new MultiCompConstraintEvaluator(model, displayAll, synWeight, carlMcSimData);
			}else{
				mcEvaluator = new MultiCompConstraintEvaluator(model, displayAll, synWeight, rampRheo);
			}			
			setMcEvalholder(mcEvaluator);
			if(saveModelPattern) {
				mcEvaluator.setSaveSP_Is_hold(true);
			}
			//System.out.println(mcConstraintData.length);
			for(int i = 0; i < mcConstraintData.length; i++) {			
				if(displayStatus) {System.out.println("Start mc data point....");}			
				mcError = mcEvaluator.calculateMcConstraintError(mcConstraintData[i]);			
				if(mcError < Float.MAX_VALUE && totMcError < Float.MAX_VALUE) {
					totMcError += mcError;				
				}else{
					return -Float.MAX_VALUE;
				}						
				if(displayStatus) {System.out.println("End mc data point...");}
			}		
			if(displayAll) {String displayString = "\n\nTotMcConsError.\t"+totMcError;System.out.print(displayString+"\n***********");}	
			if(!(totMcError<Float.MAX_VALUE)) return -Float.MAX_VALUE;
			mcEvaluator = null;
		}
				
		/*
		 * 3. fitness = error (1.) + error (2.)
		 */
		fitness = - (totPatternError + totMcError + totPhenTypeError);		
		if(Float.isNaN(fitness)){fitness = -Float.MAX_VALUE;}
		if(displayAll) {System.out.print("\n\n<<o>> Fitness\t"+(fitness)+"\n");}
		
		
		return (fitness);
	}
	
	public float[] getMultiObjFitnesses() {	
		int mcConsIndOffset = 0;
		if(this.model.getNCompartments()>3){
			mcConsIndOffset = mcConstraintData.length;
		}
		float[] fitnesses = new float[expSpikePatternData.length + mcConsIndOffset];		
		float patternError = 0;		
		float mcError = 0;
		//float count =0;
				
		if(!isKValid() || !isAValid()) {
			if(displayAll) 
			{     	 System.out.println("**K || A =0**");		}
			for(int i=0;i<fitnesses.length;i++){
				fitnesses[i] = -Float.MAX_VALUE;
			}
			return fitnesses;
		}
		
		/*
		 * 1. pattern constraints
		 */		
		for(int i = 0; i < expSpikePatternData.length; i++) {			
			if(displayStatus) {System.out.println("Start pattern data point....");}			
			patternError = calculatePatternError(expSpikePatternData[i], i);			
			if(patternError < Float.MAX_VALUE) {
				fitnesses[i]= -patternError;				
			}else{
				fitnesses[i]= -Float.MAX_VALUE;
			}						
			if(displayStatus) {System.out.println("End pattern data point...");}
		}		
				
		/*
		 * 2. mcCompartment constraints
		 */
		if(model.getNCompartments()>3){
			MultiCompConstraintEvaluator mcEvaluator = new MultiCompConstraintEvaluator(model, displayAll, synWeight, rampRheo);
			for(int i = 0; i < mcConstraintData.length; i++) {			
				if(displayStatus) {System.out.println("Start mc data point....");}			
				mcError = mcEvaluator.calculateMcConstraintError(mcConstraintData[i]);			
				if(mcError < Float.MAX_VALUE) {
					fitnesses[expSpikePatternData.length+ i] = -mcError;				
				}else{
					fitnesses[expSpikePatternData.length+ i]= -Float.MAX_VALUE;
				}						
				if(displayStatus) {System.out.println("End mc data point...");}
			}			
		}
				
			
	//	if(Float.isNaN(fitness)){fitness = -Float.MAX_VALUE;}
		if(displayAll) {System.out.print("\n\n<<o>> Fitnesses\t");
						GeneralUtils.displayArray(fitnesses);}
		return (fitnesses);
	}
	
	
	private float calculatePatternError(InputSpikePatternConstraint expSpikePatternData, int consIdx) {
		float patternError = Float.MAX_VALUE;
		
		PatternType type = expSpikePatternData.getType();
		double time_min = expSpikePatternData.getTimeMin();		
		double duration_of_current = expSpikePatternData.getCurrentDuration();
		double current = 0;
		/*
		 * if dendritic injection.... 
		 * dendritic current - from input file.-- just for validity check -- not to fit
		 */
		double[] appCurrent = new double[model.getNCompartments()];	
		
		if(expSpikePatternData.getCompartment() > 0) {
			current= expSpikePatternData.getCurrent().getValue();	
			appCurrent[expSpikePatternData.getCompartment()] = current;			
			model.setInputParameters(appCurrent, time_min, duration_of_current);
		}
		if(expSpikePatternData.getCompartment() == 0) {
			current = getNextSomaticCurrent();		//override, if somatic compartment - current evolved
			appCurrent[expSpikePatternData.getCompartment()] = current;	
			model.setInputParameters(appCurrent, time_min, duration_of_current);
		}							
		
		if(displayStatus) {	System.out.println("Start ODE solver...");}
        IzhikevichSolverMC solver = new IzhikevichSolverMC(model);
        if(displayStatus) {solver.setDisplayStatus(true);} 
        SpikePatternAdapting[] model_spike_pattern = null;
        if(externalSimulatorUsed){
        	CarlSpikePattern somaPattern = this.carlSpikePattern[consIdx];
        	if(somaPattern.matchesInputSpikePatternCons(expSpikePatternData)){
        		//System.out.println("MC:: inside calculatepatternerror..");
        		model_spike_pattern = solver.getSpikePatternAdapting(somaPattern.getODESolutionFormat());	   
        	}else{
        		System.out.println("ModelEvaluatorMC.java: Matching failed! exp_spike_constraint to carl_soma_pattern!");
        		System.out.println("CARL output data..");
        		somaPattern.display();
        		System.out.println("input spike pattern cons..");
        		System.out.print("Imin "+expSpikePatternData.getCurrent().getValueMin());
        		System.out.print("Imax "+expSpikePatternData.getCurrent().getValueMax());
        		System.out.print("Idur "+expSpikePatternData.getCurrentDuration());
        	//	throw new IllegalStateException("Matching failed!");
        	}        	   
        }else{
        	model_spike_pattern = solver.solveAndGetSpikePatternAdapting();	 
        }
       // System.out.println("MC:: spikePatternAdaptingGot..");
        if(displayStatus) {	System.out.println("End ODE solver...");}
        
        if(DisplayUtilMcwSyn.displayPatternForExternalPlot)
        	model_spike_pattern[0].display();

        if(model_spike_pattern!=null){
        	for(SpikePatternAdapting modelSpikePatternAdapting: model_spike_pattern)
        		if(modelSpikePatternAdapting == null){
        			if(displayAll) {   System.out.println("**NULL model compartment spike pattern**");	 }
        			return patternError;
        		}
        }else{
        	if(displayAll) {   System.out.println("**NULL model spike pattern** ModelEvaluatorMC:calculatePatternError()");	 }
        	return patternError;
        }
		if(displayAll) {
        	int index = expSpikePatternData.getIndex();
        	String displayString = "\n\n*"+index+"*\t"+type+"\t"+current+"pA\t"+duration_of_current+"ms\n";
        	System.out.print(displayString);
        }
		
		//System.out.println("MC:: evaluator instance creation will..");
		SpikePatternEvaluatorV2 evaluator = new SpikePatternEvaluatorV2(model_spike_pattern[expSpikePatternData.getCompartment()], 
																	expSpikePatternData, 
																	this.patternRepairWeights,
																	this.model.getvMin()[expSpikePatternData.getCompartment()], 
																	this.model.getvR()[expSpikePatternData.getCompartment()], 
																	this.model.getvT()[expSpikePatternData.getCompartment()], 
																	displayAll,
																	displayOnlyClass,
																	externalSimulatorUsed);
		evaluator.setCheckForPatternValidity(true);
		
		//System.out.println("MC:: calculatepatternerror will..");
		patternError = evaluator.calculatePatternError();
		//System.out.println("MC:: calculatepatternerror done..");
			/*
			 * temporary: active dendrites should mirror spikes from soma
			 * may remove this constraint, since dendritic recordings are rare
			 */
	/*		if(expSpikePatternData[i].getFeaturesToEvaluate().contains(PatternFeatureID.dendMirrorSpikes) 
					&& compartmentError < Float.MAX_VALUE) {
				compartmentError += 0.1*temporaryDendriticMirroredSpikesError(model_spike_pattern);
			}*/
		if(saveModelPattern) {
			this.addModelSpikePatternHolder(model_spike_pattern);
			this.setSpEvalHolder(evaluator);
		}
			
		if(displayAll) {
			String comp = "SOMA";
			if(expSpikePatternData.getCompartment()>0){
				comp = "DEND";
			}
			String displayString = "\nFinal"+comp+"patternError.\t"+patternError; 	System.out.print(displayString);
		}					
		solver = null;
		evaluator = null;
		model_spike_pattern = null;
		return patternError;
	}
	

	private boolean isKValid() {
		double[] k=model.getK();
		for(int i=0;i<k.length;i++){
			if(k[i] < 0.1) return false; 
		}
		return true;
	}
	private boolean isAValid() {
		double[] a=model.getA();
		for(int i=0;i<a.length;i++){
			if(a[i] < 0.000001) return false; 
		}
		return true;
	}	
	private boolean isGValid(){
		if(model.getNCompartments()<2) return true;
		double[] g = model.getG();
		for(int i=0;i<g.length;i++)
			if(g[i]<0.01) return false;
		return true;
	}
	private boolean isCmValid(){
		double[] g = model.getcM();
		for(int i=0;i<g.length;i++)
			if(g[i]<10) return false;
		return true;
	}
	private boolean isDValid(){
		double[] g = model.getD();
		for(int i=0;i<g.length;i++)
			if(g[i]<0) return false;
		return true;
	}
	
	public boolean isDisplayAll() {
		return displayAll;
	}

	public void setDisplayAll(boolean displayAll) {
		this.displayAll = displayAll;
	}
	public void setDisplayStatus(boolean displayStatus) {
		this.displayStatus = displayStatus;
	}
	
	public boolean isDisplayForPlot() {
		return displayForPlot;
	}
	public void setDisplayForPlot(boolean displayForPlot) {
		this.displayForPlot = displayForPlot;
	}
	public int getDisplayForPlotIdx() {
		return displayForPlotIdx;
	}
	public void setDisplayForPlotIdx(int displayForPlotIdx) {
		this.displayForPlotIdx = displayForPlotIdx;
	}
	private double getNextSomaticCurrent(){
		return this.somaticCurrents[this.somCurrIdx++];
	}

	public MultiCompConstraintEvaluator getMcEvalholder() {
		return mcEvalholder;
	}

	public void setMcEvalholder(MultiCompConstraintEvaluator mcEvalholder) {
		this.mcEvalholder = mcEvalholder;
	}

	public List<SpikePatternAdapting[]> getModelSpikePatternHolder() {
		return modelSpikePatternHolder;
	}
/*
	public void setModelSomaSpikePatternHolder(
			SpikePatternAdapting modelSomaSpikePatternHolder) {
		this.modelSomaSpikePatternHolder = modelSomaSpikePatternHolder;
	}*/
	public void addModelSpikePatternHolder(
			SpikePatternAdapting[] modelSpikePatternHolder) {
		if(this.modelSpikePatternHolder==null) {
			this.modelSpikePatternHolder=new ArrayList<SpikePatternAdapting[]>();
		}
		this.modelSpikePatternHolder.add(modelSpikePatternHolder);
	}

	public boolean isRampRheo() {
		return rampRheo;
	}

	public void setRampRheo(boolean rampRheo) {
		this.rampRheo = rampRheo;
	}


	public boolean isDisplayOnlyClass() {
		return displayOnlyClass;
	}


	public void setDisplayOnlyClass(boolean displayOnlyClass) {
		this.displayOnlyClass = displayOnlyClass;
	}


	public SpikePatternEvaluatorV2 getSpEvalHolder() {
		return spEvalHolder;
	}


	public void setSpEvalHolder(SpikePatternEvaluatorV2 spEvalHolder) {
		this.spEvalHolder = spEvalHolder;
		this.spEvalHolders.add(spEvalHolder);
	}


	public PhenotypeConstraintEvaluator getPtEvalHolder() {
		return ptEvalHolder;
	}


	public void setPtEvalHolder(PhenotypeConstraintEvaluator ptEvalHolder) {
		this.ptEvalHolder = ptEvalHolder;
	}


	public boolean isSaveModelPattern() {
		return saveModelPattern;
	}


	public void setSaveModelPattern(boolean saveModelPattern) {
		this.saveModelPattern = saveModelPattern;
	}
	
	
}
