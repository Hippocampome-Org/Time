package ec.app.izhikevich.starter;


/*
 * These EA gene parms are same across compartments with the exception of mutBounded,
 * which is handled in MCParamFile class (ECJSTARTER) addGene method. simple enough!!!
 * HOWEVER, the genes for 'I' will vary (min and max genes) for a neuron type.
 * Therefore, only for the genes 'I', use a different constructor!!!
 * 
 * 10/4 -- Also, vpeak should be less for dendrites. so use min max gene arrays for vpeak.
 */
public class EAParmsOfModelParm {
	
	private String minGene;
	private String maxGene;
	private String mutRate;
	private String mutType;
	private String mutSD;
	private String mutBounded;
	
	private String minGenes[];
	private String maxGenes[];	
	
	public EAParmsOfModelParm(String minGene,
					String maxGene,					
					String mutType,
					String mutRate,
					String mutSD,
					String mutBounded){
		this.setMinGene(minGene);
		this.setMaxGene(maxGene);
		this.setMutRate(mutRate);
		this.setMutType(mutType);
		this.setMutSD(mutSD);
		this.setMutBounded(mutBounded);
	}

	public EAParmsOfModelParm(String[] minGenes,
			String[] maxGenes,					
			String mutType,
			String mutRate,
			String mutSD,
			String mutBounded){
			this.setMinGenes(minGenes);
			this.setMaxGenes(maxGenes);
			this.setMutRate(mutRate);
			this.setMutType(mutType);
			this.setMutSD(mutSD);
			this.setMutBounded(mutBounded);
	}
	
	public String getMinGene() {
		return minGene;
	}

	public void setMinGene(String minGene) {
		this.minGene = minGene;
	}

	public String getMutRate() {
		return mutRate;
	}

	public void setMutRate(String mutRate) {
		this.mutRate = mutRate;
	}

	public String getMaxGene() {
		return maxGene;
	}

	public void setMaxGene(String maxGene) {
		this.maxGene = maxGene;
	}

	public String getMutType() {
		return mutType;
	}

	public void setMutType(String mutType) {
		this.mutType = mutType;
	}

	public String getMutBounded() {
		return mutBounded;
	}

	public void setMutBounded(String mutBounded) {
		this.mutBounded = mutBounded;
	}

	public String getMutSD() {
		return mutSD;
	}

	public void setMutSD(String mutSD) {
		this.mutSD = mutSD;
	}

	public String[] getMinGenes() {
		return minGenes;
	}

	public void setMinGenes(String minGenes[]) {
		this.minGenes = minGenes;
	}

	public String[] getMaxGenes() {
		return maxGenes;
	}

	public void setMaxGenes(String maxGenes[]) {
		this.maxGenes = maxGenes;
	}
	
}
