/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.izhikevich;
import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.app.izhikevich.evaluator.ModelEvaluatorMC;
import ec.app.izhikevich.inputprocess.InputMCConstraint;
import ec.app.izhikevich.inputprocess.InputModelParameterRanges;
import ec.app.izhikevich.inputprocess.InputSpikePatternConstraint;
import ec.app.izhikevich.inputprocess.PhenotypeConstraint;
import ec.app.izhikevich.inputprocess.labels.PatternFeatureID;
import ec.app.izhikevich.model.Izhikevich9pModel;
import ec.app.izhikevich.model.Izhikevich9pModel1C;
import ec.app.izhikevich.model.Izhikevich9pModel3C;
import ec.app.izhikevich.model.Izhikevich9pModel4C;
import ec.app.izhikevich.model.Izhikevich9pModelMC;
import ec.app.izhikevich.model.neurontypes.mc.EAGenes;
import ec.app.izhikevich.resonate.Bifurcation;
import ec.app.izhikevich.starter.ECJStarterV2;
import ec.simple.SimpleFitness;
import ec.simple.SimpleProblemForm;
import ec.vector.DoubleVectorIndividual;

public class ModelEvaluatorWrapper extends Problem implements SimpleProblemForm
    {
	public static InputModelParameterRanges INPUT_MODEL_PARAMETER_RANGES = null;
	public static InputSpikePatternConstraint[] INPUT_SPIKE_PATTERN_CONS = null;
	public static PhenotypeConstraint[] INPUT_PHENOTYPE_CONSTRAINT = null;
	
	public static InputMCConstraint[] INPUT_MC_CONS = null;
	public static double[] INPUT_PAT_REP_WEIGHTS = null;
	public static boolean ISO_COMPS = false;
	
	public void evaluate(final EvolutionState state,
        final Individual ind,
        final int subpopulation,
        final int threadnum)
        {
        if (ind.evaluated) return;
       
        if (!(ind instanceof DoubleVectorIndividual))
            state.output.fatal("Whoa!  It's not a DoubleVectorIndividual!!!",null);
        
        DoubleVectorIndividual ind2 = (DoubleVectorIndividual)ind;     
        EAGenes genes = new EAGenes(ind2.genome, ISO_COMPS);
        
        if(!ECJStarterV2.MULTI_OBJ)	{
        	//SpikePatternClassifier mq = ;
        	float fitness=-Float.MAX_VALUE;
        	try{
        		if(INPUT_SPIKE_PATTERN_CONS[0].getFeaturesToEvaluate().contains(PatternFeatureID.period)){
        			fitness = evaluateIndForPeriod(genes);
        		}else{
        			fitness = evaluateInd(genes); 
        		}
        		  
        	}
        	catch(Exception e){
        		e.printStackTrace();
        	}
        	boolean shouldStop = false;//mq.doesQualify();
        	/*if(shouldStop){
        		fitness = SpikePatternClassifier.SHADOW_FITNESS;
        	}*/
            if (!(ind2.fitness instanceof SimpleFitness))
                state.output.fatal("Whoa!  It's not a SimpleFitness!!!",null);
            ((SimpleFitness)ind2.fitness).setFitness(state,
                /// ...the fitness...
                fitness,
                ///... is the individual ideal?  Indicate here...
                shouldStop);
            ind2.evaluated = true;
        	}else{
        		float[] fitnesses_float = evaluateIndForMultiObj(genes);   
        		double[] fitnesses = new double[fitnesses_float.length];
        		for(int i=0;i<fitnesses.length;i++){
        			fitnesses[i]=fitnesses_float[i];
        		}
        		//System.out.println(fitnesses[0]);
        		
        		if(fitnesses.length==3)
	                {
	        		if (!(ind2.fitness instanceof IzhikevichMultiObjectiveFitness))
	                    state.output.fatal("Whoa!  It's not a IzhikevichMultiObjectiveFitness!!!",null);
	                ((IzhikevichMultiObjectiveFitness)ind2.fitness).setObjectives(state, fitnesses);	
	                
	              //  ((IzhikevichMultiObjectiveFitness)ind2.fitness).
	                
	                ((IzhikevichMultiObjectiveFitness)ind2.fitness).obj1 =  fitnesses[0];
	                ((IzhikevichMultiObjectiveFitness)ind2.fitness).obj2 =  fitnesses[1];
	                ((IzhikevichMultiObjectiveFitness)ind2.fitness).obj3 =  fitnesses[2];
	         //       ((IzhikevichMultiObjectiveFitness)ind2.fitness).fitness = fitnesses[0]+ fitnesses[1] + fitnesses[2];
	                
	                ind2.evaluated = true;
	                }
                if(fitnesses.length==2)
	                {
	        		if (!(ind2.fitness instanceof IzhikevichMultiObjectiveFitness))
	                    state.output.fatal("Whoa!  It's not a IzhikevichMultiObjectiveFitness!!!",null);
	                ((IzhikevichMultiObjectiveFitness)ind2.fitness).setObjectives(state, fitnesses);
	                ((IzhikevichMultiObjectiveFitness)ind2.fitness).obj1 =  fitnesses[0];
	                ((IzhikevichMultiObjectiveFitness)ind2.fitness).obj2 =  fitnesses[1];
	      //          ((IzhikevichMultiObjectiveFitness)ind2.fitness).fitness = fitnesses[0]+ fitnesses[1];
	                ind2.evaluated = true;
	                }
                if(fitnesses.length==1) {
	        		if (!(ind2.fitness instanceof IzhikevichMultiObjectiveFitness))
	                    state.output.fatal("Whoa!  It's not a IzhikevichMultiObjectiveFitness!!!",null);
	                ((IzhikevichMultiObjectiveFitness)ind2.fitness).setObjectives(state, fitnesses);
	                ((IzhikevichMultiObjectiveFitness)ind2.fitness).obj1 =  fitnesses[0];	               
	                ((IzhikevichMultiObjectiveFitness)ind2.fitness).fitness = fitnesses[0];
	                ind2.evaluated = true;
	                }
               }
        genes = null;
        
        }
	
	private float evaluateInd(EAGenes genes){
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
      /*  
        System.out.println(ECJStarterSlave.count++);
        
        try {
            Thread.sleep(2000);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        */
        ModelEvaluatorMC evaluator = new ModelEvaluatorMC(model,
        								INPUT_SPIKE_PATTERN_CONS,
        								INPUT_PHENOTYPE_CONSTRAINT,
        								INPUT_PAT_REP_WEIGHTS,
        								INPUT_MC_CONS,
        								currents,
        								weight);
   //   evaluator.setDisplayStatus(true);
        float fitness = evaluator.getFitness();
        evaluator = null;
        model = null;
       return fitness;
        
	}
	
	private float evaluateIndForPeriod(EAGenes genes){
		Izhikevich9pModel model = new Izhikevich9pModel();	
		
		model.setK(genes.getK()[0]);
		model.setA(genes.getA()[0]);
		model.setB(genes.getB()[0]);
		model.setD(genes.getD()[0]);	
		model.setcM(genes.getCM()[0]);
		model.setvR(genes.getVR());
		model.setvT(genes.getVT()[0]);		
		model.setvMin(genes.getVMIN()[0]);	
        model.setvPeak(genes.getVPEAK()[0]);
        
        
        Bifurcation bf = new Bifurcation(model, genes.getI()[0]);		
		int period = bf.identifyPeriod(bf.cutSSPcareU(model.getvPeak()-20));
		double freq = bf.getSpattern().getFiringFrequencyBasedOnISIs();
		float normFactor;
		if(freq<.1d){
			normFactor = 1f;
		}else{
			normFactor = (float)Math.log10(freq);
		}
		bf=null;
		return (period*1f/normFactor);        
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
      /*  
        System.out.println(ECJStarterSlave.count++);
        
        try {
            Thread.sleep(2000);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        */
        ModelEvaluatorMC evaluator = new ModelEvaluatorMC(model,
        								INPUT_SPIKE_PATTERN_CONS,
        								INPUT_PHENOTYPE_CONSTRAINT,
        								INPUT_PAT_REP_WEIGHTS,
        								INPUT_MC_CONS,
        								currents,
        								weight);
        
         //evaluator.setDisplayStatus(true);
        float[] fitness = evaluator.getMultiObjFitnesses();
        evaluator = null;
        model = null; 
       return fitness;
       
	}
	
	private Izhikevich9pModelMC getRightInstanceForModel(){
		if(EAGenes.nComps==1){
			return new Izhikevich9pModel1C(1);
		}
		if(EAGenes.nComps==2){
			return new Izhikevich9pModelMC(2);
		}
		if(EAGenes.nComps==3){
			return new Izhikevich9pModel3C(3);
		}
		if(EAGenes.nComps==4){
			return new Izhikevich9pModel4C(4);
		}
		System.out.println("rightModel needs to be instantiated!!--ModelEvaluatorWrapper.java");
		return null;	
	}
	
    }
