package ec.app.izhikevich.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import ec.app.izhikevich.ModelEvaluatorWrapper;
import ec.app.izhikevich.evaluator.ModelEvaluatorMC;
import ec.app.izhikevich.evaluator.qualifier.StatAnalyzer;
import ec.app.izhikevich.model.Izhikevich9pModel1C;
import ec.app.izhikevich.model.Izhikevich9pModel3C;
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
	
	static {
		StatAnalyzer.display_stats = true;		
		//phenfileNamePath = "output/10_2_3/B3/2-043/mcmp1/";
		//jobID = 0;
				
		String phen_category = ECJStarterV2.Phen_Category;
		String phen_num = ECJStarterV2.Phen_Num;
		String Neur = ECJStarterV2.Neur;//"N2";		
		String exp = "mcmp6"; 
		
		nComp = ECJStarterV2.N_COMP;
		forConnIdcs = ECJStarterV2.CONN_IDCS;
				 
		
		ECJfilePath =phen_category+"/"+phen_num+"/"+Neur+"/"+exp;
				 
	}
	
	public static void main(String[] args) {
		OneNeuronInitializer.init(nComp, forConnIdcs, ECJStarterV2.PRIMARY_INPUT, ECJStarterV2.iso_comp);
		double[][] parms = DisplayUtilMcwSyn.readBestParmsS(ECJfilePath, DisplayUtilMcwSyn.nPops);
		for(int i=0;i<parms.length;i++){
			float fitness = evaluateInd(parms[i], "output/"+ECJfilePath+"/"+i+"_phenotype");
			//stripSemicolon(i);
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
    	
    	EAGenes genes = new EAGenes(parms,  ECJStarterV2.iso_comp);
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
        return evaluator.getFitness(); 
	}
	
	public static Izhikevich9pModelMC getRightInstanceForModel(){
		if(nComp==1){
			return new Izhikevich9pModel1C(1);
		}
		if(nComp==2){
			return new Izhikevich9pModelMC(2);
		}
		if(nComp==3){
			return new Izhikevich9pModel3C(3);
		}
		if(nComp==4){
			return new Izhikevich9pModel4C(4);
		}
		System.out.println("rightModel needs to be instantiated!!");
		return null;	
	}
	
}
