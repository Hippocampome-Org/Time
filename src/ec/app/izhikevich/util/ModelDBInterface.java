package ec.app.izhikevich.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import ec.app.izhikevich.ModelEvaluatorWrapper;
import ec.app.izhikevich.evaluator.ModelEvaluatorMC;
import ec.app.izhikevich.evaluator.MultiCompConstraintEvaluator;
import ec.app.izhikevich.inputprocess.InputSpikePatternConstraint;
import ec.app.izhikevich.model.Izhikevich9pModel;
import ec.app.izhikevich.model.Izhikevich9pModel1C;
import ec.app.izhikevich.model.Izhikevich9pModel3C;
import ec.app.izhikevich.model.Izhikevich9pModel3C_L2;
import ec.app.izhikevich.model.Izhikevich9pModel3CwSyn;
import ec.app.izhikevich.model.Izhikevich9pModel3CwSyn_L2;
import ec.app.izhikevich.model.Izhikevich9pModel4C;
import ec.app.izhikevich.model.Izhikevich9pModel4CwSyn;
import ec.app.izhikevich.model.Izhikevich9pModelMC;
import ec.app.izhikevich.model.Izhikevich9pModelMCwSyn;
import ec.app.izhikevich.model.IzhikevichSolver;
import ec.app.izhikevich.model.IzhikevichSolverMC;
import ec.app.izhikevich.model.neurontypes.mc.EAGenes;
import ec.app.izhikevich.model.neurontypes.mc.OneNeuronInitializer;
import ec.app.izhikevich.outputprocess.CarlOutputParser;
import ec.app.izhikevich.spike.SpikePatternAdapting;
import ec.app.izhikevich.starter.ECJStarterV2;
import ec.app.izhikevich.util.analysis.NeuronPageEntries;
import ec.app.izhikevich.util.forportal.ModelDataStructure;
import ec.util.MersenneTwister;

public class ModelDBInterface {		
	public static final double MC_RHEO_DUR=100;
	
	public static final double MC_IR_I=-100;
	public static final double MC_IR_DUR=1000;
	
	public static final double MC_SP_DUR=1000;
	public static final double MC_SP_TIME_MIN=50;
	public static final double MC_SP_I_MIN=500;
	public static final double MC_SP_I_STEP=100;
	
	public static final double MC_EPSP_DUR=100;
	public static final float MC_EPSP_TC=5f;
	
	public static void main(String[] args) {
		boolean save_plot = true;
		String primary_input=ECJStarterV2.raw_primary_input;//"11,3b,6-018-1a,3,0,0,1";//"11,1c,1-000-2,2,0,0";
		String exp="mc0";
		int job=0;		
		int nsubPop=10;
		int nthsol=5;
		
		ModelDBInterface mi = new ModelDBInterface(primary_input, exp, job, save_plot);
		
		
		
		mi.runModelAndPlot(false, nsubPop, nthsol);		
	}
	//String primaryInput;
	String exp;
	int job;
	boolean savePlot;
	
	String rootDir;
	String phenotypeDir;
	String neuronSubtypeID;
	private String uniqueID;
	int nComp;
	int[] connIdcs;
	boolean isoComp;
	
	public ModelDBInterface(String primary_input, String exp, int job, boolean save_plot){
		//System.out.println(primary_input);
		StringTokenizer st = new StringTokenizer(primary_input, ",");
		rootDir = st.nextToken();
		phenotypeDir = st.nextToken();
		neuronSubtypeID = st.nextToken();
		
		nComp = Integer.valueOf(st.nextToken());
		connIdcs = new int[nComp];
		for(int i=0;i<nComp;i++)
			connIdcs[i]=Integer.valueOf(st.nextToken());
		if(st.hasMoreTokens()){
			isoComp = Boolean.valueOf(st.nextToken());
		}
		this.exp=exp;
		this.job=job;
		this.savePlot=save_plot;
		
		ECJStarterV2.iso_comp = isoComp;
		OneNeuronInitializer.init(nComp, connIdcs, "input/"+rootDir+"/"+phenotypeDir+"/"+neuronSubtypeID+".json", isoComp);
	}
	
	public ModelDataStructure createModelDataStructure( String name, int nsubPop, int nthSol) {
		String ecj_opfile ="";
		double[] parms = null;
		CarlOutputParser parser = null;
		
			if(nsubPop==1) {
				ecj_opfile="output/"+rootDir+"/"+phenotypeDir+"/"+neuronSubtypeID+"/"+exp+"/job."+job+".Full";
				parms = ECStatOutputReader.readBestSolution(ecj_opfile, 50);
				parser = null;
			}else { //OR --> if(exp.equals("sp"))
				String dir = "C:\\Users\\sivav\\Projects\\TIMES\\output\\11\\mcdone_noplot\\results_"+rootDir+"_"+phenotypeDir+"_"+neuronSubtypeID;
				ecj_opfile=dir+"\\job.0.Full";
				parms = ECStatOutputReader.readBestSolutionS(ecj_opfile, 50, nsubPop)[nthSol];
				/*
				 * new addition for MC analysis
				 */
				String phenotypeFileName = dir+"\\"+nthSol+"_phenotype";
				parser = new CarlOutputParser(phenotypeFileName, false); 
				
			}		
		
		
		Izhikevich9pModelMC model = getRightInstanceForModel();   
		model.setIso_comp(isoComp);
		EAGenes genes = new EAGenes(parms, isoComp);        
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
       // double[] curr_durs = genes.get
        double[] weights = genes.getW();
        ModelDataStructure mds = new ModelDataStructure(uniqueID, neuronSubtypeID, name, model, currents);
        mds.Idurs = genes.getIDurs();
        mds.setCarlOutputParser(parser);
        mds.setMcLayoutCode(mclayoutcode());
        return mds;
	}
	
	/*
	 * currently (reading many sample models for a neuron type is) only for single compartment models
	 */
	public List<ModelDataStructure> createModelDataStructure_manySampleModels( String name, int nsamples) {
		List<ModelDataStructure> mdsList = new ArrayList<>();
		int[] sampleJobs = new int[nsamples];
		sampleJobs[0] = job; //include the best one
		
		String ecj_opDir = "output/"+rootDir+"/"+phenotypeDir+"/"+neuronSubtypeID+"/"+exp;		
		
		File dir = new File(ecj_opDir);
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {				
				return filename.endsWith(".Full");
			}			
		});
		
		if(files.length<nsamples) {
			nsamples = files.length;
		}
		// generate random job idx
		MersenneTwister mt = new MersenneTwister(System.currentTimeMillis());			
		for(int i=1; i<nsamples; i++) {
			int newJob;			
			while(true) {
				newJob = mt.nextInt(files.length);
				if(!contains(sampleJobs, i-1, newJob))
					break;
			}			
			sampleJobs[i] = newJob;
		}

		//System.out.println(name +" "+nsamples+ "  "+sampleJobs[0]);
		for(int i=0; i<nsamples; i++) {
			String ecj_opfile=ecj_opDir+"/job."+sampleJobs[i]+".Full";
			double[] parms = null;		
			parms = ECStatOutputReader.readBestSolution(ecj_opfile, 50);		
			
			
			Izhikevich9pModelMC model = getRightInstanceForModel();   
			
			EAGenes genes = new EAGenes(parms, isoComp);        
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
	        double[] weights = genes.getW();
	        ModelDataStructure mds = new ModelDataStructure(uniqueID, neuronSubtypeID, name, model, currents);
	        mdsList.add(mds);
		}        
        return mdsList;
	}
	/*
	 * if samples upto length contains item
	 */
	private boolean contains(int[] samples, int length, int item) {
		boolean cont = false;		
		for(int i=0; i<length; i++) {
			if(samples[i]==item)
				return true;
		}		
		return cont;
	}
	public ModelDataStructure createModelDataStructure(String name, double[] parms) {		
		
		Izhikevich9pModelMC model = getRightInstanceForModel();   
		model.setIso_comp(isoComp);
		int i=0;
        model.setK(new double[] {parms[i++]});
		model.setA(new double[] {parms[i++]});
		model.setB(new double[] {parms[i++]});
		model.setD(new double[] {parms[i++]});	
		model.setcM(new double[] {parms[i++]});
		model.setvR(parms[i++]);
		model.setvT(new double[] {parms[i++]});	
		model.setvPeak(new double[] {parms[i++]});
		model.setvMin(new double[] {parms[i++]});	
       
		if(model.getNCompartments()>1) {
			model.setG(new double[] {parms[i++]}); 
	        model.setP(new double[] {parms[i++]});   
		}    
		
		double[] currents = new double[parms.length-9];
		for(i=9;i<parms.length;i++) {
			currents[i-9]=parms[i];
		}
		
        
        ModelDataStructure mds = new ModelDataStructure(uniqueID, neuronSubtypeID, name, model, currents);
        
        double[] Idurs = new double[currents.length];
        for(int j=0;j<currents.length;j++) {
        	InputSpikePatternConstraint expConstraint = NeuronPageEntries.findMatchingConstraint(ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS, currents[j]);
        	Idurs[j] = expConstraint.getCurrentDuration();
        }
        
        
        mds.Idurs = Idurs;
        mds.setMcLayoutCode(mclayoutcode());
        return mds;
	}
  
public ModelDataStructure createModelDataStructure_mc(String name, double[] parms) {		
		
		Izhikevich9pModelMC model = getRightInstanceForModel();   
		model.setIso_comp(isoComp);
		EAGenes genes = new EAGenes(parms, isoComp);        
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
        
	    ModelDataStructure mds = new ModelDataStructure(uniqueID, neuronSubtypeID, name, model, currents);

        
        double[] Idurs = new double[currents.length];
        for(int j=0;j<currents.length;j++) {
        	InputSpikePatternConstraint expConstraint = NeuronPageEntries.findMatchingConstraint(ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS, currents[j]);
        	Idurs[j] = expConstraint.getCurrentDuration();
        }
        
        mds.Idurs = Idurs;
        mds.setMcLayoutCode(mclayoutcode());
        return mds;
	}

	public void runModelAndPlot(boolean rampRheo, int nsubPop, int nthSol) {
		String ecj_opfile ="";
		double[] parms = null;
		if(nsubPop==1) {
			ecj_opfile="output/"+rootDir+"/"+phenotypeDir+"/"+neuronSubtypeID+"/"+exp+"/job."+job+".Full";
			parms = ECStatOutputReader.readBestSolution(ecj_opfile, 50);
		}else {
			ecj_opfile="output/"+rootDir+"/"+phenotypeDir+"/"+neuronSubtypeID+"/"+exp+"/job.0.Full";
			parms = ECStatOutputReader.readBestSolutionS(ecj_opfile, 50, nsubPop)[nthSol];
		}
		
		String VOLTAGE_FILENAME_PFX = "output/"+rootDir+"/"+phenotypeDir+"/"+neuronSubtypeID+"/"+exp+"/forExternalPlottingTemp";
		
		Izhikevich9pModelMC model = getRightInstanceForModel();   
		
		EAGenes genes = new EAGenes(parms, isoComp);        
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
        double[] weights = genes.getW();
        
        MultiCompConstraintEvaluator.TURN_OFF_MC_ERROR_DISPLAY=true;
        ModelEvaluatorMC evaluator = new ModelEvaluatorMC(model,
				ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS,
				ModelEvaluatorWrapper.INPUT_PHENOTYPE_CONSTRAINT,
				ModelEvaluatorWrapper.INPUT_PAT_REP_WEIGHTS,
				ModelEvaluatorWrapper.INPUT_MC_CONS,
				currents, weights);    
        
        evaluator.setRampRheo(rampRheo);
        evaluator.setSaveModelPattern(true);
        evaluator.setDisplayAll(true);	
        
       // evaluator.getMcEvalholder().setSaveSP_Is_hold(true); //set within!
        
        float fitness = evaluator.getFitness();	
        System.out.println(fitness);
		//String _class = evaluator.getSpEvalHolder().patternClassifier.getSpikePatternClass().toString();
		int nScens = currents.length;
				
		String opFile="output/"+rootDir+"/"+phenotypeDir+"/"+neuronSubtypeID+"/"+exp+"/"+job;
		
		double modelvr = model.getvR()[0];
		double modelvpk = model.getvPeak()[0];
		
		PyInvoker invoker = new PyInvoker(opFile, nScens, nComp, modelvr, modelvpk, currents);			
		invoker.setDisplayErrorStream(true);	
		
		for(int i=0;i<nScens;i++) {
			double[]  times = evaluator.getModelSpikePatternHolder().get(i)[0].getModelSpikePatternData().getTime();
			File tempFileT = new File(VOLTAGE_FILENAME_PFX+"_t"+i);
			GeneralFileWriter.write(tempFileT.getAbsolutePath(), times);
			
			for(int j=0;j<nComp;j++) {
				double[]  vs = evaluator.getModelSpikePatternHolder().get(i)[j].getModelSpikePatternData().getVoltage();				
				File tempFileV = new File(VOLTAGE_FILENAME_PFX+"_v"+i+"_"+j);				
				GeneralFileWriter.write(tempFileV.getAbsolutePath(), vs);
			}			
		}
		
		/*
		 * do MC constraints simulation
		 */
		//1. rheo - excit.
		double[] rheos = model.getRheoBases();
		rheos[0]=rheos[0]+1;
	//	rheos[0]=37;
	//	rheos[1]=52;
		invoker.setRheos(rheos);
		
		SpikePatternAdapting[] modelSpikePattern = new SpikePatternAdapting[nComp];
		for(int i=0;i<nComp;i++) {
			modelSpikePattern[i]=getModelSpikePattern(model.getIsolatedCompartment(i), rheos[i], 50, MC_RHEO_DUR);
		}
		writeTsAndVssForMCConsForExcitability(modelSpikePattern, VOLTAGE_FILENAME_PFX);
		
		//2. IR		
		modelSpikePattern = new SpikePatternAdapting[nComp];
		double[] irIs=new double[nComp];
		for(int i=0;i<nComp;i++) {
			modelSpikePattern[i]=getModelSpikePattern(model.getIsolatedCompartment(i), MC_IR_I, 100, MC_IR_DUR);
			irIs[i]=MC_IR_I;
		}
		invoker.setIrCurrents(irIs);
		writeTsAndVssForMCConsForIR(modelSpikePattern, VOLTAGE_FILENAME_PFX);
		
		//3.SpikeProp
		double[] spIs=new double[nComp-1];
		for(int i=1;i<nComp;i++) {
			double[] Is = new double[nComp];
			Is[i] = evaluator.getMcEvalholder().getSP_Is_Hold().get(i-1);			
			SpikePatternAdapting[] modelPattern = getModelSpikePattern(model, Is, MC_SP_TIME_MIN, MC_SP_DUR);
			writeTsAndVssForMCConsForSpikePro(modelPattern, VOLTAGE_FILENAME_PFX, i);
			spIs[i-1]=Is[i];
		}
		invoker.setSpCurrents(spIs);
		//4.EPSP
		for(int i=1;i<nComp;i++) {			
			SpikePatternAdapting[] modelPattern = getModelSpikePattern(model, i, MC_EPSP_TC, weights[i-1], 0, MC_EPSP_DUR);
			writeTsAndVssForMCConsForEPSP(modelPattern, VOLTAGE_FILENAME_PFX, i);
		}
		/*
		 * start plotting...
		 */
		
		
		String doShow = "1";
		if(savePlot) doShow="0";
		invoker.invoke(VOLTAGE_FILENAME_PFX, doShow);//invoker.invoke(model);
		
		for(int i=0;i<nScens;i++) {
			File tempFileT = new File(VOLTAGE_FILENAME_PFX+"_t"+i);
			tempFileT.delete();
			
			for(int j=0;j<nComp;j++) {
				File tempFileV = new File(VOLTAGE_FILENAME_PFX+"_v"+i+"_"+j);				
				tempFileV.delete();
			}			
		}
			
		/*
		 * delete all mc files
		 */
		File tempFileT = new File(VOLTAGE_FILENAME_PFX+"_t_exc");
		tempFileT.delete();
		for(int j=0;j<nComp;j++) {
			File tempFileV = new File(VOLTAGE_FILENAME_PFX+"_v_exc_"+j);				
			tempFileV.delete();
		}	
		
		tempFileT = new File(VOLTAGE_FILENAME_PFX+"_t_ir");
		tempFileT.delete();
		
		for(int j=0;j<nComp;j++) {			
			File tempFileV = new File(VOLTAGE_FILENAME_PFX+"_v_ir_"+j);				
			tempFileV.delete();
		}	
		
		for(int i=1;i<nComp;i++) {
			tempFileT = new File(VOLTAGE_FILENAME_PFX+"_t_sp"+i);
			tempFileT.delete();
			
			for(int j=0;j<nComp;j++) {	
				File tempFileV = new File(VOLTAGE_FILENAME_PFX+"_v_sp"+i+"_"+j);				
				tempFileV.delete();
			}	
		}
		
		for(int i=1;i<nComp;i++) {
			tempFileT = new File(VOLTAGE_FILENAME_PFX+"_t_epsp"+i);
			tempFileT.delete();
			
			for(int j=0;j<nComp;j++) {	
				File tempFileV = new File(VOLTAGE_FILENAME_PFX+"_v_epsp"+i+"_"+j);				
				tempFileV.delete();
			}
		}
		
		for(int idx=0;idx<model.getNCompartments();idx++){
			DisplayUtilMcwSyn.displayForC(model, idx);
		}
		for(int idx=0;idx<model.getNCompartments()-1;idx++){
			System.out.println("G_"+(idx+1)+"="+model.getG()[idx]+"/ms");
			System.out.println("P_"+(idx+1)+"="+model.getP()[idx]);
		}
	}
	/*
	 * following plots using all_V files generated under CARLsim results
	 */
	public void onlyPlot(String all_VFileDir, String opDir, int nsubPop, int nthSol, double[] parms, int layoutcode) {
		/*String ecj_opfile ="";
		double[] parms = null;
		if(nsubPop==1) {
			ecj_opfile=opDir+"/job."+job+".Full";
			parms = ECStatOutputReader.readBestSolution(ecj_opfile, 50);
		}else {
			ecj_opfile=opDir+"/job.0.Full";
			parms = ECStatOutputReader.readBestSolutionS(ecj_opfile, 50, nsubPop)[nthSol];
		}*/
		
		String VOLTAGE_FILENAME_PFX = all_VFileDir+"\\forExternalPlottingTemp";
		
		Izhikevich9pModelMC model = getRightInstanceForModel();   
		
		EAGenes genes = new EAGenes(parms, isoComp);        
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
        double[] weights = genes.getW();
        
		int nScens = currents.length;
				
		String opFile=opDir+"/"+nthSol;//"output/"+rootDir+"_"+phenotypeDir+"_"+neuronID+"/"+nthSol;
		
		double modelvr = model.getvR()[0];
		double modelvpk = model.getvPeak()[0];
		
		PyInvoker invoker = new PyInvoker(opFile, nScens, nComp, modelvr, modelvpk, currents);			
		invoker.setDisplayErrorStream(true);	
	
		int nTotScens = nScens+ (2) + (2*(nComp-1));
		
		writeTsAndVssFromCARL_AllVs(all_VFileDir, nthSol, nTotScens, VOLTAGE_FILENAME_PFX);
		
		/*
		 * do MC constraints simulation
		 */
		//1. rheo - excit.
		double[] rheos = new double[nComp];
		for(int i=0;i<rheos.length;i++){
			rheos[i]=-77;//model.getRheoBases();
		}
		
	//	rheos[0]=rheos[0]+1;
	//	rheos[0]=37;
	//	rheos[1]=52;
		invoker.setRheos(rheos);
		
		String doShow = "1";
		if(savePlot) doShow="0";
		
		invoker.setlayoutcode(layoutcode);

		invoker.invoke2(VOLTAGE_FILENAME_PFX, doShow);//invoker.invoke(model);
		
		for(int i=0;i<nTotScens;i++) {
			File tempFileT = new File(VOLTAGE_FILENAME_PFX+"_t"+i);
			tempFileT.delete();
			
			for(int j=0;j<nComp;j++) {
				File tempFileV = new File(VOLTAGE_FILENAME_PFX+"_v"+i+"_"+j);					
				tempFileV.delete();
			}			
		}
			
		
		/*for(int idx=0;idx<model.getNCompartments();idx++){
			DisplayUtilMcwSyn.displayForC(model, idx);
		}
		for(int idx=0;idx<model.getNCompartments()-1;idx++){
			System.out.println("G_"+(idx+1)+"="+model.getG()[idx]+"/ms");
			System.out.println("P_"+(idx+1)+"="+model.getP()[idx]);
		}*/
	}
	
	public SpikePatternAdapting[] getModelSpikePattern(Izhikevich9pModelMC model, double[] I, double timeMin, double duration) {
		model.setInputParameters(I, timeMin, duration);
		IzhikevichSolverMC solver = new IzhikevichSolverMC(model);
		solver.setsS(0.1);
		SpikePatternAdapting[] model_spike_pattern = solver.solveAndGetSpikePatternAdapting();				
		if(model_spike_pattern==null) {			
			System.out.println("Null pattern array -- ModelDBInterface.getModelSpikePatternFor()");
		}
		for(int i=0;i<model_spike_pattern.length;i++){
			if(model_spike_pattern[i]==null) 
				System.out.println("Null compartment pattern -- ModelDBInterface.getModelSpikePatternFor()");
		}
		return model_spike_pattern;
	}
	public SpikePatternAdapting[] getModelSpikePattern(Izhikevich9pModelMC model, int compIdx, float timeConst, double synwt,
								double timeMin, double duration) {
		Izhikevich9pModelMCwSyn modelwSyn = getRightInstanceForModelWSyn(); 
		modelwSyn.setK(model.getK());
		modelwSyn.setA(model.getA());
		modelwSyn.setB(model.getB());
		modelwSyn.setD(model.getD());	
		modelwSyn.setcM(model.getcM());
		modelwSyn.setvR(model.getvR()[0]);
		modelwSyn.setvT(model.getvT());		
		modelwSyn.setvMin(model.getvMin());//		
		modelwSyn.setvPeak(model.getvPeak());
		modelwSyn.setG(model.getG()); 
		modelwSyn.setP(model.getP());        
		float stepSize = (float) IzhikevichSolver.SS;
		double[] appCurrent = new double[model.getNCompartments()];
		modelwSyn.setInputParameters(appCurrent, timeMin, duration);
		
		float[] timeConstant = new float[nComp-1];	
		double[] weight = new double[nComp-1];
		
		for(int i=0;i<timeConstant.length;i++)
			timeConstant[i]=timeConst;
	
		weight[compIdx-1] = synwt;		
		//weight[0]=10;
		//weight[1]=20;
		
		modelwSyn.setTau_ampa(timeConstant);
		modelwSyn.setWeight(weight);
		
		IzhikevichSolverMC solver = new IzhikevichSolverMC(modelwSyn);			
		solver.setsS(stepSize);
		SpikePatternAdapting[] model_spike_pattern = solver.solveAndGetSpikePatternAdapting();		
		
		if(model_spike_pattern==null) {			
			System.out.println("Null pattern array -- ModelDBInterface.getModelSpikePatternFor(...float timeConst, double synwt,...)");
		}
		for(int i=0;i<model_spike_pattern.length;i++){
			if(model_spike_pattern[i]==null) 
				System.out.println("Null compartment pattern -- ModelDBInterface.getModelSpikePatternFor(...float timeConst, double synwt,...)");
		}
		return model_spike_pattern;		
		
	}
	public SpikePatternAdapting getModelSpikePattern(Izhikevich9pModel model, double I, double timeMin, double duration) {
		model.setInputParameters(I, timeMin, duration);
		IzhikevichSolver solver = new IzhikevichSolver(model);
		solver.setsS(0.1);
		SpikePatternAdapting modelSpikePattern = solver.getSpikePatternAdapting();		
				
		if(modelSpikePattern==null) {			
			System.out.println("Null pattern -- ModelDBInterface.getModelSpikePatternFor(izh9pModel, ...). I="+I+"\tduration:"+duration);
		}
		return modelSpikePattern;
	}
	
	//all excitability constraints -- one call!
	public void writeTsAndVssForMCConsForExcitability(SpikePatternAdapting[] modelSpikePattern, String VOLTAGE_FILENAME_PFX) {
		
			double[]  times = modelSpikePattern[0].getModelSpikePatternData().getTime();	
			double[][]  vs = new double[nComp][];
			int trimLength=Integer.MAX_VALUE;
			for(int j=0;j<nComp;j++) {
				vs[j] = modelSpikePattern[j].getModelSpikePatternData().getVoltage();	
				if(vs[j].length<trimLength)
					trimLength=vs[j].length;
			}				
			times=trim(times, trimLength);
			File tempFileT = new File(VOLTAGE_FILENAME_PFX+"_t_exc");
			GeneralFileWriter.write(tempFileT.getAbsolutePath(), times);
			
			for(int j=0;j<nComp;j++) {
				vs[j]=trim(vs[j], trimLength);			
				File tempFileV = new File(VOLTAGE_FILENAME_PFX+"_v_exc_"+j);				
				GeneralFileWriter.write(tempFileV.getAbsolutePath(), vs[j]);
			}	
	}
	
	//all IR constraints -- one call!
	public void writeTsAndVssForMCConsForIR(SpikePatternAdapting[] modelSpikePattern, String VOLTAGE_FILENAME_PFX) {		
		double[]  times = modelSpikePattern[0].getModelSpikePatternData().getTime();
		double[][]  vs = new double[nComp][];
		int trimLength=Integer.MAX_VALUE;
		for(int j=0;j<nComp;j++) {
			vs[j] = modelSpikePattern[j].getModelSpikePatternData().getVoltage();	
			if(vs[j].length<trimLength)
				trimLength=vs[j].length;
		}	
		
		times=trim(times, trimLength);
		File tempFileT = new File(VOLTAGE_FILENAME_PFX+"_t_ir");
		GeneralFileWriter.write(tempFileT.getAbsolutePath(), times);
		
		for(int j=0;j<nComp;j++) {
			vs[j]=trim(vs[j], trimLength);//modelSpikePattern[j].getModelSpikePatternData().getVoltage();				
			File tempFileV = new File(VOLTAGE_FILENAME_PFX+"_v_ir_"+j);				
			GeneralFileWriter.write(tempFileV.getAbsolutePath(), vs[j]);
		}	
}
	
	//spike prop cons - ncomp-1 scenarios and ncomp-1 calls!
	public void writeTsAndVssForMCConsForSpikePro(SpikePatternAdapting[] modelSpikePattern, String VOLTAGE_FILENAME_PFX, int compIdBeingTested) {
		
		double[]  times = modelSpikePattern[0].getModelSpikePatternData().getTime();
		File tempFileT = new File(VOLTAGE_FILENAME_PFX+"_t_sp"+compIdBeingTested);
		GeneralFileWriter.write(tempFileT.getAbsolutePath(), times);
		
		for(int j=0;j<nComp;j++) {
			double[]  vs = modelSpikePattern[j].getModelSpikePatternData().getVoltage();				
			File tempFileV = new File(VOLTAGE_FILENAME_PFX+"_v_sp"+compIdBeingTested+"_"+j);				
			GeneralFileWriter.write(tempFileV.getAbsolutePath(), vs);
		}	
}
	
	//EPSP cons - ncomp-1 scenarios and ncomp-1 calls!
	public void writeTsAndVssForMCConsForEPSP(SpikePatternAdapting[] modelSpikePattern, String VOLTAGE_FILENAME_PFX, int compIdBeingTested) {
		
		double[]  times = modelSpikePattern[0].getModelSpikePatternData().getTime();
		File tempFileT = new File(VOLTAGE_FILENAME_PFX+"_t_epsp"+compIdBeingTested);
		GeneralFileWriter.write(tempFileT.getAbsolutePath(), times);
		
		for(int j=0;j<nComp;j++) {
			double[]  vs = modelSpikePattern[j].getModelSpikePatternData().getVoltage();				
			File tempFileV = new File(VOLTAGE_FILENAME_PFX+"_v_epsp"+compIdBeingTested+"_"+j);				
			GeneralFileWriter.write(tempFileV.getAbsolutePath(), vs);
		}	
}
	
	public void writeTsAndVssFromCARL_AllVs(String allVDir, int subpop_prefix, int nTotScens, String VOLTAGE_FILENAME_PFX) {
		for(int i=0;i<nTotScens;i++) {	
			double[]  vs = null;
			for(int j=0;j<nComp;j++) {
				vs = GeneralFileReader.readAllVCarlOPFile(allVDir+"/"+subpop_prefix+"allV_"+i+"_"+j);					
				File tempFileV0 = new File(allVDir+"/"+subpop_prefix+"allV_"+i+"_"+j);		 		
				tempFileV0.delete();
				
				File tempFileV = new File(VOLTAGE_FILENAME_PFX+"_v"+i+"_"+j);				
				GeneralFileWriter.write(tempFileV.getAbsolutePath(), vs);
			}	
			
			double[] times = new double[vs.length];
			for(int ti=0;ti<times.length;ti++) {
				times[ti]=ti+1;
			}
			File tempFileT = new File(VOLTAGE_FILENAME_PFX+"_t"+i);
			GeneralFileWriter.write(tempFileT.getAbsolutePath(), times);
			
		}
	}
	
	public Izhikevich9pModelMC getRightInstanceForModel(){
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
	private Izhikevich9pModelMCwSyn getRightInstanceForModelWSyn(){
		if(nComp==2){
			return new Izhikevich9pModelMCwSyn(2);
		}
		if(nComp==3){
			if(MultiCompConstraintEvaluator.forwardConnectionIdcs[2]==0)
				return new Izhikevich9pModel3CwSyn(3);
			else
				return new Izhikevich9pModel3CwSyn_L2(3);
		}
		if(nComp==4){
			return new Izhikevich9pModel4CwSyn(4);
		}
		System.out.println("rightModelwSyn needs to be instantiated!!");
		return null;	
	}
	
	private static double[] trim(double[] array, int new_length) {
		if(array.length<new_length) {
			System.out.println("array already shorter than new_length! make new_length to a minmum!");
			System.exit(-1);
		}
		if(array.length==new_length) {
			return array;
		}
		if(array.length>new_length) {
			double[] newArray = new double[new_length];
			for(int i=0;i<newArray.length;i++)
				newArray[i]=array[i];
				return newArray;
		}
		return null;
	}

	public String getUniqueID() {
		return uniqueID;
	}

	public void setUniqueID(String uniqueID) {
		this.uniqueID = uniqueID;
	}
	
	public String mclayoutcode() {
		String layoutcode="0c";
		if(nComp == 1)
			layoutcode = "1c";
		
		if(nComp == 2)
			layoutcode = "2c";
		
		if(nComp ==3 && connIdcs[2]==1) {
			layoutcode = "3c1";
		}
		if(nComp ==3 && connIdcs[2]==0) {
			layoutcode = "3c2";
		}
		
		if(nComp ==4 && connIdcs[2]==1) {
			layoutcode = "4c1";
		}
		if(nComp ==4 && connIdcs[2]==0) {
			layoutcode = "4c2";
		}
		
		return layoutcode;
	}
}
