package ec.app.izhikevich.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

import ec.app.izhikevich.ModelEvaluatorWrapper;
import ec.app.izhikevich.evaluator.ModelEvaluatorMC;
import ec.app.izhikevich.evaluator.MultiCompConstraintEvaluator;
import ec.app.izhikevich.evaluator.qualifier.StatAnalyzer;
import ec.app.izhikevich.model.Izhikevich9pModel1C;
import ec.app.izhikevich.model.Izhikevich9pModel3C;
import ec.app.izhikevich.model.Izhikevich9pModel3C_L2;
import ec.app.izhikevich.model.Izhikevich9pModel4C;
import ec.app.izhikevich.model.Izhikevich9pModelMC;
import ec.app.izhikevich.model.neurontypes.mc.EAGenes;
import ec.app.izhikevich.model.neurontypes.mc.OneNeuronInitializer;
import ec.app.izhikevich.outputprocess.CarlMcSimData;
import ec.app.izhikevich.outputprocess.CarlOutputParser;
import ec.app.izhikevich.outputprocess.CarlSpikePattern;
import ec.app.izhikevich.starter.ECJStarterV2;

public class DisplayUtilwCarlPhenoType {		
	static boolean displayParms = true;
	static boolean displayErrors = false;
	static boolean plotVtraces = false;	
	public static final boolean displayPatternForExternalPlot = false;
	
	static int nComp;
	private static int[] forConnIdcs;
	static String phenfileNamePath;
	static int jobID;
	static String ECJfilePath;
	static String exp;
	static int nthsubpop;
	static String all_VFileDir;
	
	static String phen_category;
	static String phen_num;
	static String Neur;
	
	static String primary_input_json;
	static boolean iso_comp;
	static String raw_primary_input;
	
	private static void setVars(String[] args){
		StatAnalyzer.display_stats = false;
		iso_comp= false;
		exp="mc0";		
		nthsubpop = -1;
		
		if(args!=null && args.length>0) {			
			String str = args[0];			
			if(str==null){
				System.out.println("Empty primary input args[0]!");
				System.exit(-1);
			}
		//	if(args.length>1) {
		//		nthsubpop=Integer.valueOf(args[1]);
		//	}
			raw_primary_input = str;
			//System.out.println(lastButTwo);
			StringTokenizer st = new StringTokenizer(str, ",");
			phen_category = st.nextToken();
			phen_num = st.nextToken();
			Neur = st.nextToken();
			///System.out.println(st.nextToken());
			nComp = Integer.valueOf(st.nextToken());
			forConnIdcs = new int[nComp];
			for(int i=0;i<nComp;i++)
				forConnIdcs[i]=Integer.valueOf(st.nextToken());
			if(st.hasMoreTokens()){
				iso_comp = Boolean.valueOf(st.nextToken());
			}
			if(args[1]==null) {
				System.out.println("Empty primary input args[1]! -- ecj op file path empty");
				System.exit(-1);
			}
			
			ECJfilePath =args[1];//"/home/siyappan/CARLsim3.1/projects/tuneIzh9p/";			
			primary_input_json = "input/"+phen_category+"/"+phen_num+"/"+Neur+".json";
			
			//all_VFileDir = ECJfilePath;
			all_VFileDir="results";
			
			nthsubpop = Integer.valueOf(args[2]);
			
		}else {			
			phen_category = ECJStarterV2.Phen_Category;
			phen_num = ECJStarterV2.Phen_Num;
			Neur = ECJStarterV2.Neur;//"N2";			
			nComp = ECJStarterV2.N_COMP;
			forConnIdcs = ECJStarterV2.CONN_IDCS;		
				
			raw_primary_input = ECJStarterV2.raw_primary_input;
			ECJfilePath ="output/"+phen_category+"/"+phen_num+"/"+Neur+"/"+exp;	
			primary_input_json = "input/"+phen_category+"/"+phen_num+"/"+Neur+".json";
			
			all_VFileDir=ECJfilePath;
		}
		
	}
	
	public static void main(String[] args) {
		/*
		 * set primary input from ECJ starter (from primary_input text file) 
		 * OR 
		 * pass in args raw_primary input string (csv) for server run 
		 */
		
		//part of RASP.ASP fix
		// args = new String[3];
		// args[0] = "11,2b,5-005-1,2,0,0" ;
		// args[1] ="output\\11\\mcdone\\results_11_2b_5-005-1";
		// args[2] = "0";
		//
		setVars(args);				
		OneNeuronInitializer.init(nComp, forConnIdcs, primary_input_json, iso_comp);		
		//String exp = "mc0"; 
		int nsubPop = CARL_PostECJWrapper1.N_SUBPOP;
		boolean save_plot = true;	
		String opDir =all_VFileDir;//"output\\11\\1c\\4-000-4\\mc0";
		//all_VFileDir =  //;
		
		double[][] parms = ECStatOutputReader.readBestSolutionS(ECJfilePath+"/job.0.Full", 50, nsubPop);
		ModelDBInterface mi = new ModelDBInterface(raw_primary_input, exp, 0, save_plot);
		
		/*
		 * iterate through best solutions from each sub pop; use carl phenotype file for fitness; and use carl allv files for plot
		 */
		for(int i=0;i<nsubPop;i++){
			
			if(nthsubpop>-1) {
				i=nthsubpop;
				System.out.println("\n\n<<<o>>>\n<<<o>>>  "+raw_primary_input+"   subpop "+i);
				//notice "0" in below call: 
				float fitness = evaluateInd(parms[i], all_VFileDir+"/"+0+"_phenotype");				
				//mi.onlyPlot(all_VFileDir, opDir, nsubPop, i, parms[i]);
				
				//mi.onlyPlot(all_VFileDir, opDir, nsubPop, i, parms[i], CARL_PostECJWrapper1.mclayoutcode(nComp));
				break;
			}else {
				System.out.println("\n\n<<<o>>>\n<<<o>>>  "+raw_primary_input+"   subpop "+i);
				float fitness = evaluateInd(parms[i], all_VFileDir+"/"+i+"_phenotype");				
				mi.onlyPlot(all_VFileDir, opDir, nsubPop, i, parms[i], CARL_PostECJWrapper1.mclayoutcode(nComp));
			}
		}			
	}
	
	
	
	private static void stripSemicolon(int popIdx){
		int nSomScen = 1;
		int nTotScen = nSomScen + 1 + 1 + (nComp-1)*2;
		
		for(int i=0;i<nTotScen;i++){
			for(int j=0;j<nComp;j++){
				try {
					File rawVFile = new File("output/"+ECJfilePath+"/"+popIdx+"/allV_"+i+"_"+j);
					BufferedReader br = new BufferedReader(new FileReader(rawVFile));
					BufferedWriter bw = new BufferedWriter(new FileWriter("output/"+ECJfilePath+"/"+popIdx+"/allVt_"+i+"_"+j));
					String str = br.readLine();
					while(str!=null){
						str = str.split(";")[0];
						bw.write(str);
						bw.newLine();
						bw.flush();
						str = br.readLine();
					}
					br.close();
					bw.close();
					
					//System.out.println("raw file deleted?:\t"+rawVFile.delete());
				}catch(IOException io){
					io.printStackTrace();
				}
			}
		}
		
		
		
	}
	private static float evaluateInd(double[] parms, String phenotypeFileName){
    	//System.out.println("entry to evaluateInd..");
    	//genes.display(); System.out.println(phenotypeFileName);
    	
    	EAGenes genes = new EAGenes(parms,  iso_comp);
		Izhikevich9pModelMC model = getRightInstanceForModel(); 
        model.setK(genes.getK());
		model.setA(genes.getA());
		model.setB(genes.getB());
		model.setD(genes.getD());	
		model.setcM(genes.getCM());
		model.setvR(genes.getVR());
		model.setvT(genes.getVT());		
		model.setvMin(genes.getVMIN());//		
        model.setvPeak(genes.getVPEAK());

        model.setG(genes.getG()); 
        model.setP(genes.getP());
        
        double[] currents = genes.getI();
        double[] weight = genes.getW();
      
        //PhenotypeParser parser = new PhenotypeParser(phenotype, model.getNCompartments());        
        CarlOutputParser parser = new CarlOutputParser(phenotypeFileName, false);          
		CarlSpikePattern[] carlSpikePatterns = parser.extractCarlSomaPatterns();		
		/*
		 * carlSpikePatterns should map to input_spike_pattern_cons array below
		 */
	
		CarlMcSimData carlMcSimData = parser.extractCarlMcSimData();
		//parser.deletePhenotypeFile();
		ModelEvaluatorMC evaluator = new ModelEvaluatorMC(model,
        		ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS,
        		ModelEvaluatorWrapper.INPUT_PAT_REP_WEIGHTS,
        		ModelEvaluatorWrapper.INPUT_MC_CONS,
        								currents,
        								weight,
        								carlSpikePatterns,
        								carlMcSimData);
		//System.out.println("after evaluator instance creation..");
		evaluator.setDisplayAll(true);	
        float f = evaluator.getFitness(); 
        
        for(int idx=0;idx<model.getNCompartments();idx++){
			displayForC(model, idx);
		}
		for(int idx=0;idx<model.getNCompartments()-1;idx++){
			System.out.println("Gt_"+(idx+1)+"="+model.getG()[idx]);
			System.out.println("P_"+(idx+1)+"="+model.getP()[idx]);
			//System.out.println("W"+idx+"="+weights[idx]);
		}
		
        return f;
	}
	
	public static Izhikevich9pModelMC getRightInstanceForModel(){
		if(nComp==1){
			return new Izhikevich9pModel1C(1);
		}
		if(nComp==2){
			return new Izhikevich9pModelMC(2);
		}
		if(nComp==3){
			if(MultiCompConstraintEvaluator.forwardConnectionIdcs[2]==0)
				return new Izhikevich9pModel3C(3);
			else
				return new Izhikevich9pModel3C_L2(3);
		}
		if(nComp==4){
			return new Izhikevich9pModel4C(4);
		}
		System.out.println("rightModel needs to be instantiated!!");
		return null;	
	}
	public static void displayForC(Izhikevich9pModelMC model, int idx) {
		String forC ="";		
		forC = "\n\nk"+idx+"="+model.getK()[idx]+";\na"+idx+"="+model.getA()[idx]+";\nb"+idx+"="+model.getB()[idx]+
				";\nd"+idx+"="+model.getD()[idx]+";\nC"+idx+"="+model.getcM()[idx]+";\nvR"+idx+"="+model.getvR()[idx]+";\nvT"+idx+"="+model.getvT()[idx]+
				";\nvPeak"+idx+"="+model.getvPeak()[idx]+";\nc"+idx+"="+model.getvMin()[idx]+";";
		System.out.println(forC);
	}
	
}
