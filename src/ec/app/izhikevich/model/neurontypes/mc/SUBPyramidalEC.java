package ec.app.izhikevich.model.neurontypes.mc;

import java.util.HashMap;
import java.util.Map;

import ec.app.izhikevich.ModelEvaluatorWrapper;
import ec.app.izhikevich.evaluator.MultiCompConstraintEvaluator;
import ec.app.izhikevich.inputprocess.InputSpikePatternConstraint;
import ec.app.izhikevich.inputprocess.InputParser;
import ec.app.izhikevich.inputprocess.InputMCConstraint;
import ec.app.izhikevich.inputprocess.labels.ModelParameterID;
import ec.app.izhikevich.spike.PatternFeature;
import ec.app.izhikevich.starter.EAParmsOfModelParm;

public class SUBPyramidalEC{
	private static final String INPUT = "input/TSWB_NASP.input";	
	private static final String COMMON_MUT_RATE = "0.2";
	public static InputSpikePatternConstraint[] EXP_SPIKE_PATTERN_DATA = null;	
	public static InputMCConstraint[] MC_CONSTRAINT_DATA = null;	
	public static final Map<ModelParameterID, EAParmsOfModelParm> geneParms = new HashMap<>();
	private static int nCompartments =0;
	private static int nCurrentGenes=0;
	
	public static void init(int nComp, int[] forwardConnIdcs, int nCurr){
		nCompartments=nComp;
		initForwardConnectionIdcs(forwardConnIdcs);
		nCurrentGenes=nCurr;
		initExpSpikePatternData();
		initPatRepWeights();
		if(nComp>1)
			initMcConstraintData();
		initGeneLayout();
		initGeneParmsOfModelParmsExceptI();
		initGeneParmForI();		
	}	
	
	private static void initExpSpikePatternData() {
		EXP_SPIKE_PATTERN_DATA = InputParser.getExpSpikePatternData(INPUT);		
		ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS = EXP_SPIKE_PATTERN_DATA;		
	}
	
	private static void initMcConstraintData() {
		MC_CONSTRAINT_DATA = InputParser.getMCConstraintData(INPUT);
		ModelEvaluatorWrapper.INPUT_MC_CONS= MC_CONSTRAINT_DATA;		
	}
	private static void initPatRepWeights() {
		ModelEvaluatorWrapper.INPUT_PAT_REP_WEIGHTS = InputParser.getPatternRepairWeights(INPUT);
	}
	private static void initForwardConnectionIdcs(int[] forwardConnIdcs) {
		MultiCompConstraintEvaluator.forwardConnectionIdcs = forwardConnIdcs;
	}
	private static void initGeneLayout() {
		/*
		 * setup GENE layout
		 */
		EAGenes.setupEAGene(getNCompartments(), getNCurrentGenes());
	}
	
	private static void initGeneParmsOfModelParmsExceptI() {
		/*
		 * load default ranges for model parameters: 
		 * compartments share same parms except mutation bounded: 
		 * dendritic compartments have unbounded mutation
		 */
		initGeneParmForK(0,10,3,5);
	//	geneParms.put(ModelParameter.K, new EAParmsOfModelParm("1",		"3",	"gauss",				"0.1",	"0.3",	"true" ));		
	
		geneParms.put(ModelParameterID.A, new EAParmsOfModelParm("0.0","0.5","gauss", 				COMMON_MUT_RATE,	"0.05",	"true" ));		
		geneParms.put(ModelParameterID.B, new EAParmsOfModelParm("0",		"5",	"gauss", 				COMMON_MUT_RATE,	"0.3", 	"false" ));		
		geneParms.put(ModelParameterID.D, new EAParmsOfModelParm("0",		"120",	"integer-random-walk",	COMMON_MUT_RATE,	"0.0", 	"false" ));		
		
		geneParms.put(ModelParameterID.CM, new EAParmsOfModelParm("20",	"100",	"integer-random-walk", 	COMMON_MUT_RATE, 	"0.0",	"true" ));		
		geneParms.put(ModelParameterID.VR, new EAParmsOfModelParm("-62",	"-58",	"gauss", 				COMMON_MUT_RATE,	"0.3", 	"true" ));	
		geneParms.put(ModelParameterID.VMIN, new EAParmsOfModelParm("5",	"10",	"gauss", 				COMMON_MUT_RATE,	"0.3", 	"true" ));	//vr + gene
		geneParms.put(ModelParameterID.VT, new EAParmsOfModelParm("10",	"15",	"gauss", 				COMMON_MUT_RATE,	"0.3", 	"true" ));	//vr + gene
		
		/*
		 * different vpeaks for soma and dendrites; for spikeProp (contrary to bio realism)
		 */
		initGeneParmForVpeak(40, 45, 1, 20);
		
		//geneParms.put(ModelParameter.VPEAK, new EAParmsOfModelParm("58","60",	"gauss", 				"0.1", 	"0.3", 	"true" ));	//vr + gene	
		
		geneParms.put(ModelParameterID.G, new EAParmsOfModelParm(	"5",	"250",	"integer-random-walk", 	COMMON_MUT_RATE, 	"0.0", 	"true" ));	
		geneParms.put(ModelParameterID.P, new EAParmsOfModelParm(	"0",	"1",	"gauss", 				COMMON_MUT_RATE, 	"0.05", "true" ));	
		geneParms.put(ModelParameterID.W, new EAParmsOfModelParm(	"0",	"1",	"gauss", 				COMMON_MUT_RATE, 	"0.2", 	"true" ));	
	}
	
	
	private static void initGeneParmForK(int somaMinGene, int somaMaxGene, int dendMinGene, int dendMaxGene){
		//setup for k. ranges are different for soma and dendrite
		String[] minGenes = new String[getNCompartments()];
		String[] maxGenes = new String[getNCompartments()];
		for(int i=0;i<minGenes.length;i++) {
			if(i==0) {
				minGenes[i] = ""+somaMinGene;
				maxGenes[i] = ""+somaMaxGene;
			}else{
				minGenes[i] = ""+dendMinGene;
				maxGenes[i] = ""+dendMaxGene;
			}
			
		}
		geneParms.put(ModelParameterID.K, new EAParmsOfModelParm(	minGenes, maxGenes,	"gauss", 				COMMON_MUT_RATE, 	"0.5", 	"true" ));
		
	}
	
	private static void initGeneParmForVpeak(int somVpeakMin, int somVpeakMax, int dendOffsetMin, int dendOffsetMax){
		//setup for vpeak, since vpeak ranges are different for soma and dendrite
		String[] minGenes = new String[getNCompartments()];
		String[] maxGenes = new String[getNCompartments()];
		for(int i=0;i<minGenes.length;i++) {
			if(i==0) {
				minGenes[i] = ""+somVpeakMin;
				maxGenes[i] = ""+somVpeakMax;
			}else{
				minGenes[i] = ""+(dendOffsetMin);
				maxGenes[i] = ""+(dendOffsetMax);    //dend vPeak anywhere between somvpeak and somvpeak+ofset// may help with spike prop
			}
			
		}
		geneParms.put(ModelParameterID.VPEAK, new EAParmsOfModelParm(	minGenes, maxGenes,	"gauss", 				COMMON_MUT_RATE, 	"0.3", 	"true" ));
		
	}
	
	private static void initGeneParmForI(){
		//setup for current genes requires extra setup
		int nCurrentGenes = EAGenes.nCurrents;
		String[] minGenes = new String[nCurrentGenes];
		String[] maxGenes = new String[nCurrentGenes];
		int idx=0;
		for(int i=0;i<EXP_SPIKE_PATTERN_DATA.length; i++) {
			PatternFeature current = EXP_SPIKE_PATTERN_DATA[i].getCurrent();			
			if(current.isRange()) {
				minGenes[idx] = String.valueOf(current.getValueMin());
				maxGenes[idx++] = String.valueOf(current.getValueMax());
			}
		}
		geneParms.put(ModelParameterID.I, new EAParmsOfModelParm(	minGenes, maxGenes,	"integer-random-walk", 	COMMON_MUT_RATE, 	"0.0", 	"true" ));
		
	}
	
	private static int getNCompartments() {
		return nCompartments;
	}
	
	private static int getNCurrentGenes() {
		return nCurrentGenes;
	}
}
