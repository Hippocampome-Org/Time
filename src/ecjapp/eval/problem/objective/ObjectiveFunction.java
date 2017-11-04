package ecjapp.eval.problem.objective;

import ec.EvolutionState;
import ec.Fitness;
import ec.Individual;
import ec.util.Parameter;

/**
 * An ObjectiveFunction takes a String representation of a simulation result
 * and assigns it a fitness value.
 * 
 * @author Eric 'Siggy' Scott
 */
public interface ObjectiveFunction<T extends Fitness> {
    void setup(EvolutionState state, Parameter base);
 //   T evaluate(EvolutionState state, String phenotype);
    T evaluate(EvolutionState state, Individual ind, String phenotype);
}
