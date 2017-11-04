package ec.app.izhikevich.evaluator.qualifier;

import ec.app.izhikevich.model.Izhikevich9pModelMC;
import ec.app.izhikevich.model.IzhikevichSolverMC;
import ec.app.izhikevich.spike.SpikePatternAdapting;
import ec.app.izhikevich.util.ModelFactory;

public class ModelSpikePatternClassifier {

	public static void main(String[] args) {
		Izhikevich9pModelMC model = ModelFactory.getUserDefined2cModel();
		model.setInputParameters(new double[]{312, 0}, 0, 2000);
		IzhikevichSolverMC solver = new IzhikevichSolverMC(model);
		IzhikevichSolverMC.RECORD_U = true;
		SpikePatternAdapting spattern = solver.solveAndGetSpikePatternAdapting()[0];
		spattern.displayISIs();
		/*
		Izhikevich9pModel model = ModelFactory.getUserDefinedModel();

		model.setInputParameters(770, 0, 600);
		IzhikevichSolver solver = new IzhikevichSolver(model);
		IzhikevichSolver.RECORD_U = true;
		SpikePatternAdapting spattern = solver.getSpikePatternAdapting();
		spattern.displayISIs();
		SpikePatternClassifier classifier = new SpikePatternClassifier(spattern);
		classifier.classifySpikePattern(1, true);
		System.out.println(classifier.getSpikePatternClass());
		*/
	}

}
