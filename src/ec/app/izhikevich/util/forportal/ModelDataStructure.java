package ec.app.izhikevich.util.forportal;

import java.util.HashMap;
import java.util.Map;

import ec.app.izhikevich.evaluator.qualifier.SpikePatternClassifier;
import ec.app.izhikevich.inputprocess.labels.PatternFeatureID;
import ec.app.izhikevich.model.Izhikevich9pModel1C;
import ec.app.izhikevich.model.Izhikevich9pModelMC;
import ec.app.izhikevich.model.IzhikevichSolverMC;
import ec.app.izhikevich.outputprocess.CarlOutputParser;
import ec.app.izhikevich.spike.SpikePatternAdapting;
import ec.app.izhikevich.spike.labels.SpikePatternClass;
import ec.app.izhikevich.spike.labels.SpikePatternComponent;
import ec.app.izhikevich.util.GeneralUtils;
import ec.app.izhikevich.util.analysis.Property;
import ec.app.izhikevich.util.analysis.Region;

public class ModelDataStructure {
        
    public static Map<String, Map<String, String>> UniqueID_to_SubtypeID_to_ReplacementID;
    
	public String uniqueID;
	public String neuronSubtypeID;
	public String name; 
	public String preferredCondition;
	
	public Izhikevich9pModelMC model;
	public double[] Is;
	public double[] Idurs;// populate later
	public double[] weights;//populate later?
	
	public float[] errorSC;
	public float[] featCount;
	public float[] errorMC;
	
	public Map<PatternFeatureID, Double>[] featErrors;
	
	public String S; //soma=0 or dend=1,2,3
	
	public String RBS; // "0" or "1"
	public String D;
	public String ASP;
	public String PSTUT;
	public String NASP;
	
	public Property property;
	private CarlOutputParser carlOutputParser;
	private String mcLayoutCode;
	
	static {
		UniqueID_to_SubtypeID_to_ReplacementID = new HashMap<>();
	}
	
	public ModelDataStructure(String uniqueID, String subtypeID, String name, Izhikevich9pModelMC model, double[] Is) {
		this.uniqueID = uniqueID;
		this.neuronSubtypeID=subtypeID;
		this.name = name;
		this.model=model;
		this.Is=Is;
		this.errorSC = new float[Is.length];
		this.featCount = new float[Is.length];
		this.errorMC = new float[Is.length];
		//this.Idurs=Idurs;
		updateSubtypeIDUsingUniqueID();
		
		preferredCondition="Y"; //for now this logic
		property = new Property();
		
		this.D="0";
		this.ASP="0";
		this.PSTUT="0";
		this.RBS="0";
		this.NASP="0";
		
		this.S="0";
	}
	
	public Region identifyRegion() {		
		return Region.getRegion(Integer.valueOf(neuronSubtypeID.substring(0, 1)));		
	}
	
	public double measureExcitability() {			
		model.determineRheobases(1000, 0, 1000, 0.1);
		return model.getRheoBases()[0];		
	}
	
	public double measureExcitability_rb() {			
		model.determineRheobases_rb(750, 0, -2000, 0.1);
		return model.getRheoBases()[0];		
	}
	
	public double measureMinFreq() {
		float _2spikeRheo = model.getIsolatedCompartment(0).get2SpikeRheo(1000f, 0f, 1000f, 0.1f);
		SpikePatternAdapting modelPattern = getModelPattern(_2spikeRheo, 2000d);
		return modelPattern.getFiringFrequencyBasedOnSpikesCount();
	}
	/*
	public SpikePatternClass[] getPhenotype(double I_start, double I_offset, int n_Is, boolean doRBS) {
		SpikePatternClass[] phenotype = null;
		if(doRBS)
			phenotype = new SpikePatternClass[n_Is+1];
		else
			phenotype = new SpikePatternClass[n_Is];
		
		double max_D_factor = 0;
		for(int i=0;i<n_Is;i++) {
			double I = I_start + (i*I_offset)*1.0d;			
			SpikePatternAdapting modelPattern = getModelPattern(I, 750);			
			if(modelPattern==null) {			
				phenotype[i] = new SpikePatternClass();
				phenotype[i].addComponent(SpikePatternComponent.EMPTY);
			}else {
				SpikePatternClassifier classifier = new SpikePatternClassifier(modelPattern);
				classifier.classifySpikePattern(modelPattern.getSwa(), true);
				phenotype[i]=classifier.getSpikePatternClass();
				
				if(modelPattern.getNoOfSpikes()>0) {
					double D_factor =modelPattern.getFSL(); //classifier.getDelayFactor();
					if(D_factor > max_D_factor)
						max_D_factor = D_factor;
				}				
			}			
		}
		property.delayFactor = max_D_factor;
		
		// Rebound spiking or not? is appended at the end
		 
		if(doRBS) {
			double I = -500;			
			SpikePatternAdapting modelPattern = getModelPattern(I, 750);	
			phenotype[n_Is] = new SpikePatternClass();
			
			if(modelPattern==null) {			
				phenotype[n_Is].addComponent(SpikePatternComponent.EMPTY);
			}else {
				if(modelPattern.getNoOfSpikes()>0)
					phenotype[n_Is].addComponent(SpikePatternComponent.RBS);
				else
					phenotype[n_Is].addComponent(SpikePatternComponent.EMPTY);				
			}
		}		
		return phenotype;
	}
	*/
	public SpikePatternClass[] getPhenotype_v2(double I) {
		SpikePatternClass[] phenotype = new SpikePatternClass[2];
			
		SpikePatternAdapting modelPattern = getModelPattern(I, 750);			
		if(modelPattern==null || modelPattern.getNoOfSpikes() <= 1) {			
			phenotype[1] = new SpikePatternClass();
			phenotype[1].addComponent(SpikePatternComponent.EMPTY);
		}else {
			SpikePatternClassifier classifier = new SpikePatternClassifier(modelPattern);
			classifier.classifySpikePattern(modelPattern.getSwa(), true);
			phenotype[1]=classifier.getSpikePatternClass();		
			System.out.println(this.neuronSubtypeID +" "+modelPattern.getNoOfSpikes());
			
			property.delayFactor = classifier.getDelayFactor();
		}	
		
		
		I = -500;			
		modelPattern = getModelPattern(I, 750);	
		phenotype[0] = new SpikePatternClass();
		
		if(modelPattern==null) {			
			phenotype[0].addComponent(SpikePatternComponent.EMPTY);
		}else {
			if(modelPattern.getNoOfSpikes()>0)
				phenotype[0].addComponent(SpikePatternComponent.RBS);
			else
				phenotype[0].addComponent(SpikePatternComponent.EMPTY);				
		}
				
		return phenotype;
	}
	/*
	 * v3 of getphenotype()
	 * populate 4bit strings: RBS, D, ASP, PSTUT
	 */
	
	public void populatePhenotype_v3(double I) {
		I= property.excitability + I;
		
		SpikePatternClass  _class = new SpikePatternClass();
		SpikePatternAdapting modelPattern = getModelPattern(I, 750);			
		if(modelPattern==null || modelPattern.getNoOfSpikes() <= 1) {			
			_class.addComponent(SpikePatternComponent.EMPTY);
		}else {
			SpikePatternClassifier classifier = new SpikePatternClassifier(modelPattern);
			classifier.classifySpikePattern(modelPattern.getSwa(), true);
			_class = classifier.getSpikePatternClass();		
			//System.out.println(this.neuronSubtypeID +" "+modelPattern.getNoOfSpikes());			
			property.delayFactor = classifier.getDelayFactor();
			property.stutFactor = classifier.getStutFactor();
			
		}	
		if(_class.contains(SpikePatternComponent.D))
			this.D="1";
		if(_class.contains(SpikePatternComponent.ASP) || _class.contains(SpikePatternComponent.RASP))
			this.ASP="1";
		if(_class.contains(SpikePatternComponent.PSTUT) || _class.contains(SpikePatternComponent.PSWB))
			this.PSTUT="1";
		
		
		if(_class.equals(new SpikePatternClass("NASP", ".")))
			this.NASP="1";
		
		I = -500;			
		modelPattern = getModelPattern(I, 750);	
		_class = new SpikePatternClass();
		
		if(modelPattern==null) {			
			_class.addComponent(SpikePatternComponent.EMPTY);
			property.rbDelay = 500;
		}else {
			if(modelPattern.getNoOfSpikes()>0) {
				_class.addComponent(SpikePatternComponent.RBS);
				property.rbDelay = modelPattern.getFSL()-750;
			}				
			else {
				_class.addComponent(SpikePatternComponent.EMPTY);	
				property.rbDelay = 500;
			}							
		}
		
		if(_class.contains(SpikePatternComponent.RBS))
			this.RBS="1";
	}
	
	private SpikePatternAdapting getModelPattern(double I, double Idur) {
		model.setInputParameters(new double[] {I}, 100, Idur);		
		IzhikevichSolverMC solver = new IzhikevichSolverMC(model);
		solver.setsS(0.1);
		SpikePatternAdapting[] model_spike_pattern = solver.solveAndGetSpikePatternAdapting();	
		if(model_spike_pattern == null)
			return null;
		return model_spike_pattern[0];
	}
	/*
	 * the following needs to be done for duplicate cases 
	 */
	private void updateSubtypeIDUsingUniqueID() {
		String replacementID;
		Map<String, String> subtype_id_to_replacement_id;
		
		if(!(   neuronSubtypeID.equals("4-083") || 
				neuronSubtypeID.equals("4-087") ||
				neuronSubtypeID.equals("2-001-1") ||
				neuronSubtypeID.equals("4-011-1")
				)
				) {//!!!Except for ISO 2-compartment models!!! 
			if(model.getNCompartments()>1) { 
				
													// MC model consolidation comes after SC, 
												//so the map is already populated with original_subtype
														//to replacement ID maps
											
				
				subtype_id_to_replacement_id = UniqueID_to_SubtypeID_to_ReplacementID.get(uniqueID);
				
				neuronSubtypeID = uniqueID+"-"+subtype_id_to_replacement_id.get(neuronSubtypeID);
				return;
			}		
		}
		
		if(UniqueID_to_SubtypeID_to_ReplacementID.containsKey(uniqueID)) {
			subtype_id_to_replacement_id = UniqueID_to_SubtypeID_to_ReplacementID.get(uniqueID);
			replacementID = ""+(subtype_id_to_replacement_id.size()+1);
			
		}else {
			replacementID = "1"; 
			subtype_id_to_replacement_id = new HashMap<>();
			UniqueID_to_SubtypeID_to_ReplacementID.put(uniqueID, subtype_id_to_replacement_id);
		}
		
		String original_subtype_id = neuronSubtypeID;			
		neuronSubtypeID = uniqueID +"-"+ replacementID;		
		 
		subtype_id_to_replacement_id.put(original_subtype_id, replacementID);
		
		//UniqueID_to_SubtypeID_to_ReplacementID.put(uniqueID, subtype_id_to_replacement_id);
		
		
		/*String subStringFirst5 = neuronSubtypeID.substring(0, 5);
		if(!uniqueID.equals(subStringFirst5)) {			
				// exception logic
				if(uniqueID.equals(UNQIDTOSUBTYPE_MAP[0][0]) && neuronSubtypeID.equals(UNQIDTOSUBTYPE_MAP[0][1])) {
					neuronSubtypeID = UNQIDTOSUBTYPE_MAP[0][2];
				}else {				
					
					neuronSubtypeID= neuronSubtypeID.replaceFirst(subStringFirst5, uniqueID);
				}
				System.out.println("subtypeID update for "+uniqueID);
		}*/
	}
	
	public static String getHeaderCSVString() {
		String row="uniqueID,subtypeID,name,preferred";
	//	String row="name";
		row+=",k,a,b,d,C,Vr,Vt,Vpeak,Vmin";
	
		for(int i=0;i<4;i++) {
			row+=",k"+i;
			row+=",a"+i;
			row+=",b"+i;
			row+=",d"+i;
			row+=",C"+i;
			row+=",Vr"+i;
			row+=",Vt"+i;
			row+=",Vpeak"+i;
			row+=",Vmin"+i;
			
			if(i>0) {
				row+=",G"+(i-1);
				row+=",P"+(i-1);
			}
		}				
		return row;
	}
	public static String getHeaderCSVString_for_STAT_CORR() {
		String row="uniqueID,name";
	//	String row="name";
		row+=",k,a,b,d";
			
		return row;
	}
	public String getCSVString() {
		String row = "";
		if(model.getNCompartments()==1 || (model.getNCompartments()==2 && model.isIso_comp())) {
			row=uniqueID+","+neuronSubtypeID+","+name+","+preferredCondition;
		}
		
		//String row=name;
		for(int i=0;i<model.getNCompartments();i++) {
			row+=","+model.getK()[i];
			row+=","+model.getA()[i];
			row+=","+model.getB()[i];
			row+=","+model.getD()[i];
			row+=","+model.getcM()[i];
			row+=","+model.getvR()[i];
			row+=","+model.getvT()[i];
			row+=","+model.getvPeak()[i];
			row+=","+model.getvMin()[i];
			
			if(i>0) {
				row+=","+model.getG()[i-1];
				row+=","+model.getP()[i-1];
			}
			if(model.isIso_comp()) {
				break;
			}
		}
		return row;
	}
	
	public String getCSVString_For_STAT_CORR() {
		String row = "";
		if(model.getNCompartments()==1) {
			row=uniqueID+","+name;
		}
		
		double K_LOW = 0.65d;
		double K_HIGH = 1.3d;
		
		double A_LOW = 0.002d;
		double A_HIGH = 0.008d;
		
		double B_LOW = 0;
		double B_HIGH = 0;
		
		double D_LOW = 10d;
		double D_HIGH = 57d;
		
		
		String k = "int";
		String a = "int";
		String b = "ZERO";
		String d = "int";
		
		//String row=name;
		for(int i=0;i<model.getNCompartments();i++) {
			if(model.getK()[i]<=K_LOW)
				k="LOW";
			if(model.getK()[i]>=K_HIGH)
				k="HIGH";
			
			if(model.getA()[i]<=A_LOW)
				a="LOW";
			if(model.getA()[i]>=A_HIGH)
				a="HIGH";
			
			if(model.getB()[i]<B_LOW)
				b="NEG";
			if(model.getB()[i]>B_HIGH)
				b="POS";
			
			if(model.getD()[i]<=D_LOW)
				d="LOW";
			if(model.getD()[i]>=D_HIGH)
				d="HIGH";
			
			row+=","+k;
			row+=","+a;
			row+=","+b;
			row+=","+d;
		}
		return row;
	}
	public String getCSVStringFor_D_ND() {
		String row = "";
				
		//String row=name;
		for(int i=0;i<model.getNCompartments();i++) {
			row+=model.getK()[i];
			row+=","+model.getA()[i];
			row+=","+model.getB()[i];
			row+=","+model.getD()[i];
			row+=","+model.getcM()[i];
			row+=","+(model.getvT()[i] - model.getvR()[i]);
			row+=","+(model.getvMin()[i] - model.getvR()[i]);
			
			if(i>0) {
				row+=","+model.getG()[i-1];
				row+=","+model.getP()[i-1];
			}
		}
		row += ","+property.delayFactor;
		return row;
	}
	
	public boolean equals(ModelDataStructure mds) {
		boolean match = false;
		
		if(	matches(this.model.getK()[0], mds.model.getK()[0]) &&
			matches(this.model.getA()[0], mds.model.getA()[0]) &&
			matches(this.model.getB()[0], mds.model.getB()[0]) &&
			matches(this.model.getD()[0], mds.model.getD()[0]) &&
			matches(this.model.getcM()[0], mds.model.getcM()[0]) &&
			matches(this.model.getvR()[0], mds.model.getvR()[0]) &&
			matches(this.model.getvT()[0], mds.model.getvT()[0]) &&
			matches(this.model.getvPeak()[0], mds.model.getvPeak()[0]) ) {
				match = true;
		}
		
		return match;		
	}
	
	private boolean matches(double val1, double val2) {
		if(GeneralUtils.isCloseEnough(val1, val2, 0.001))
			return true;
		return false;
	}

	public CarlOutputParser getCarlOutputParser() {
		return carlOutputParser;
	}

	public void setCarlOutputParser(CarlOutputParser carl_op_parser) {
		this.carlOutputParser = carl_op_parser;
	}

	public String getMcLayoutCode() {
		return mcLayoutCode;
	}

	public void setMcLayoutCode(String mcLayoutCode) {
		this.mcLayoutCode = mcLayoutCode;
	}
	
	public int flatSubtypeID() {
		//6-023-1
		return Integer.valueOf(neuronSubtypeID.substring(0, 1)+neuronSubtypeID.substring(2, 5)+neuronSubtypeID.substring(6, 7));
	}
	
	private Izhikevich9pModelMC fetch_Compartment(int i) {		
		Izhikevich9pModelMC _model =null;
		_model = new Izhikevich9pModel1C(1);
		
		_model.setK(new double[] {model.getK()[i]});
		_model.setA(new double[] {model.getA()[i]});
		_model.setB(new double[] {model.getB()[i]});
		_model.setD(new double[] {model.getD()[i]});	
		_model.setcM(new double[] {model.getcM()[i]});
		_model.setvR(model.getvR()[i]);
		_model.setvT(new double[] {model.getvT()[i]});	
		_model.setvMin(new double[] {model.getvMin()[i]});	
		_model.setvPeak(new double[] {model.getvPeak()[i]});        
        
		return _model;
	}
	
	public ModelDataStructure[] deCoupleMCModelDataStructure() {
		if(model.getNCompartments()<2) {
			System.out.println("WARNING!! ncomp < 2 for" +neuronSubtypeID);
		}
		ModelDataStructure[] mdss = new ModelDataStructure[model.getNCompartments()];
		
		for(int i=0;i<model.getNCompartments();i++) {
			Izhikevich9pModelMC _model = fetch_Compartment(i);
			mdss[i] = new ModelDataStructure(uniqueID, neuronSubtypeID, name, _model  , Is);
			if(i==0)
				mdss[i].S = "0";
			else
				mdss[i].S = "1";
		}
		
		return mdss;
	}
}
