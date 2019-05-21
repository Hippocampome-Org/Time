/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.izhikevich;
import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.app.izhikevich.evaluator.ModelEvaluatorMC2;
import ec.app.izhikevich.inputprocess.InputMCConstraint;
import ec.app.izhikevich.inputprocess.InputModelParameterRanges;
import ec.app.izhikevich.inputprocess.InputSpikePatternConstraint;
import ec.app.izhikevich.inputprocess.PhenotypeConstraint;
import ec.app.izhikevich.model.Ermen;
import ec.app.izhikevich.model.neurontypes.mc.EAGenes;
import ec.simple.SimpleFitness;
import ec.simple.SimpleProblemForm;
import ec.vector.DoubleVectorIndividual;

public class ModelEvaluatorWrapper2 extends Problem implements SimpleProblemForm
    {
	public void evaluate(final EvolutionState state,
        final Individual ind,
        final int subpopulation,
        final int threadnum)
        {
        if (ind.evaluated) return;
       
        if (!(ind instanceof DoubleVectorIndividual))
            state.output.fatal("Whoa!  It's not a DoubleVectorIndividual!!!",null);
        
        DoubleVectorIndividual ind2 = (DoubleVectorIndividual)ind;     
        EAGenes genes = new EAGenes(ind2.genome, ModelEvaluatorWrapper.ISO_COMPS);
        
       
        float fitness=-Float.MAX_VALUE;
        	try{
        		fitness = evaluateInd(ind2.genome);       		 
        	}
        	catch(Exception e){
        		e.printStackTrace();
        	}
        	boolean shouldStop = false;//mq.doesQualify();
        	
        	if(fitness > -0.00000001)
        		shouldStop = true;
        	
            if (!(ind2.fitness instanceof SimpleFitness))
                state.output.fatal("Whoa!  It's not a SimpleFitness!!!",null);
            ((SimpleFitness)ind2.fitness).setFitness(state,
                /// ...the fitness...
                fitness,
                ///... is the individual ideal?  Indicate here...
                shouldStop);
            ind2.evaluated = true;
        	
        genes = null;
        
        }
	
	private float evaluateInd(double[] genome){
		double tau_v = genome[0];
		double tau_w = genome[1];
		double m = genome[2];
		double n = genome[3];
		
		double a = genome[4];
		double b = genome[5];
		double c = genome[6];
		double d = genome[7];
		
		Ermen model = new Ermen(tau_v, tau_w, m,n, a,b,c,d);
        model.setInputParameters(600, 0, 750);
        ModelEvaluatorMC2 evaluator = new ModelEvaluatorMC2(model, ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS);
  
        float fitness = evaluator.getFitness();
        evaluator = null;
        model = null;
        return fitness;
        
	}
}
