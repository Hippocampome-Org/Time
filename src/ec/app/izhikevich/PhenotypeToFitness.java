package ec.app.izhikevich;

import ec.EvolutionState;
import ec.Individual;
import ec.app.izhikevich.evaluator.ModelEvaluatorMC;
import ec.app.izhikevich.evaluator.MultiCompConstraintEvaluator;
import ec.app.izhikevich.model.Izhikevich9pModel1C;
import ec.app.izhikevich.model.Izhikevich9pModel3C;
import ec.app.izhikevich.model.Izhikevich9pModel3C_L2;
import ec.app.izhikevich.model.Izhikevich9pModel4C;
import ec.app.izhikevich.model.Izhikevich9pModelMC;
import ec.app.izhikevich.model.neurontypes.mc.EAGenes;
import ec.app.izhikevich.outputprocess.CarlMcSimData;
import ec.app.izhikevich.outputprocess.CarlOutputParser;
import ec.app.izhikevich.outputprocess.CarlSpikePattern;
import ec.app.izhikevich.starter.ECJStarterV2;
import ec.simple.SimpleFitness;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;
import ecjapp.eval.problem.objective.ObjectiveFunction;
import ecjapp.util.Misc;
import ecjapp.util.Option;

public class PhenotypeToFitness implements ObjectiveFunction<SimpleFitness>  {
public final static String P_IDEAL_FITNESS_VALUE = "idealFitnessValue";    
	private static final String PHENOTYPE_FILE_PFIX = "";
    private Option<Double> idealFitnessValue = Option.NONE;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        if (state == null)
            throw new IllegalArgumentException(this.getClass().getSimpleName() + ": state is null.");
        if (state.parameters == null)
            throw new IllegalArgumentException(this.getClass().getSimpleName() + ": state.parameters is null.");
        if (base == null)
            throw new IllegalArgumentException(this.getClass().getSimpleName() + ": base is null.");
        idealFitnessValue = new Option<Double>(state.parameters.getDouble(base.push(P_IDEAL_FITNESS_VALUE), null));
    }
    
    @Override
    public SimpleFitness evaluate(final EvolutionState state, Individual ind, final String phenotypeFileName) {        
    	//System.out.println("entry to evaluate..");
    	String fileName = PHENOTYPE_FILE_PFIX+phenotypeFileName;
    	if (!(ind instanceof DoubleVectorIndividual))
            state.output.fatal("Whoa!  It's not a DoubleVectorIndividual!!!",null);
        
    	
        DoubleVectorIndividual ind2 = (DoubleVectorIndividual)ind;    
       // System.out.println("before i1.5..");
        EAGenes genes = new EAGenes(ind2.genome, ECJStarterV2.iso_comp);   
        //System.out.println("before i2..");
        
        float fitness = evaluateInd(genes, fileName);
        final boolean isIdeal = idealFitnessValue.isDefined() ? Misc.doubleEquals(fitness, idealFitnessValue.get()) || fitness > idealFitnessValue.get() : false;
                                
        final SimpleFitness _fitness = new SimpleFitness();
        _fitness.setFitness(state, fitness, isIdeal);
        
        //(new File(fileName)).delete();
        return _fitness;
    }
    
   
    private float evaluateInd(EAGenes genes, String phenotypeFileName){
    	//System.out.println("entry to evaluateInd..");
    	//genes.display(); System.out.println(phenotypeFileName);
    	
		Izhikevich9pModelMC model = getRightInstanceForModel(); 
        model.setK(genes.getK());
		model.setA(genes.getA());
		model.setB(genes.getB());
		model.setD(genes.getD());	
		model.setcM(genes.getCM());
		model.setvR(genes.getVR());
		model.setvT(genes.getVT());		
		model.setvMin(genes.getVMIN());//		
        model.setvPeak(genes.getVPEAK());

        model.setG(genes.getG()); 
        model.setP(genes.getP());
        
        double[] currents = genes.getI();
        double[] weight = genes.getW();
      
        //PhenotypeParser parser = new PhenotypeParser(phenotype, model.getNCompartments());        
        CarlOutputParser parser = new CarlOutputParser(phenotypeFileName);          
		CarlSpikePattern[] carlSpikePatterns = parser.extractCarlSomaPatterns();		
		/*
		 * carlSpikePatterns should map to input_spike_pattern_cons array below
		 */
	
		CarlMcSimData carlMcSimData = parser.extractCarlMcSimData();
		//parser.deletePhenotypeFile();
		ModelEvaluatorMC evaluator = new ModelEvaluatorMC(model,
        		ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS,
        		ModelEvaluatorWrapper.INPUT_PAT_REP_WEIGHTS,
        		ModelEvaluatorWrapper.INPUT_MC_CONS,
        								currents,
        								weight,
        								carlSpikePatterns,
        								carlMcSimData);
		//System.out.println("after evaluator instance creation..");
         //evaluator.setDisplayStatus(true);
        return evaluator.getFitness(); 
	}
	
	private float[] evaluateIndForMultiObj(EAGenes genes){
		Izhikevich9pModelMC model = getRightInstanceForModel(); 
        model.setK(genes.getK());
		model.setA(genes.getA());
		model.setB(genes.getB());
		model.setD(genes.getD());	
		model.setcM(genes.getCM());
		model.setvR(genes.getVR());
		model.setvT(genes.getVT());		
		model.setvMin(genes.getVMIN());//		
        model.setvPeak(genes.getVPEAK());

        model.setG(genes.getG()); 
        model.setP(genes.getP());
        
        double[] currents = genes.getI();
        double[] weight = genes.getW();
      
        ModelEvaluatorMC evaluator = new ModelEvaluatorMC(model,
        		ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS,
        		ModelEvaluatorWrapper.INPUT_PHENOTYPE_CONSTRAINT,
        		ModelEvaluatorWrapper.INPUT_PAT_REP_WEIGHTS,
        		ModelEvaluatorWrapper.INPUT_MC_CONS,
        								currents,
        								weight);
        
         //evaluator.setDisplayStatus(true);
        return evaluator.getMultiObjFitnesses(); 
	}
	
	private Izhikevich9pModelMC getRightInstanceForModel(){
		if(EAGenes.nComps==1){
			return new Izhikevich9pModel1C(1);
		}
		if(EAGenes.nComps==2){
			return new Izhikevich9pModelMC(2);
		}
		if(EAGenes.nComps==3){
			if(MultiCompConstraintEvaluator.forwardConnectionIdcs[2]==0)
				return new Izhikevich9pModel3C(3);
			else
				return new Izhikevich9pModel3C_L2(3);
		}
		if(EAGenes.nComps==4){
			return new Izhikevich9pModel4C(4);
		}
		System.out.println("rightModel needs to be instantiated!!--ModelEvaluatorWrapper.java");
		return null;	
	}	
	 
}
/*
class PhenotypeParser{
	private final String CONS_SEP = "+";
	private final String COMP_SEP = "=";
	private final String FEAT_SEP =	"?";
	
	private String phenotype;
	private int nComp;
	PhenotypeParser(String phenotype, int nComp){
		this.phenotype = phenotype;
		this.nComp = nComp;
	}
	
	ODESolution[][] separateConstraints() {
		ODESolution[][] odeSolutions = new ODESolution[ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS.length][];
		StringTokenizer st = new StringTokenizer(phenotype, CONS_SEP);
		for(int i=0;i<odeSolutions.length;i++){
			odeSolutions[i] = separateCompartments(st.nextToken());
		}
		return odeSolutions;
	}
	
	ODESolution[] separateCompartments(String consPhenotype) {
		ODESolution[] odeSolution = new ODESolution[nComp];
		StringTokenizer st = new StringTokenizer(consPhenotype, COMP_SEP);
		for(int i=0;i<nComp;i++){
			odeSolution[i]=separateFeatures(st.nextToken());
		}		
		return odeSolution;
	}
	ODESolution separateFeatures(String compPhenotype){		
		StringTokenizer st = new StringTokenizer(compPhenotype, FEAT_SEP);
		String timeString = st.nextToken();
		String voltString = st.nextToken();
		String spikeTimesString = st.nextToken();
		
		return new ODESolution(
				stringToDouble(timeString), 
				stringToDouble(voltString), 
				stringToDouble(spikeTimesString));
	}
	
	private double[] stringToDouble(String str){		
		ArrayList<Double> vals = new ArrayList<>();
		StringTokenizer st = new StringTokenizer(str, " ");
		while(st.hasMoreTokens()){
			vals.add(Double.valueOf(st.nextToken()));
		}
		return GeneralUtils.listToArrayDouble(vals);
	}
}
*/