package ec.app.izhikevich.model.nineparmgenes.dynamicconstraints;

public class GAGenesWConstraints {
	
	private float[] constraintGenes;
	
	public GAGenesWConstraints(float[] genes, int startIdxForConst) {
		constraintGenes = new float[genes.length - startIdxForConst];
		for(int i=startIdxForConst;i<genes.length;i++) {
			constraintGenes[i-startIdxForConst] = genes[i];
		}
	}
	
	public float[] getConstraintGenes() {
		return constraintGenes;
	}
}
