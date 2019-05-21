/*
  Portions copyright 2010 by Sean Luke, Robert Hubley, and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.app.izhikevich;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.LineNumberReader;

import ec.EvolutionState;
import ec.Fitness;
import ec.multiobjective.MultiObjectiveFitness;
import ec.util.Code;

/* 
 * SPEA2MultiObjectiveFitness.java
 * 
 * Created: Sat Oct 16 11:24:43 EDT 2010
 * By: Sean Luke
 * Replaces earlier class by: Robert Hubley, with revisions by Gabriel Balan and Keith Sullivan
 */

/**
 * SPEA2MultiObjectiveFitness is a subclass of MultiObjectiveFitness which adds three auxiliary fitness
 * measures used in SPEA2: strength S(i), kthNNDistance D(i), and a final fitness value R(i) + D(i).  
 * Note that so-called "raw fitness" (what Sean calls "Wimpiness" in Essentials of Metaheuristics) is 
 * not retained.
 * 
 * <p>The fitness comparison operators solely use the 'fitness' value R(i) + D(i).
 */

public class IzhikevichMultiObjectiveFitness extends MultiObjectiveFitness
    {
    public static final String Izhi_FITNESS_PREAMBLE = "Fitness: ";
    public static final String Izhi_obj1_PREAMBLE = "Obj-1: ";
    public static final String Izhi_obj2_PREAMBLE = "Obj-2: ";
    public static final String Izhi_obj3_PREAMBLE = "Obj-3: ";
    
    public String[] getAuxilliaryFitnessNames() { return new String[] { "Obj-1", "Obj-2", "Obj-3", "Raw Fitness" }; }
    public double[] getAuxilliaryFitnessValues() { return new double[] { obj1, obj2, obj3, fitness  }; }
        
    /** SPEA2 strength (# of nodes it dominates) */
    public double obj1; // S(i)

    /** SPEA2 NN distance */
    public double obj2; // D(i)

    public double obj3;
    
    /** Final SPEA2 fitness.  Equals the raw fitness R(i) plus the kthNNDistance D(i). */
    public double fitness;

    public String fitnessToString()
        {
        return super.fitnessToString() + "\n" + Izhi_FITNESS_PREAMBLE + Code.encode(fitness) 
        			+ "\n" + Izhi_obj1_PREAMBLE + Code.encode(obj1) 
        			+ "\n" + Izhi_obj2_PREAMBLE + Code.encode(obj2)
        			+ "\n" + Izhi_obj3_PREAMBLE + Code.encode(obj3);
        }

    public String fitnessToStringForHumans()
        {
        return super.fitnessToStringForHumans() + 
        		"\n" + Izhi_obj1_PREAMBLE + obj1 + 
        		"\n" + Izhi_obj2_PREAMBLE + obj2 + 
        		"\n" + Izhi_obj3_PREAMBLE + obj3 + 
        		"\n" + Izhi_FITNESS_PREAMBLE + fitness;
        }

    public void readFitness(final EvolutionState state, final LineNumberReader reader) throws IOException
        {
        super.readFitness(state, reader);
        fitness = Code.readDoubleWithPreamble(Izhi_FITNESS_PREAMBLE, state, reader);
        obj1 = Code.readDoubleWithPreamble(Izhi_obj1_PREAMBLE, state, reader);
        obj2 = Code.readDoubleWithPreamble(Izhi_obj2_PREAMBLE, state, reader);
        obj3 = Code.readDoubleWithPreamble(Izhi_obj3_PREAMBLE, state, reader);
        }

    public void writeFitness(final EvolutionState state, final DataOutput dataOutput) throws IOException
        {
        super.writeFitness(state, dataOutput);
        dataOutput.writeDouble(fitness);
        dataOutput.writeDouble(obj1);
        dataOutput.writeDouble(fitness);
        dataOutput.writeDouble(obj2);
        dataOutput.writeDouble(fitness);
        dataOutput.writeDouble(obj3);
        writeTrials(state, dataOutput);
        }

    public void readFitness(final EvolutionState state, final DataInput dataInput) throws IOException
        {
        super.readFitness(state, dataInput);
        fitness = dataInput.readDouble();
        obj1 = dataInput.readDouble();
        fitness = dataInput.readDouble();
        obj2 = dataInput.readDouble();
        fitness = dataInput.readDouble();
        obj3 = dataInput.readDouble();
        readTrials(state, dataInput);
        }
	

    /**
     * The selection criteria in SPEA2 uses the computed fitness, and not
     * pareto dominance.
     */
    /*public boolean equivalentTo(Fitness _fitness){
	    	double obj1 = ((IzhikevichMultiObjectiveFitness)_fitness).obj1;
	    	double obj2 = ((IzhikevichMultiObjectiveFitness)_fitness).obj2;
	    	if((this.obj1 == obj1 && this.obj2 == obj2)
	    						||
    			(this.obj1 > obj1 && this.obj2 < obj2)
	    						||
    			(this.obj1 < obj1 && this.obj2 > obj2)	
    			){
	    		return true;
	    	}else
	    		return false;
	       // return fitness == ((IzhikevichMultiObjectiveFitness)_fitness).fitness;
    }

    /**
     * The selection criteria in SPEA2 uses the computed fitness, and not
     * pareto dominance.
     */
    /*
    public boolean betterThan(Fitness _fitness){
	    	double obj1 = ((IzhikevichMultiObjectiveFitness)_fitness).obj1;
	    	double obj2 = ((IzhikevichMultiObjectiveFitness)_fitness).obj2;
	    	if(  	((this.obj1 > obj1) && !(this.obj2 < obj2))
	    								||
	    			(!(this.obj1 < obj1) && (this.obj2 > obj2)) 
	    		){
	    		return true;
	    	}else
	    		return false;
	    	//return fitness > ((IzhikevichMultiObjectiveFitness)_fitness).fitness;
        }
    */
    }
