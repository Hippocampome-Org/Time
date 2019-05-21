package ec.app.izhikevich.spike.labels;

import java.util.List;

public enum Phenotype {
	PSTUT(1, "Persistent Interrupted Spiking"),
	TSTUT(2, "Non-Persistent Interrupted Spiking"),
	
	D(3, "Delayed Spiking"),
	NASP(4, "Non-Adapting Spiking"),
	
	RASP(5, "Rapidly Adapting Spiking"),	
	ASP_SLN(6, "Discontinuous Adapting Spiking"),	
	ASP(7, "Simple Adapting Spiking"),
	ASP_NASP(8, "Adapting - Non-Adapting Spiking"),
	
	UNIDENTIFIED(10, "Unidentified");
	
	String phenTypeName;
	int identifier;
	Phenotype(int id, String phen_name){
		phenTypeName=phen_name;
		identifier = id;
	}
	public String getPhenotypeName() {
		return phenTypeName;
	}
	public int getIdentifier() {
		return identifier;
	}
	/*
	 * order of checks important!
	 */
	public static Phenotype getPhenotype(String pattern_class) {	
		SpikePatternClass patternClass = new SpikePatternClass(pattern_class, ".");
		if(patternClass.contains(SpikePatternComponent.PSTUT) || patternClass.contains(SpikePatternComponent.PSWB)) {
			return Phenotype.PSTUT;
		}
			
		if(patternClass.contains(SpikePatternComponent.TSTUT) || patternClass.contains(SpikePatternComponent.TSWB)) {
			return Phenotype.TSTUT;
		}		

		if(patternClass.contains(SpikePatternComponent.D))
			return Phenotype.D;
		
		if(patternClass.equals(new SpikePatternClass("NASP", ".")))
			return Phenotype.NASP;
		
		if(patternClass.contains(SpikePatternComponent.RASP))
			return Phenotype.RASP;
		
		if(patternClass.equals(new SpikePatternClass("ASP.SLN.", ".")))
			return Phenotype.ASP_SLN;
		
		if(patternClass.equals(new SpikePatternClass("ASP.", ".")))
			return Phenotype.ASP;
		
		if(patternClass.equals(new SpikePatternClass("ASP.NASP.", ".")))
			return Phenotype.ASP_NASP;
		
		return Phenotype.UNIDENTIFIED;
	}
	
	public static Phenotype getPhenotype(List<String> pattern_classes) {	
		SpikePatternClass[] patternClass = new SpikePatternClass[pattern_classes.size()];
		for(int i=0;i<patternClass.length;i++) {
			patternClass[i] = new SpikePatternClass(pattern_classes.get(i), ".");
		}
		return getPhenotype(patternClass);	
	}
	
	public static Phenotype getPhenotype(SpikePatternClass[] patternClass) {
		if(hasPSTUT(patternClass)) return Phenotype.PSTUT;
		if(hasTSTUT(patternClass)) return Phenotype.TSTUT;
		if(hasD(patternClass)) return Phenotype.D;
		if(hasRASP(patternClass)) return Phenotype.RASP;
		
		if(hasASP_SLN(patternClass)) return Phenotype.ASP_SLN;
		if(hasASP_NASP(patternClass)) return Phenotype.ASP_NASP;
		
		if(isASP(patternClass)) return Phenotype.ASP;
		if(isNASP(patternClass)) return Phenotype.NASP;
		
		return Phenotype.UNIDENTIFIED;
	}
	public static boolean hasPSTUT(SpikePatternClass[] patternClasses) {
		boolean hasPSTUT = false;
		for(int i=0;i<patternClasses.length;i++) {
			if(patternClasses[i].contains(SpikePatternComponent.PSTUT) || patternClasses[i].contains(SpikePatternComponent.PSWB)) {
				hasPSTUT=true;
			}
		}
		return hasPSTUT;
	}
	
	public static boolean hasTSTUT(SpikePatternClass[] patternClasses) {
		boolean hasPSTUT = false;
		for(int i=0;i<patternClasses.length;i++) {
			if(patternClasses[i].contains(SpikePatternComponent.TSTUT) || patternClasses[i].contains(SpikePatternComponent.TSWB)) {
				hasPSTUT=true;
			}
		}
		return hasPSTUT;
	}
	
	public static boolean hasD(SpikePatternClass[] patternClasses) {
		boolean hasPSTUT = false;
		for(int i=0;i<patternClasses.length;i++) {
			if(patternClasses[i].contains(SpikePatternComponent.D)) {
				hasPSTUT=true;
			}
		}
		return hasPSTUT;
	}
	
	public static boolean hasRASP(SpikePatternClass[] patternClasses) {
		boolean hasPSTUT = false;
		for(int i=0;i<patternClasses.length;i++) {
			if(patternClasses[i].contains(SpikePatternComponent.RASP)) {
				hasPSTUT=true;
			}
		}
		return hasPSTUT;
	}
	
	public static boolean hasASP_SLN(SpikePatternClass[] patternClasses) {
		boolean hasPSTUT = false;
		for(int i=0;i<patternClasses.length;i++) {
			if(patternClasses[i].equals(new SpikePatternClass("ASP.SLN.", "."))) {
				hasPSTUT=true;
			}
		}
		return hasPSTUT;
	}
	
	public static boolean hasASP_NASP(SpikePatternClass[] patternClasses) {
		boolean hasPSTUT = false;
		for(int i=0;i<patternClasses.length;i++) {
			if(patternClasses[i].equals(new SpikePatternClass("ASP.NASP.", "."))) {
				hasPSTUT=true;
			}
		}
		return hasPSTUT;
	}
	
	public static boolean isASP(SpikePatternClass[] patternClasses) {
		boolean isASP = false;
		for(int i=0;i<patternClasses.length;i++) {
			if(patternClasses[i].equals(new SpikePatternClass("ASP.", "."))) {
				isASP=true;
			}
		}
		return isASP;
	}
	
	public static boolean isNASP(SpikePatternClass[] patternClasses) {
		boolean isASP = false;
		for(int i=0;i<patternClasses.length;i++) {
			if(patternClasses[i].equals(new SpikePatternClass("NASP.", "."))) {
				isASP=true;
			}
		}
		return isASP;
	}
}
