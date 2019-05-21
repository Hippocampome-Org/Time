package ec.app.izhikevich.plot;

import ec.app.izhikevich.ModelEvaluatorWrapper;
import ec.app.izhikevich.evaluator.ModelEvaluatorMC;
import ec.app.izhikevich.inputprocess.labels.ModelParameterID;
import ec.app.izhikevich.model.Izhikevich9pModelMC;
import ec.app.izhikevich.model.neurontypes.mc.OneNeuronInitializer;
import ec.app.izhikevich.starter.ECJStarterV2;
import ec.app.izhikevich.util.DisplayUtilMcwSyn;

/*
 * diff from the parallel class: this doesn't take in the center model and create models on either direction
 * instead, just on one direction
 */

public class FitnessLandscapePlotter2 {
	static boolean _3dPlot = true;
	static String fileName = "C:/gnu/10_2_3_b1_2003/";
	static String ipStartModel = "10_2_3/B1/2-003/art2";
	
	static final float FIT_LOW_BND = -2f;
	public static void main(String[] args) {
		if(args!=null && args.length>0){
			fileName = args[0];			
		}
		//OneNeuronInitializer.init(4, new int[]{0,0,0,2}, ECJStarterV2.PRIMARY_INPUT, false);		
		//Izhikevich9pModelMC startingModel = DisplayUtilMcwSyn.readModel(ipStartModel, 4);	
		
		OneNeuronInitializer.init(1, null, ECJStarterV2.PRIMARY_INPUT,false);	
		Izhikevich9pModelMC startingModel = DisplayUtilMcwSyn.readModel(ipStartModel, 96);	
		
		//for(ModelParameterID id: ModelParameterID.values())
			//System.out.println(startingModel.getParm(id)[0]+"\t");
		//DataSetOfModels.nParmsToPlot=2;		
		//System.exit(1);
		
		ModelParameterID xID = ModelParameterID.A;			
		DataSetOfModels.x = xID;			
		double[] xParms = startingModel.getParm(xID);
		xParms[0] = 0;	//starting point	
		startingModel.setParm(xID, xParms);
		float stepSizeX = 0.0001f;
		int nStepsX = 100;
		
		ModelParameterID yID = ModelParameterID.D;			
		DataSetOfModels.y= yID;
		double[] yParms = startingModel.getParm(yID);
		yParms[0]=0;		//starting point
		startingModel.setParm(yID, yParms);
		float stepSizeY = 2.5f;
		int nStepsY = 100;		
		
		/*
		DataSetOfModels.x = ModelParameterID.K;
		String xRange = "[-1:3]";
		startingModel.setK(new float[]{0f});
		float stepSize = 0.01f;
		int nSteps = 1300;
		*/
				
		DataSetOfModels dataSet = null;
		if(_3dPlot){
			dataSet =  constructDataSetWithStepModelsAndFitness(startingModel , 
					DataSetOfModels.x, stepSizeX, nStepsX
					,DataSetOfModels.y, stepSizeY, nStepsY);
			//dataSet.mergeFitnessAndModelParmsFor3dPlot(fileName+"_3d.dat");
			dataSet.WriteDatFor3dPlot(fileName);
		}else{
			dataSet =  constructDataSetWithStepModelsAndFitness(startingModel , 
					DataSetOfModels.x, stepSizeX, nStepsX					
						,true);
			dataSet.mergeFitnessAndModelParmsFor2dPlot(fileName+"_2d.dat");
		}	
		
		/*	String fitnessRange="[-0.5:0]";
		plotFitnessLandscape(dataSet ,xRange, 
				//yRange, 
				fitnessRange);		
		 */
	
	}
	static DataSetOfModels constructDataSetWithStepModelsAndFitness(Izhikevich9pModelMC model, 
				ModelParameterID id, float stepSize, int nSteps, boolean displayStatus){
		
		Izhikevich9pModelMC[] models = new Izhikevich9pModelMC[1+nSteps];
		float[] fitnesses = new float[1+nSteps];		
		float percentOfCompletion = 0;
		
		double[] weights = new double[model.getNCompartments()-1];//genes.getW();
        for(int wi=0;wi<weights.length;wi++){
        	weights[wi]=1;
        }
        
		/*
		 * parm x
		 */
		models[0] = model;		
		ModelEvaluatorMC evaluator = new ModelEvaluatorMC(models[0],
				ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS,
				ModelEvaluatorWrapper.INPUT_PHENOTYPE_CONSTRAINT,
				ModelEvaluatorWrapper.INPUT_PAT_REP_WEIGHTS,
				ModelEvaluatorWrapper.INPUT_MC_CONS,
				new double[]{ model.getAppCurrent()[0]}, weights);  	
		float tempFitness = evaluator.getFitness();
		if(tempFitness<FIT_LOW_BND) tempFitness = FIT_LOW_BND;
		fitnesses[0]=  tempFitness;
		//System.out.println(fitnesses[0]);
		double preParmVal = model.getParm(id)[0];		
		/*
		 * nsteps higher than parm x
		 */
		int idxOffset=1;
		
		for(int i=0;i<nSteps;i++){
			models[idxOffset+i] = models[idxOffset+i-1].cloneModelWith(id, 0, preParmVal+stepSize);
			evaluator = new ModelEvaluatorMC(models[idxOffset+i],
					ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS,
					ModelEvaluatorWrapper.INPUT_PHENOTYPE_CONSTRAINT,
					ModelEvaluatorWrapper.INPUT_PAT_REP_WEIGHTS,
					ModelEvaluatorWrapper.INPUT_MC_CONS,
					new double[]{ model.getAppCurrent()[0]}, weights);  
			tempFitness = evaluator.getFitness();
			
			if(tempFitness<FIT_LOW_BND) tempFitness = FIT_LOW_BND;
			fitnesses[idxOffset+i]= tempFitness;
			//System.out.println(fitnesses[idxOffset+i]);
			preParmVal = models[idxOffset+i].getParm(id)[0];
			percentOfCompletion = ((i)*1.0f)/(nSteps*1.0f);
			if(displayStatus){
				displayStatus(percentOfCompletion);	
			}
		}
		DataSetOfModels dataSet = new DataSetOfModels(models, fitnesses, "fitLandscape");
		return dataSet;
	}
	
	static DataSetOfModels constructDataSetWithStepModelsAndFitness(Izhikevich9pModelMC model, 
			ModelParameterID id1, float stepSize1, int nSteps1,
			ModelParameterID id2, float stepSize2, int nSteps2){
	
		float percentOfCompletion = 0;
		DataSetOfModels dataSet2d = new DataSetOfModels("fitLandscape");
		
	//	Izhikevich9pModelMC[][] models = new Izhikevich9pModelMC[1+2*nStepsOnBothDirections1][1+2*nStepsOnBothDirections2];
	//	float[][] fitnesses = new float[1+2*nStepsOnBothDirections1][1+2*nStepsOnBothDirections2];		
				
		DataSetOfModels tempDataSet = constructDataSetWithStepModelsAndFitness(model, id2, stepSize2, nSteps2, false);
		dataSet2d.add1dModels(tempDataSet.modelsOfStochasticTrials);
		dataSet2d.add1dFitnesses(tempDataSet.fitness);		
		Izhikevich9pModelMC preModel = model;
		double preParmVal = preModel.getParm(id1)[0];
		
		
		/*
		 * nsteps higher than parm 1
		 * 	   - nest parm y both directions by calling the existing method
		 */		
		for(int i=0;i<nSteps1;i++){
			Izhikevich9pModelMC newModel1 = preModel.cloneModelWith(id1, 0, preParmVal+stepSize1);					
			tempDataSet = constructDataSetWithStepModelsAndFitness(newModel1, id2, stepSize2, nSteps2, false);			
			dataSet2d.add1dModels(tempDataSet.modelsOfStochasticTrials);
			dataSet2d.add1dFitnesses(tempDataSet.fitness);		
			preModel = newModel1;	
			preParmVal = preModel.getParm(id1)[0];
			
			percentOfCompletion = ((i)*1.0f)/(nSteps1*1.0f);			
			displayStatus(percentOfCompletion);			
		}		
		return dataSet2d;
}
	
	public static void plotFitnessLandscape(DataSetOfModels dataSet,
								String xRange,
								String yRange,
								String zFitnessRange
								){
		String x = DataSetOfModels.x.name();
		String y = DataSetOfModels.y.name();		
		PlotGnu plotter = new PlotGnu("Fitness Landscape with "+x+" and "+y, 
									"'"+x+"'", "'"+y+"'", "'fitness'",
									xRange, yRange, zFitnessRange);		
		
		double[][] PointsOfFitnessInSpace = dataSet.mergeFitnessAndModelParmsFor3dPlot();
		plotter.addDataSet(PointsOfFitnessInSpace, "Fitness");		
		//plotter.plotDataSetPoints(Style.DOTS);
	}
	
	public static void plotFitnessLandscape(DataSetOfModels dataSet,
			String xRange,			
			String yFitnessRange
			){
		String x = DataSetOfModels.x.name();			
		PlotGnu plotter = new PlotGnu("Fitness Landscape with "+x, 
						"'"+x+"'","'fitness'",
						xRange, yFitnessRange);		
		
		double[][] PointsOfFitnessInSpace = dataSet.mergeFitnessAndModelParmsFor2dPlot();
		//GeneralUtils.displayArray(PointsOfFitnessInSpace);
		plotter.addDataSet(PointsOfFitnessInSpace, "Fitness");		
		//plotter.plotDataSetPoints(Style.DOTS);
	}
	
	private static void displayStatus(float percent){
		for(int i=0;i<10;i++)
		if(percent > i*0.1f && !displayed[i]){
			System.out.println(percent +" completed!");
			displayed[i]=true;
		}
	}
	static boolean[] displayed = new boolean[10];
	static {
		for(int i=0;i<10;i++) displayed[i]=false;
	}
}

