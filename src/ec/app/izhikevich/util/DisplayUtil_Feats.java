package ec.app.izhikevich.util;

import java.io.File;

import ec.app.izhikevich.ModelEvaluatorWrapper;
import ec.app.izhikevich.evaluator.ModelEvaluatorMC;
import ec.app.izhikevich.evaluator.MultiCompConstraintEvaluator;
import ec.app.izhikevich.evaluator.qualifier.StatAnalyzer;
import ec.app.izhikevich.inputprocess.labels.PatternFeatureID;
import ec.app.izhikevich.model.Izhikevich9pModel1C;
import ec.app.izhikevich.model.Izhikevich9pModel3C;
import ec.app.izhikevich.model.Izhikevich9pModel3C_L2;
import ec.app.izhikevich.model.Izhikevich9pModel4C;
import ec.app.izhikevich.model.Izhikevich9pModelMC;
import ec.app.izhikevich.model.neurontypes.mc.EAGenes;
import ec.app.izhikevich.model.neurontypes.mc.OneNeuronInitializer;
import ec.app.izhikevich.spike.labels.SpikePatternClass;
import ec.app.izhikevich.starter.ECJStarterV2;

public class DisplayUtil_Feats {
	static int nComp;
	private static int[] forConnIdcs;// = new int[] {0,0};//new int[]{0,0,0};;//new int[] {0,0,0,2};//
	//public static int fitnessLine;
	
	public static int nJobs;
	public static int nGens;
	public static String opFolder;
	public static boolean iso_comp;
	public static int forceTrial;
	
	static {
		StatAnalyzer.display_stats = false;		
		String phen_category = ECJStarterV2.Phen_Category;
		String phen_num = ECJStarterV2.Phen_Num;
		String Neur = ECJStarterV2.Neur;//"N2";		
		iso_comp = ECJStarterV2.iso_comp;
		String exp = "1"; 
		
		forceTrial = -1;
		
		nComp = ECJStarterV2.N_COMP;
		forConnIdcs = ECJStarterV2.CONN_IDCS;
				 
		
		opFolder =phen_category+"/"+phen_num+"/"+Neur+"/"+exp;
		
		/*	if(!ECJStarterV2.MULTI_OBJ)		fitnessLine = nGens*7 + 5;
		//+1; // if single objective terminated in the middle
	else		fitnessLine = nGens*11 - 1;
*/	 
	}
	
	public static void main(String[] args) {		
		File file = new File("output/"+opFolder);
		File[] files = file.listFiles();
		int jCount =0;
		for(File f: files){
			if(f.getName().endsWith(".Full"))
				jCount++;
		}
		nJobs = jCount;
		
		OneNeuronInitializer.init(nComp, forConnIdcs, ECJStarterV2.PRIMARY_INPUT, iso_comp);
		int idx = 0;
		if(!ECJStarterV2.MULTI_OBJ)
		for(int i=0;i<nJobs;i++)
		{
			idx = i;
			if(forceTrial>-1){
				idx=forceTrial;				
			}		
			
			double[] parms = readBestParms(opFolder, idx);
			
			System.out.print(i+"\t");			
			runForFeats(parms);
			System.out.println();
			
			if(forceTrial>-1){
				break;				
			}
		}
	}

	
	private static void runForFeats(double[] parms){
		 Izhikevich9pModelMC model = getRightInstanceForModel();   		 
	        EAGenes genes = new EAGenes(parms, iso_comp);        
	        model.setK(genes.getK());
			model.setA(genes.getA());
			model.setB(genes.getB());
			model.setD(genes.getD());	
			model.setcM(genes.getCM());
			model.setvR(genes.getVR());
			model.setvT(genes.getVT());		
			model.setvMin(genes.getVMIN());	
	        model.setvPeak(genes.getVPEAK());
	        model.setG(genes.getG()); 
	        model.setP(genes.getP());   
	        double[] currents = genes.getI();
	   
	        double[] weights = new double[model.getNCompartments()-1];//genes.getW();
	        for(int wi=0;wi<weights.length;wi++){
	        	weights[wi]=1;
	        }
	        
	        ModelEvaluatorMC evaluator = new ModelEvaluatorMC(model,
	        									ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS,
	        									ModelEvaluatorWrapper.INPUT_PHENOTYPE_CONSTRAINT,
	        									ModelEvaluatorWrapper.INPUT_PAT_REP_WEIGHTS,
	        									ModelEvaluatorWrapper.INPUT_MC_CONS,
	        									currents, weights);    
	        evaluator.setRampRheo(false);
	        evaluator.setDisplayAll(false);	
	        evaluator.setDisplayOnlyClass(false);
	        float f = evaluator.getFitness();
	        
	        SpikePatternClass _class = evaluator.getSpEvalHolder().patternClassifier.getSpikePatternClass();
						
			
			double m1 = evaluator.getSpEvalHolder().sfa_m1();
			double c1 = evaluator.getSpEvalHolder().sfa_c1();
			int n_isis1 = 0;//evaluator.getSpEvalHolder().n_ISIs(1);
			
			double fsl = evaluator.getSpEvalHolder().fsl();
			double pss = evaluator.getSpEvalHolder().pss();
			
			double m2 = evaluator.getSpEvalHolder().sfa_m2();
			double c2 = evaluator.getSpEvalHolder().sfa_c2();
			int n_isis2 = 0;//evaluator.getSpEvalHolder().n_ISIs(2);
			
			System.out.print(_class+"\t"+f+"\t"+fsl+"\t"+pss+"\t"+m1+"\t"+c1+"\t"+n_isis1+"\t"+m2+"\t"+c2+"\t"+n_isis2);
	}
	
	
	
	public static double[] readBestParms(String exp, int best_trial){
		double[] parms = ECStatOutputReader.readBestSolution("output/"+exp+"/job."+best_trial+".Full", 50);//"C:\\Users\\Siva\\TIME\\ECJ Izhikevich\\output\\I2, 3 - BEST so far\\job."+best_trial+".Full",6,11);//	
		return parms;
	}
	
	private static int findNGen(String exp, int best_trial){
		return ECStatOutputReader.findNGen("output/"+exp+"/job."+best_trial+".Stat");//"C:\\Users\\Siva\\TIME\\ECJ Izhikevich\\output\\I2, 3 - BEST so far\\job."+best_trial+".Full",6,11);//	
	}
	
	private static double[][] readParmsOfParetoFront(String exp, int trial,  int nObj){
		int nSolnsOnFront = ECStatOutputReader.readParetoFronts("output/"+exp+"/job."+trial+".pareto", nObj).length;
		return ECStatOutputReader.readParetoBestSolutions("output/"+exp+"/job."+trial+".Full", nSolnsOnFront, 50);
	}
	
	public static Izhikevich9pModelMC readModel(String exp, int trial){
		File newFile =new File("output/"+exp+"/job."+trial+".Full"); 
		if(!(newFile).exists()) 
		{ System.out.println("file not found! skipping.." +"file: "+ newFile.getAbsolutePath());return null;}
		double[] parms =  ECStatOutputReader.readBestSolution("output/"+exp+"/job."+trial+".Full",  50);
		Izhikevich9pModelMC model = getRightInstanceForModel();    			 
        EAGenes genes = new EAGenes(parms, iso_comp);        
        model.setK(genes.getK());
		model.setA(genes.getA());
		model.setB(genes.getB());
		model.setD(genes.getD());	
		model.setcM(genes.getCM());
		model.setvR(genes.getVR());
		model.setvT(genes.getVT());		
		model.setvMin(genes.getVMIN());	
        model.setvPeak(genes.getVPEAK());
        model.setG(genes.getG()); 
        model.setP(genes.getP());   
        double[] I = genes.getI();
       // System.out.println(I[0]);
        model.setInputParameters(new double[] {I[0]}, 0d, 0d);
        return model;
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
	
}
