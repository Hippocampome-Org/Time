package ec.app.izhikevich.evaluator;

import ec.app.izhikevich.ModelEvaluatorWrapper;
import ec.app.izhikevich.inputprocess.InputSpikePatternConstraint;
import ec.app.izhikevich.model.Ermen;
import ec.app.izhikevich.model.ErmenSolver;
import ec.app.izhikevich.outputprocess.CarlSpikePattern;
import ec.app.izhikevich.spike.PatternType;
import ec.app.izhikevich.spike.SpikePatternAdapting;
import ec.app.izhikevich.util.DisplayUtilMcwSyn;

public class ModelEvaluatorMC2{
		
	Ermen model;	
	InputSpikePatternConstraint[] expSpikePatternData;	
	
	private boolean displayAll;
	private boolean displayStatus;
	private boolean displayForPlot;
	private int displayForPlotIdx;
	private boolean displayOnlyClass;
	
	private SpikePatternEvaluatorV2 spEvalHolder;
	private SpikePatternAdapting modelSomaSpikePatternHolder;

	
	/*
	 * Internal Simulator
	 */
	public ModelEvaluatorMC2(Ermen model, 
			InputSpikePatternConstraint[] expSpikePatternData
			) {
		if(expSpikePatternData==null) {
			System.out.println("NULL expSpikePatternData!!!");
			throw new IllegalStateException("NULL expSpikePatternData!!!");
		}
		
		this.model = model;
		this.expSpikePatternData = expSpikePatternData;	
		
		this.setDisplayAll(false);
		this.setDisplayForPlot(false);
		this.setDisplayOnlyClass(false);
		displayForPlotIdx = -1;
		
		modelSomaSpikePatternHolder = null;
	}
	

	public float getFitness() {	
		
		float fitness=0;
		float totPatternError = 0;
		float patternError = 0;
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
		
		
		fitness = - (totPatternError);		
		if(Float.isNaN(fitness)){fitness = -Float.MAX_VALUE;}
		if(displayAll) {System.out.print("\n\n<<o>> Fitness\t"+(fitness)+"\n");}
		
		
		return (fitness);
	}
	
	
	private float calculatePatternError(InputSpikePatternConstraint expSpikePatternData, int consIdx) {
		float patternError = Float.MAX_VALUE;
		
		PatternType type = expSpikePatternData.getType();
				
        ErmenSolver solver= new ErmenSolver(model, 0, 0);
        SpikePatternAdapting model_spike_pattern = solver.getSpikePatternAdapting();
        if(model_spike_pattern == null){
        	return patternError;
        }

        SpikePatternEvaluatorV2 evaluator = new SpikePatternEvaluatorV2(model_spike_pattern, 
																	expSpikePatternData,
																	ModelEvaluatorWrapper.INPUT_PAT_REP_WEIGHTS);
		
        evaluator.setCheckForPatternValidity(true);
		patternError = evaluator.calculatePatternError();
		
		this.setModelSomaSpikePatternHolder(model_spike_pattern);
		this.setSpEvalHolder(evaluator);
						
		solver = null;
		evaluator = null;
		model_spike_pattern = null;
		return patternError;
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
	
	public SpikePatternAdapting getModelSomaSpikePatternHolder() {
		return modelSomaSpikePatternHolder;
	}

	public void setModelSomaSpikePatternHolder(
			SpikePatternAdapting modelSomaSpikePatternHolder) {
		this.modelSomaSpikePatternHolder = modelSomaSpikePatternHolder;
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
	}
	
}
