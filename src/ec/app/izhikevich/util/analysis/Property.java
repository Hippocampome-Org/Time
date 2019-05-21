package ec.app.izhikevich.util.analysis;

import java.util.List;

import ec.app.izhikevich.inputprocess.InputSpikePatternConstraint;

public class Property {

	public double excitability;
	public double excitability_rb;
	private InputSpikePatternConstraint[] constraints; //implemented, but order matching not done at this level. done in entry.java
														// meaning -> DO NOT MATCH it with fitFileStructure, although the length is the same
	private FitFileStructure[] fitFileStructure;
	
	public double delayFactor;
	public double stutFactor;
	public double slope;
	public double rbDelay;
	//Region region;
	
	List<String> xppFile;
	List<String> fitFile;
	List<String> csvFile;
	
	public void setInputSpikePatternConstraints(InputSpikePatternConstraint[] constraints) {
		this.constraints=constraints;
		fitFileStructure = new FitFileStructure[constraints.length];
	}
	
	public InputSpikePatternConstraint[] getInputSpikePatternConstraints() {
		return this.constraints;
	}
	
	public FitFileStructure[] getFitFileStructure() {
		return fitFileStructure;
	}
}
