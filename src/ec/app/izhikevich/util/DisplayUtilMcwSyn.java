package ec.app.izhikevich.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import ec.app.izhikevich.ModelEvaluatorWrapper;
import ec.app.izhikevich.evaluator.ModelEvaluatorMC;
import ec.app.izhikevich.evaluator.MultiCompConstraintEvaluator;
import ec.app.izhikevich.evaluator.qualifier.StatAnalyzer;
import ec.app.izhikevich.inputprocess.labels.MCConstraintAttributeID;
import ec.app.izhikevich.inputprocess.labels.PatternFeatureID;
import ec.app.izhikevich.model.Izhikevich9pModel;
import ec.app.izhikevich.model.Izhikevich9pModel1C;
import ec.app.izhikevich.model.Izhikevich9pModel3C;
import ec.app.izhikevich.model.Izhikevich9pModel4C;
import ec.app.izhikevich.model.Izhikevich9pModelMC;
import ec.app.izhikevich.model.neurontypes.mc.EAGenes;
import ec.app.izhikevich.model.neurontypes.mc.OneNeuronInitializer;
import ec.app.izhikevich.resonate.Bifurcation;
import ec.app.izhikevich.starter.ECJStarterV2;

public class DisplayUtilMcwSyn {		
	static boolean displayOnlyFitness = false;
	static boolean writeBestParmsAsCSV = true;
	static boolean displayParms = true;
	static boolean displayErrors = true;	
	static boolean plotVtraces = true;	
	public static final boolean displayPatternForExternalPlot = false;
	
	private static String VOLTAGE_FILENAME_PFX;
	
	static int nComp;
	public static int nPops = 1;
	
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
		String exp = "0"; 
		
		forceTrial =-1;
		
		nComp = ECJStarterV2.N_COMP;
		forConnIdcs = ECJStarterV2.CONN_IDCS;
				 
		
		opFolder =phen_category+"/"+phen_num+"/"+Neur+"/"+exp;
		
		VOLTAGE_FILENAME_PFX = "output/"+opFolder+"/forExternalPlottingTemp";
		
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
		
		
		if(!displayOnlyFitness && nPops ==1){
			int trial = getBestTrial(nJobs, opFolder);
		}
	
		OneNeuronInitializer.init(nComp, forConnIdcs, ECJStarterV2.PRIMARY_INPUT, iso_comp);
	/*	if(writeBestParmsAsCSV) {
			ArrayList<double[]> parmslist = readAllParms(nJobs);
			writeBestModelsAsCsv(parmslist);
			System.exit(0);
		}	*/
		//double[] nGens = new double[nJobs];
		int idx = 0;
		if(!ECJStarterV2.MULTI_OBJ)
		for(int i=0;i<nJobs;i++)
		{
			//if(i<=70) continue;
			idx = i;
			if(forceTrial>-1){
				idx=forceTrial;				
			}
		
			if(!displayOnlyFitness && nPops ==1){
				System.out.println("\n******************************************************************");
				System.out.println("******************************************************************\t"+idx);
				System.out.println("******************************************************************");
			}
			if(nComp==1 || nPops ==1){
				double[] parms = readBestParms(opFolder, idx);				
				//runWithUserParmValues();
				if(displayOnlyFitness){
					System.out.print(i+"\t");
				}
				if(ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS[0].getFeaturesToEvaluate().contains(PatternFeatureID.period)){
					runSecondary(parms, opFolder+"/"+idx, displayParms, displayErrors, plotVtraces);
				}else
					runPrimary(idx, parms, opFolder+"/"+idx, displayParms, displayErrors, plotVtraces);
			//	writeParmsIntoFile(parms, opFolder, idx); //no need for 1c;
				if(forceTrial>-1){
					break;				
				}
			}else{
				double[][] parms = readBestParmsS(opFolder, nPops);				
				//runWithUserParmValues();				
				for(int j=0;j<nPops;j++){
					if(displayOnlyFitness){
						System.out.print(i+"\t");
					}
					runPrimary(idx, parms[j], opFolder+"/"+idx, displayParms, displayErrors, plotVtraces);
					writeParmsSIntoFile(parms, opFolder);
				}
		}
		}
		
	/*	double meanNGens = StatUtil.calculateMean(nGens);
		double nGensSD = StatUtil.calculateStandardDeviation(nGens, meanNGens);
		System.out.println("\nNGens.\t\tMean: "+meanNGens
				+"\tSD: "+nGensSD
				+"\tMin: "+GeneralUtils.findMin(nGens)
				+"\tMax: "+GeneralUtils.findMax(nGens));
		*/
		//int[] js = {39,33};
		if(ECJStarterV2.MULTI_OBJ)			
		for(int i=0;i<nJobs;i++)		
		{
			//if(i<7) continue;
			//int i=js[0];
			if(!displayOnlyFitness){
				System.out.println("\n******************************************************************");
				System.out.println("******************************************************************\t"+i);
				System.out.println("******************************************************************");	
			}
			int nObj = ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS.length; //dummy for reading pareto file
			double[][] parms = readParmsOfParetoFront(opFolder, i, nObj);
			
			System.out.println("\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\");	
			System.out.println(parms.length+" solutions on Pareto Front");
			System.out.println("\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\");	
			for(int j=0;j<parms.length;j++)
			{
			//	int j=js[1];
				System.out.println("\t\t\t\t\\\\\\\\\\\\\\\\Soln. "+j +"////////////");
				runPrimary(i, parms[j], opFolder+"/"+i+"_"+j, displayParms, displayErrors, plotVtraces);
			}
			System.out.println("End of pareto Front\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\");
		}
	}

	private static ArrayList<double[]> readAllParms(int nJobs){
		ArrayList<double[]> parmslist = new ArrayList<>();
		for(int i=0;i<nJobs;i++) {
			double[] parms = readBestParms(opFolder, i);
			parmslist.add(parms);
		}
		return parmslist;
	}
	
	private static void writeParmsIntoFile(double[] parms, String opFolder, int Jidx){
		try{
			FileWriter fw = new FileWriter(new File("output\\"+opFolder+"\\bestGenes_"+Jidx));		
			for(int i=0;i<EAGenes.geneLength;i++){
				fw.write(String.valueOf(parms[i]));
				if(i<EAGenes.geneLength-1)
					fw.write(",");				
			}
			fw.close();
		}catch(IOException io){
			io.printStackTrace();
		}
	}
	private static void writeParmsSIntoFile(double[][] parms, String opFolder){
		try{
			FileWriter fw = new FileWriter(new File("output\\"+opFolder+"\\bestGenes_0"));	
			for(int n=0;n<parms.length;n++){
				for(int i=0;i<EAGenes.geneLength;i++){
					fw.write(String.valueOf(parms[n][i]));
					if(i<EAGenes.geneLength-1)
						fw.write(",");				
				}
				fw.write("\n");
			}			
			fw.close();
		}catch(IOException io){
			io.printStackTrace();
		}
	}
	private static void writeBestModelsAsCsv(ArrayList<double[]> parms){	  
	        try {
				FileWriter fw = new FileWriter("bestParms_all.csv");
				for(int i=0; i<parms.size();i++) {
					GeneralUtils.displayArray(parms.get(i));
					EAGenes genes = new EAGenes(parms.get(i), iso_comp);     
					fw.write(i+","+genes.getK()[0]);
					fw.write(","+genes.getA()[0]);
					fw.write(","+genes.getB()[0]);
					fw.write(","+genes.getD()[0]);
					fw.write(","+genes.getCM()[0]);
					fw.write(","+genes.getVR());
					fw.write(","+genes.getVT()[0]);
					fw.write(","+genes.getVMIN()[0]);
					fw.write(","+genes.getVPEAK()[0]);
					
					fw.write(","+genes.getI()[0]);
					fw.write(","+genes.getI()[1]);
					
					fw.write(",1\n");
				}
				fw.flush();
				fw.close();
					
	        } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	private static void writeBestModelAndClassAsCsv(int trial, double[] parms, String _class, double fitness, 
									 boolean append){	  
        try {
        		String newFileName = "output/"+opFolder+"/bestAll.csv";
        		File fl = new File(newFileName);
        		if(!fl.exists()) {
        			append=false;//fl.createNewFile();
        		
        		}
			FileWriter fw = new FileWriter(newFileName, append);
			
			EAGenes genes = new EAGenes(parms, iso_comp);     
			fw.write(trial+","+genes.getK()[0]);
			fw.write(","+genes.getA()[0]);
			fw.write(","+genes.getB()[0]);
			fw.write(","+genes.getD()[0]);
			fw.write(","+genes.getCM()[0]);
			fw.write(","+genes.getVR());
			fw.write(","+genes.getVT()[0]);
			fw.write(","+genes.getVMIN()[0]);
			fw.write(","+genes.getVPEAK()[0]);
			
			fw.write(","+genes.getI()[0]);
			fw.write(","+_class+","+fitness+"\n");
			
			fw.flush();
			fw.close();
				
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}
	private static void runPrimary(int trial, double[] parms, String opFolder, boolean displayParms, boolean displayErrors, boolean drawPlots){
		 Izhikevich9pModelMC model = getRightInstanceForModel();        
		 float fitness=0;
		 String _class="";
		 
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
	        //currents[0] = 290;
	        //currents[1] = currents[0] + 200;
	  //      float[] newCurrents = new float[currents.length];
	   //     for(int i=0;i<newCurrents.length;i++)		
	  //      		newCurrents[i] = currents[i];
	       
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
	        if(displayErrors || displayOnlyFitness){
	        	if(displayOnlyFitness){
	        		evaluator.setDisplayAll(false);	
	        		evaluator.setDisplayOnlyClass(true);
	        	}else{
	        		evaluator.setDisplayAll(true);	
	        	}
	        	//evaluator.setDisplayForPlotIdx(0);
				 fitness = evaluator.getFitness();	
				 _class = evaluator.getSpEvalHolder().patternClassifier.getSpikePatternClass().toString();
				if(displayOnlyFitness){
					System.out.print(
							"\t"+fitness+"\t");					
				}
				//System.out.println();
				
				//evaluator.get
				double[] spikeTimes = evaluator.getModelSomaSpikePatternHolder().getISIs();
				spikeTimes = GeneralUtils.roundOff(spikeTimes);
				GeneralUtils.displayArray(spikeTimes);
	        }	        		
		
	        if(drawPlots){
	        		drawPlots( model,  currents,  evaluator);
	        }
			if(displayParms) {
				for(int idx=0;idx<model.getNCompartments();idx++){
					displayForC(model, idx);
				}
				for(int idx=0;idx<model.getNCompartments()-1;idx++){
					System.out.println("Gt_"+(idx+1)+"="+model.getG()[idx]+"/ms");
					System.out.println("P_"+(idx+1)+"="+model.getP()[idx]);
					System.out.println("W"+idx+"="+weights[idx]);
				}
				//System.out.println();
				//GeneralUtils.displayArrayUnformatWithSpace(parms);
			}
			if(writeBestParmsAsCSV) {
				writeBestModelAndClassAsCsv( trial, parms,  _class,  fitness, 
						 true );
			}
					
	}
	
	private static void drawPlots(Izhikevich9pModelMC model, double[] currents, ModelEvaluatorMC evaluator) {
		
			//float I = currents[0];
			double[][] Is = null;
			double[] Idurs = null;
			if(model.getNCompartments()>1 ) {
				Idurs = new double[ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS.length +4 ]; //exc., ir., sp., EPSP
				int i;
				for( i=0;i<ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS.length;i++){
					Idurs[i] = (float)ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS[i].getCurrentDuration();
				}			
				if(!ModelEvaluatorWrapper.ISO_COMPS){
					Idurs[i++]=(float) ModelEvaluatorWrapper.INPUT_MC_CONS[0].getAttribute(MCConstraintAttributeID.current_duration); //exc.
					Idurs[i++]=(float) ModelEvaluatorWrapper.INPUT_MC_CONS[1].getAttribute(MCConstraintAttributeID.current_duration); //ir.
					Idurs[i++]=(float) ModelEvaluatorWrapper.INPUT_MC_CONS[2].getAttribute(MCConstraintAttributeID.dend_current_duration); //sp.
					Idurs[i++]=(float) ModelEvaluatorWrapper.INPUT_MC_CONS[3].getAttribute(MCConstraintAttributeID.sim_duration); //epsp.
				}
				
				//*** similarly additional mc currents
				double[] somaCurrents = new double[currents.length +4];
				double[] dend1Currents = new double[currents.length +4];
			    for(i=0;i<currents.length;i++){
			    	somaCurrents[i] = currents[i];//somatic scenarios
			    	dend1Currents[i]=0; //no dend current for somatic scenarios
			    }				    
			    double[] rheoComp = model.getRheoBases();
			    somaCurrents[i] = rheoComp[0]; //exc.
			    dend1Currents[i++] = rheoComp[1]; // exc. 
			    
			    if(!ModelEvaluatorWrapper.ISO_COMPS){
			    somaCurrents[i] = (double) ModelEvaluatorWrapper.INPUT_MC_CONS[1].getAttribute(MCConstraintAttributeID.current);//ir
			    dend1Currents[i++] = (double) ModelEvaluatorWrapper.INPUT_MC_CONS[1].getAttribute(MCConstraintAttributeID.current);//ir
			    
			    			    
			    somaCurrents[i] = 0; // indirectly get the I required for single spike prop as below:	(have to go through unnecessary steps?!)	
			    float dend_curr_min = (float)ModelEvaluatorWrapper.INPUT_MC_CONS[2].getAttribute(MCConstraintAttributeID.dend_current_min);
				float dend_curr_max = (float)ModelEvaluatorWrapper.INPUT_MC_CONS[2].getAttribute(MCConstraintAttributeID.dend_current_max);
				float dend_current_time_min = (float)ModelEvaluatorWrapper.INPUT_MC_CONS[2].getAttribute(MCConstraintAttributeID.dend_current_time_min);
				float dend_current_duration = (float)ModelEvaluatorWrapper.INPUT_MC_CONS[2].getAttribute(MCConstraintAttributeID.dend_current_duration);
				float dend_current_step = (float)ModelEvaluatorWrapper.INPUT_MC_CONS[2].getAttribute(MCConstraintAttributeID.dend_current_step);
				float dend_target_spike_freq = (float)ModelEvaluatorWrapper.INPUT_MC_CONS[2].getAttribute(MCConstraintAttributeID.dend_target_spike_freq);
				for(int c=1;c<model.getNCompartments();c++){
					float[] spikeCounts = evaluator.getMcEvalholder().propagatedSpikeCounts(c, MultiCompConstraintEvaluator.forwardConnectionIdcs[c], 
																dend_curr_min, 
																dend_curr_max,
																dend_current_time_min,
																dend_current_duration,
																dend_current_step,
																dend_target_spike_freq);						
					dend1Currents[i++] = spikeCounts[2]; //MUST HAVE 2D ARRAY for dend currents for more than 2 comps!!!!
			    }
			    }
			    somaCurrents[i] = 0; //epsp; syn simulation
			    dend1Currents[i++] = 0; //epsp; syn simulation
			
			    Is = new double[model.getNCompartments()][];
			    Is[0]=somaCurrents;
			    Is[1]=dend1Currents;
				
			}else{
				Is = new double[1][];
				Idurs = new double[ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS.length];
				for(int i=0;i<ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS.length;i++){
					Idurs[i] = (float)ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS[i].getCurrentDuration();
				}
				Is[0]=currents;
			}
			
			double[]  times = evaluator.getModelSomaSpikePatternHolder().getModelSpikePatternData().getTime();
			double[]  vs = evaluator.getModelSomaSpikePatternHolder().getModelSpikePatternData().getVoltage();
			
			BrianInvoker invoker = new BrianInvoker(opFolder, currents.length);//new BrianInvoker(opFolder, Is, Idurs);				
			invoker.setDisplayErrorStream(true);
			
			File tempFileT = new File(VOLTAGE_FILENAME_PFX+"_t");
			File tempFileV = new File(VOLTAGE_FILENAME_PFX+"_v");
			
			GeneralFileWriter.write(tempFileT.getAbsolutePath(), times);
			GeneralFileWriter.write(tempFileV.getAbsolutePath(), vs);
			
			String doShow = "0";
			if(forceTrial>-1)
				doShow = "1";
			invoker.invoke(VOLTAGE_FILENAME_PFX, doShow);//invoker.invoke(model);
			
			tempFileT.delete();
			tempFileV.delete();
			
	}
	/*
	 * chaos evaluation for periods
	 */
	private static void runSecondary(double[] parms, String opFolder, boolean displayParms, boolean displayErrors, boolean drawPlots){
		Izhikevich9pModel model = new Izhikevich9pModel();	
		
		EAGenes genes = new EAGenes(parms, iso_comp);  
		
		model.setK(genes.getK()[0]);
		model.setA(genes.getA()[0]);
		model.setB(genes.getB()[0]);
		model.setD(genes.getD()[0]);	
		model.setcM(genes.getCM()[0]);
		model.setvR(genes.getVR());
		model.setvT(genes.getVT()[0]);		
		model.setvMin(genes.getVMIN()[0]);	
        model.setvPeak(genes.getVPEAK()[0]);
        
        
        Bifurcation bf = new Bifurcation(model, genes.getI()[0]);		
		int period = bf.identifyPeriod(bf.cutSSPcareU(model.getvPeak()-20));
		double freq = bf.getSpattern().getFiringFrequencyBasedOnISIs();
		float normFactor;
		if(freq<.1d){
			normFactor = 1f;
		}else{
			normFactor = (float)Math.log10(freq);
		}
		
		System.out.println("Period: "+ period);
		System.out.println("Frequency (spike): "+ freq+", log10:  "+(float)Math.log10(freq));
		System.out.println("fitness: "+ (period*1f/normFactor));
		
		if(displayParms) {
			System.out.println("\nI.\t"+genes.getI()[0]);
			displayForBrian(model);
		}
	}
	private static int getBestTrial(int nJobs, String nodes){
		float[] fitness = new float[nJobs];
		System.out.println("Jobs Fitness:");
		for(int job = 0; job<nJobs; job++) {
			fitness[job] = ECStatOutputReader.readBestFitness("output/"+nodes+"/job."+job+".Full");//"C:\\Users\\Siva\\TIME\\ECJ Izhikevich\\output\\I2, 3 - BEST so far\\job."+job+".Full", 5);//
			//fitness[job] = ECStatOutputReader.readBestFitness("output/"+nodes+"/$Full_0_"+job, 5);//"C:\\Users\\Siva\\TIME\\ECJ Izhikevich\\output\\I2, 3 - BEST so far\\job."+job+".Full", 5);//
			
			System.out.println(job+"\t"+fitness[job]);
		}
		//find min fitness:
		float maxFit = -Float.MAX_VALUE;
		int maxFitJob = -1;
		for(int job =0; job< fitness.length; job++) {
			if(fitness[job] > maxFit) {
				maxFit = fitness[job];
				maxFitJob = job;
			}
		}
		System.out.println("Best Job:\t"+maxFitJob);;
		System.out.println("**********************************************");
		
		return maxFitJob;
	}
	
	public static double[] readBestParms(String exp, int best_trial){
		double[] parms = ECStatOutputReader.readBestSolution("output/"+exp+"/job."+best_trial+".Full", 50);//"C:\\Users\\Siva\\TIME\\ECJ Izhikevich\\output\\I2, 3 - BEST so far\\job."+best_trial+".Full",6,11);//	
		return parms;
	}
	
	public static double[][] readBestParmsS(String exp, int nSols){
		double[][] parms = ECStatOutputReader.readBestSolutionS("output/"+exp+"/job.0.Full", 50, nSols);//"C:\\Users\\Siva\\TIME\\ECJ Izhikevich\\output\\I2, 3 - BEST so far\\job."+best_trial+".Full",6,11);//	
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
	private static void displayForBrian(EAGenes genes, int idx) {
		String forBrian ="";		
		forBrian = "\n\nk"+idx+"="+genes.getK()[idx]+"/ms/mV\na"+idx+"="+genes.getA()[idx]+"\nb"+idx+"="+genes.getB()[idx]+
				"\nd"+idx+"="+genes.getD()[idx]+"\nC"+idx+"="+genes.getCM()[idx]+"\nvR"+idx+"="+genes.getVR()+"*mV\nvT"+idx+"="+genes.getVT()[idx]+
				"*mV\nvPeak"+idx+"="+genes.getVPEAK()[idx]+"*mV\nc"+idx+"="+genes.getVMIN()[idx]+"*mV";
		System.out.println(forBrian);
	}
	private static void displayForBrian(Izhikevich9pModelMC model, int idx) {
		String forBrian ="";		
		forBrian = "\n\nk"+idx+"="+model.getK()[idx]+"/ms/mV\na"+idx+"="+model.getA()[idx]+"\nb"+idx+"="+model.getB()[idx]+
				"\nd"+idx+"="+model.getD()[idx]+"\nC"+idx+"="+model.getcM()[idx]+"\nvR"+idx+"="+model.getvR()[idx]+"*mV\nvT"+idx+"="+model.getvT()[idx]+
				"*mV\nvPeak"+idx+"="+model.getvPeak()[idx]+"*mV\nc"+idx+"="+model.getvMin()[idx]+"*mV";
		System.out.println(forBrian);
	}
	private static void displayForC(Izhikevich9pModelMC model, int idx) {
		String forC ="";		
		forC = "\n\nk"+idx+"="+model.getK()[idx]+";\na"+idx+"="+model.getA()[idx]+";\nb"+idx+"="+model.getB()[idx]+
				";\nd"+idx+"="+model.getD()[idx]+";\nC"+idx+"="+model.getcM()[idx]+";\nvR"+idx+"="+model.getvR()[idx]+";\nvT"+idx+"="+model.getvT()[idx]+
				";\nvPeak"+idx+"="+model.getvPeak()[idx]+";\nc"+idx+"="+model.getvMin()[idx]+";";
		System.out.println(forC);
	}
	private static void displayForBrian(Izhikevich9pModel model) {
		String forBrian ="";		
		int idx = 0;
		forBrian = "\n\nk"+idx+"="+model.getK()+"/ms/mV\na"+idx+"="+model.getA()+"\nb"+idx+"="+model.getB()+
				"\nd"+idx+"="+model.getD()+"\nC"+idx+"="+model.getcM()+"\nvR"+idx+"="+model.getvR()+"*mV\nvT"+idx+"="+model.getvT()+
				"*mV\nvPeak"+idx+"="+model.getvPeak()+"*mV\nc"+idx+"="+model.getC()+"*mV";
		System.out.println(forBrian);
	}
	private static void runWithUserParmValues(){
		Izhikevich9pModelMC model = getRightInstanceForModel();
		double k0, k1, k2;
		double a0, a1, a2;
		double b0, b1, b2;
		double d0, d1, d2;
		double C0, C1, C2;
		double vR0;
		double vT0,vT1, vT2;
		double vPeak0, vPeak1, vPeak2;
		double c0, c1, c2;
		
		double Gt_1, P_1, Gt_2, P_2;
		double[] newCurrents = new double[]{408};//, currents[1]};

		k0=1.029252f;
				a0=0.0015812784f;
				b0=2.3435593f;
				d0=3.0f;
				C0=85.0f;
				vR0=-60.772045f;
				vT0=-30.712957f;
				vPeak0=19.3511f;
				c0=-53.59736f;

						
        model.setK(getDouble1dArray(k0));
		model.setA(getDouble1dArray(a0));
		model.setB(getDouble1dArray(b0));
		model.setD(getDouble1dArray(d0));	
		model.setcM(getDouble1dArray(C0));
		model.setvR(vR0);
		model.setvT(getDouble1dArray(vT0));		
		model.setvMin(getDouble1dArray(c0));	
        model.setvPeak(getDouble1dArray(vPeak0));
        model.setG(getDouble1dArray(0)); 
        model.setP(getDouble1dArray(0));   
        
        
        double[] weights = new double[]{};
        ModelEvaluatorMC evaluator = new ModelEvaluatorMC(model,
        			ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS,
        			ModelEvaluatorWrapper.INPUT_PHENOTYPE_CONSTRAINT,
					ModelEvaluatorWrapper.INPUT_PAT_REP_WEIGHTS,
					ModelEvaluatorWrapper.INPUT_MC_CONS,
					newCurrents, weights);      
		evaluator.setDisplayAll(true);	
		evaluator.getFitness();
		System.out.println("\nSpike Times: ");
		GeneralUtils.displayArray(evaluator.getModelSomaSpikePatternHolder().getSpikeTimes());
      
	}
	private static float[] getMirroredDendComp(float[] genes){
		float[] newGenes = new float[genes.length+1];
		for(int i=0;i<genes.length;i++){
			newGenes[i]=genes[i];
		}
		newGenes[newGenes.length-1]=genes[genes.length-1];
		return newGenes;
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
	private static double[] getDouble1dArray(double f){
		return new double[]{f};
	}
	private static float[] getFloat1dArray(float f1, float f2){
		return new float[]{f1,f2};
	}
	private static float[] getFloat1dArray(float f1, float f2, float f3){
		return new float[]{f1,f2,f3};
	}
}
