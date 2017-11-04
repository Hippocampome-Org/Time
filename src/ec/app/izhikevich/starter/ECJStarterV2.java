package ec.app.izhikevich.starter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import ec.EvolutionState;
import ec.Evolve;
import ec.app.izhikevich.ModelEvaluatorWrapper;
import ec.app.izhikevich.inputprocess.labels.ModelParameterID;
import ec.app.izhikevich.model.Izhikevich9pModel;
import ec.app.izhikevich.model.neurontypes.mc.EAGenes;
import ec.app.izhikevich.model.neurontypes.mc.OneNeuronInitializer;
import ec.app.izhikevich.resonate.Bifurcation;
import ec.app.izhikevich.resonate.PyPlotter;
import ec.app.izhikevich.util.ModelFactory;
import ec.util.Output;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

public class ECJStarterV2 {	
	public static final boolean MULTI_OBJ = false;
	public static final boolean TURN_OFF_CLASSIFIER = false;
	
	public static String Phen_Category = "1-1-1";
	public static String Phen_Num = "B0";
	public static String Neur = "9-999";	
	public static boolean iso_comp = false;
	
	public static int N_COMP = -1;
	public static int[] CONN_IDCS =null; 	
	public static String PRIMARY_INPUT = "";
	private static final boolean timer = true;			
		 
	private static String ECJ_PARMS;	
	static {
		try {
			BufferedReader br = new BufferedReader(new FileReader("primary_input"));
			String str = br.readLine();
			if(str==null){
				System.out.println("Empty primary input!");
				System.exit(-1);
			}
			
			//System.out.println(lastButTwo);
			StringTokenizer st = new StringTokenizer(str, ",");
			Phen_Category = st.nextToken();
			Phen_Num = st.nextToken();
			Neur = st.nextToken();
			///System.out.println(st.nextToken());
			N_COMP = Integer.valueOf(st.nextToken());
			CONN_IDCS = new int[N_COMP];
			for(int i=0;i<N_COMP;i++)
				CONN_IDCS[i]=Integer.valueOf(st.nextToken());
			if(st.hasMoreTokens()){
				iso_comp = Boolean.valueOf(st.nextToken());
			}
			str = br.readLine();
			if(str.equals("ext")){
				if(MULTI_OBJ) ECJ_PARMS = "input/izhikevich_MO_carl.params";
				else	ECJ_PARMS = "input/izhikevich_SO_carl.params";
			}else{
				if(MULTI_OBJ) ECJ_PARMS = "input/izhikevich_MO.params";
				else	ECJ_PARMS = "input/izhikevich_SO.params";
			}
			br.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PRIMARY_INPUT = "input/"+Phen_Category+"/"+Phen_Num+"/"+Neur+".json";
	}
	
	
	public static void main(String[] args) {	
		OneNeuronInitializer.init(N_COMP, CONN_IDCS, PRIMARY_INPUT, iso_comp);
		Map<ModelParameterID, EAParmsOfModelParm> geneParms = OneNeuronInitializer.geneParms;
		
        MCParamFile ecjParamFile;
		try {	
			/*
			 * setup Gene parms - neuron type dependent
			 */
			ecjParamFile = new MCParamFile(ECJ_PARMS, geneParms);
			ParameterDatabase parameterDB = ecjParamFile.getLoadedParameterDB();
			if(MULTI_OBJ){
				ecjParamFile.addParametertoDB("multi.fitness.num-objectives", ""+ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS.length);
			}
			
			//ecjParamFile.displayParameterDB();
			//System.exit(0);
			int nJobs =  ecjParamFile.getParameterFromDB("jobs");
			
			if(args.length==0){
				File opDir = new File("output//"+Phen_Category+"//"+Phen_Num+"//"+Neur+"//"+"local");
				if(!(new File("output//"+Phen_Category).exists())){
					new File("output//"+Phen_Category).mkdir();
				}
				if(!(new File("output//"+Phen_Category+"//"+Phen_Num).exists())){
					new File("output//"+Phen_Category+"//"+Phen_Num).mkdir();
				}
				if(!(new File("output//"+Phen_Category+"//"+Phen_Num+"//"+Neur).exists())){
					new File("output//"+Phen_Category+"//"+Phen_Num+"//"+Neur).mkdir();
				}
				if(!opDir.exists()){
					opDir.mkdir();
				}
				
				runLocally(nJobs,parameterDB,true);
			}else{	
				//on server
				String opFolder = Phen_Category+"_"+Phen_Num+"_"+Neur;
				File opDir = new File("output//"+opFolder);
				if(!opDir.exists()){
					opDir.mkdir();
				}	
				int jobIdx = Integer.valueOf(args[0]);
				int chkpntidx = 0;
				if(args.length>1)
					chkpntidx=Integer.valueOf(args[1]);
				runOnServer(parameterDB, opFolder, jobIdx, chkpntidx, false);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		
	}


static void runLocally(int nJobs, ParameterDatabase pdb, boolean plot_bif){
	for(int i=0; i<nJobs; i++)
	{
		Output output = Evolve.buildOutput();
		output.setFilePrefix(Phen_Category+"//"+Phen_Num+"//"+Neur+"//local//job."+i+".");		
		long timeStart = 0;
		if(timer) timeStart = System.nanoTime();
		
		EvolutionState state = Evolve.initialize(pdb, i+1, output );	
		state.job = new Object[1];                                  // make the job argument storage
        state.job[0] = new Integer(i); 
		//state.startFresh();					
		state.run(EvolutionState.C_STARTED_FRESH);
		Evolve.cleanup(state);
		
		if(timer)  {
			  TimeUnit unit = TimeUnit.MILLISECONDS;
			  System.out.println("Total Time in ms: "+unit.convert(System.nanoTime()-timeStart, TimeUnit.NANOSECONDS));
		  }
		output = null;
		state = null;
		//if(nJobs%25==0)
		System.gc();
	
		if(plot_bif) {
			System.out.println("starting bifurcation..");
			String modelECJFilePathExp = Phen_Category+"//"+Phen_Num+"//"+Neur+"//local";
			Izhikevich9pModel model=ModelFactory.readModel(modelECJFilePathExp, i);
			
					
				double Imin = 500;
				double Imax = 750;
				int N = 1000;
				
			String dataFile = "output//"+modelECJFilePathExp+"//temp_bif"+i+".dat";
			
			
			Bifurcation.writePcareMapVals(dataFile, model, Imin, Imax, N);
			System.out.println("plotting..");
			PyPlotter plotter = new PyPlotter(dataFile);
			plotter.invoke("plotterBIF_unx.py");
			File bifFile = new File(dataFile);
			bifFile.delete();
			
			System.out.println("done");
		}
		
	}

}

static void runOnServer(ParameterDatabase pdb, String folder, int jobIdx, int chkpntGen, boolean plot_bif){
		Output output = Evolve.buildOutput();
		output.setFilePrefix(folder+"//job."+jobIdx+".");		
		long timeStart = 0;
		if(timer) timeStart = System.nanoTime();
		
		//possibly restore from chk point:
		String[] chkpntArgs = new String[] {"-checkpoint", "ec_tuneIzh9p."+chkpntGen+".gz"};
		//EvolutionState state = Evolve.possiblyRestoreFromCheckpoint(chkpntArgs);
		//if(state!=null){
		//	state.run(EvolutionState.C_STARTED_FROM_CHECKPOINT);
		//}else{
		EvolutionState state = Evolve.initialize(pdb, jobIdx+1, output );	
			
			state.job = new Object[1];                                  // make the job argument storage
	        state.job[0] = new Integer(jobIdx); 
			//state.startFresh();					
			state.run(EvolutionState.C_STARTED_FRESH);
		//}
		
		Evolve.cleanup(state);
		
		if(timer)  {
			  TimeUnit unit = TimeUnit.MILLISECONDS;
			  System.out.println("Total Time in ms: "+unit.convert(System.nanoTime()-timeStart, TimeUnit.NANOSECONDS));
		  }
		
		if(plot_bif) {
			System.out.println("starting bifurcation..");
			String modelECJFilePathExp = folder;
			Izhikevich9pModel model=ModelFactory.readModel(modelECJFilePathExp, jobIdx);
			
					
				double Imin = 300;
				double Imax = 800;
				int N = 1000;
				
			String dataFile = "output//"+modelECJFilePathExp+"//temp_bif"+jobIdx+".dat";
			
			
			Bifurcation.writePcareMapVals(dataFile, model, Imin, Imax, N);
			System.out.println("plotting..");
			PyPlotter plotter = new PyPlotter(dataFile);
			plotter.invoke("plotterBIF_unx.py");
			File bifFile = new File(dataFile);
			bifFile.delete();
			
			System.out.println("done");
		}
		
		output = null;
		state = null;
		//if(nJobs%25==0)
		System.gc();
	}

}
class MCParamFile {
	private ParameterDatabase parameterDB;
	Map<ModelParameterID, EAParmsOfModelParm> geneParms;
	
	public MCParamFile(String parentfileName, Map<ModelParameterID, EAParmsOfModelParm> geneParms) throws FileNotFoundException, IOException {		
		parameterDB = new ParameterDatabase(new File(parentfileName));
		this.geneParms = geneParms;
	}
	
	public ParameterDatabase getLoadedParameterDB() {
		loadNonGeneParmsToDB();
		loadGeneParmsToDB();
		return parameterDB;
	}
	
	private void loadNonGeneParmsToDB(){
		addParametertoDB("vector.species.genome-size",""+EAGenes.geneLength);
		addParametertoDB("vector.species.min-gene","0");						// Float vector species muhst have default min
		addParametertoDB("vector.species.max-gene","1");						//max genes
		addParametertoDB("vector.species.mutation-prob","0.1");				// default - overriden at gene level
		addParametertoDB("vector.species.mutation-type","integer-reset");		//default - overriden at gene level
		//addParametertoDB("pop.subpop.0.species.mutation-type","integer-reset");
		//pop.subpop.0.species.mutation-stdev.3
	}
	private void loadGeneParmsToDB(){
		ModelParameterID[] modelParams = ModelParameterID.values();		
		for(ModelParameterID modelParam: modelParams) {
			//System.out.println(modelParam.toString());
			if(EAGenes.nComps<2){
				if(modelParam.equals(ModelParameterID.G) || modelParam.equals(ModelParameterID.P) || modelParam.equals(ModelParameterID.W))
					continue;
			}
			Integer[] modelParamIndices = EAGenes.getIndices(modelParam);			
			boolean isDendrite = false;										//first index always soma
			int baseIdx = -1;
			if(ModelParameterID.I.equals(modelParam) || ModelParameterID.I_dur.equals(modelParam) || ModelParameterID.VPEAK.equals(modelParam) || ModelParameterID.K.equals(modelParam)) 
			//if(!ModelParameterID.VR.equals(modelParam) && !ModelParameterID.P.equals(modelParam) && !ModelParameterID.W.equals(modelParam))
			{
				 baseIdx = modelParamIndices[0];
			}			
			for(int idx: modelParamIndices){				
				addGene(modelParam, idx, isDendrite, baseIdx);
				if(!ModelParameterID.I.equals(modelParam) && !ModelParameterID.I_dur.equals(modelParam)) 
					isDendrite = true;
			}
		}
	}
	
	private void addGene(ModelParameterID modelParam, int idx, boolean isDendrite, int baseIdx) {
		EAParmsOfModelParm eaParams = geneParms.get(modelParam);	
		if(!ModelParameterID.I.equals(modelParam) && !ModelParameterID.I_dur.equals(modelParam) && !ModelParameterID.VPEAK.equals(modelParam) && !ModelParameterID.K.equals(modelParam)) 
		//if(ModelParameterID.VR.equals(modelParam) || ModelParameterID.P.equals(modelParam) || ModelParameterID.W.equals(modelParam))
		{
			addParametertoDB("vector.species.min-gene."+idx, eaParams.getMinGene());
			addParametertoDB("vector.species.max-gene."+idx, eaParams.getMaxGene());
		}else{
			addParametertoDB("vector.species.min-gene."+idx, eaParams.getMinGenes()[idx-baseIdx]);
			addParametertoDB("vector.species.max-gene."+idx, eaParams.getMaxGenes()[idx-baseIdx]);
		}
		addParametertoDB("vector.species.mutation-type."+idx, eaParams.getMutType());
		if(!eaParams.getMutSD().equalsIgnoreCase("0.0")){
			addParametertoDB("vector.species.mutation-stdev."+idx, eaParams.getMutSD());
		}
		
		if(isDendrite){
			if(ModelParameterID.B.equals(modelParam)){
				addParametertoDB("vector.species.min-gene."+idx, "-30.0");
			}
			if(eaParams.getMutType().equals("integer-random-walk")){
				addParametertoDB("vector.species.random-walk-probability."+idx, String.valueOf(Float.valueOf(eaParams.getMutRate())*1));
			}else{
				addParametertoDB("vector.species.mutation-prob."+idx,String.valueOf(Float.valueOf(eaParams.getMutRate())*1));
			}
		}else{
			if(eaParams.getMutType().equals("integer-random-walk")){
				addParametertoDB("vector.species.random-walk-probability."+idx, eaParams.getMutRate());
			}else{
				addParametertoDB("vector.species.mutation-prob."+idx, eaParams.getMutRate());
			}
		}
		
		
		if(isDendrite && 
			//	!ModelParameterID.K.equals(modelParam) &&
			//	!ModelParameterID.CM.equals(modelParam) &&
				!ModelParameterID.VPEAK.equals(modelParam)&&
				!ModelParameterID.P.equals(modelParam)&&
			//	!ModelParameterID.G.equals(modelParam)&&
				!ModelParameterID.W.equals(modelParam)) { // for dendrite bounded bw (0 and 10), hence set to true
			addParametertoDB("vector.species.mutation-bounded."+idx, "false");
		}else{
			addParametertoDB("vector.species.mutation-bounded."+idx, eaParams.getMutBounded());
		}
	}
	void addParametertoDB(String name, String value) {
		parameterDB.set(new Parameter(name), value);
	}
	public int getParameterFromDB(String name) {
		return parameterDB.getInt(new Parameter(name), new Parameter("jobs"));
	}
	public void displayParameterDB(){
		Set<String> propNames = parameterDB. stringPropertyNames();
		TreeSet<String> propNamesSorted = new TreeSet<>(propNames);
		for(String key: propNamesSorted)
			System.out.println(key+" = "+parameterDB.getProperty(key));
	}
}
