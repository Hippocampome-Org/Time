package ec.app.izhikevich.spike.labels;

public enum Phenotype_v2 {
	RD(1, "RBS_DEL"),
	_D(2, "_DEL"),
	
	RC(3, "RBS_CONT"),
	_C(4, "_CONT"),
	
	RI(5, "RBS_INT"),	
	_I(6, "_INT"),
	
	UNIDENTIFIED(10, "Unidentified");
	
	String phenTypeName;
	int identifier;
	Phenotype_v2(int id, String phen_name){
		phenTypeName=phen_name;
		identifier = id;
	}
	public String getPhenotypeName() {
		return phenTypeName;
	}
	public int getIdentifier() {
		return identifier;
	}
	
	
	
	
	public static Phenotype_v2 getPhenotype(SpikePatternClass[] patternClass) {
		
		if(patternClass[1].contains(SpikePatternComponent.PSTUT) 
				|| patternClass[1].contains(SpikePatternComponent.PSWB)) {
			
			if(patternClass[0].contains(SpikePatternComponent.RBS)) {
				return Phenotype_v2.RI;
			}else {
				return Phenotype_v2._I;
			}
			
		}else if(patternClass[1].contains(SpikePatternComponent.D)) {
			
			if(patternClass[0].contains(SpikePatternComponent.RBS)) {
				return Phenotype_v2.RD;
			}else {
				return Phenotype_v2._D;
			}
			
		}else {
			
			if(patternClass[0].contains(SpikePatternComponent.RBS)) {
				return Phenotype_v2.RC;
			}else {
				return Phenotype_v2._C;
			}
			
		}	
	}
}
