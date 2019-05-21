package ec.app.izhikevich.plot;

import ec.app.izhikevich.ModelEvaluatorWrapper;
import ec.app.izhikevich.evaluator.ModelEvaluatorMC;
import ec.app.izhikevich.inputprocess.labels.ModelParameterID;
import ec.app.izhikevich.model.Izhikevich9pModelMC;
import ec.app.izhikevich.model.neurontypes.mc.OneNeuronInitializer;
import ec.app.izhikevich.spike.labels.SpikePatternClass;
import ec.app.izhikevich.starter.ECJStarterV2;
import ec.app.izhikevich.util.DisplayUtilMcwSyn;

/*
 * diff from the parallel class: this doesn't take in the center model and create models on either direction
 * instead, just on one direction
 */

public class ClassLandscapePlotter {
	static boolean _3dPlot = true;
	static String fileName = "C:/gnu/10_2_3_A_feats/";
	static String ipStartModel = "10_2_3/B1/2-003/art2";  //sfa, trial 96
	//static String ipStartModel = "10_2_3/B4/3-000/4b";  //fsl, trial 80
//	static String ipStartModel = "10_2_3/B3/2-043/13b2"; //fs, trial 4
	static int trial = 96;
	static int min_NSPIKES_for_measure = 4;
	
	static final float FIT_LOW_BND = -2f;
	public static void main(String[] args) {
		if(args!=null && args.length>0){
			fileName = args[0];			
		}
		
		OneNeuronInitializer.init(1, null, ECJStarterV2.PRIMARY_INPUT,false);	
		Izhikevich9pModelMC startingModel = DisplayUtilMcwSyn.readModel(ipStartModel, trial);	
		
		
		 //fsl
		ModelParameterID xID = ModelParameterID.K;			
		DataSetOfModels.x = xID;			
		double[] xParms = startingModel.getParm(xID);
		xParms[0] = 0;	//starting point	
		startingModel.setParm(xID, xParms);
		float stepSizeX = 0.05f;
		int nStepsX = 100;
		
		ModelParameterID yID = ModelParameterID.I;			
		DataSetOfModels.y= yID;
		double[] yParms = startingModel.getParm(yID);
		yParms[0]=0;		//starting point
		startingModel.setParm(yID, yParms);
		float stepSizeY = 10;
		int nStepsY = 100;		
		
		
		
	   /* ModelParameterID xID = ModelParameterID.B;			
		DataSetOfModels.x = xID;			
		double[] xParms = startingModel.getParm(xID);
		xParms[0] = -25;	//starting point	
		startingModel.setParm(xID, xParms);
		float stepSizeX = 0.5f;
		int nStepsX = 100;*/
		 //-- fs
	/*	ModelParameterID xID = ModelParameterID.CM;			
		DataSetOfModels.x = xID;			
		double[] xParms = startingModel.getParm(xID);
		xParms[0] = 10;	//starting point	
		startingModel.setParm(xID, xParms);
		float stepSizeX = 5f;
		int nStepsX = 100;
		
		ModelParameterID yID = ModelParameterID.A;			
		DataSetOfModels.y= yID;
		double[] yParms = startingModel.getParm(yID);
		yParms[0]=0;		//starting point
		startingModel.setParm(yID, yParms);
		float stepSizeY = 0.002f;
		int nStepsY = 100;
		*/	
		DataSetOfModels dataSet = null;
		if(_3dPlot){
			dataSet =  constructDataSetWithStepModelsAndFeature(startingModel , 
					DataSetOfModels.x, stepSizeX, nStepsX
					,DataSetOfModels.y, stepSizeY, nStepsY);
			//dataSet.mergeFitnessAndModelParmsFor3dPlot(fileName+"_3d.dat");
			dataSet.WriteDatFor3dPlot(fileName);
		}else{
			dataSet =  constructDataSetWithStepModelsAndFeature(startingModel , 
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
	static DataSetOfModels constructDataSetWithStepModelsAndFeature(Izhikevich9pModelMC model, 
				ModelParameterID id, float stepSize, int nSteps, boolean displayStatus){
		
		Izhikevich9pModelMC[] models = new Izhikevich9pModelMC[1+nSteps];
		float[] features = new float[1+nSteps];		
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
		 
		float tempfeature=0;
		SpikePatternClass _class = null;
		if(evaluator.measureFeatures(min_NSPIKES_for_measure))
		{
			_class = evaluator.getSpEvalHolder().patternClassifier.getSpikePatternClass();
			if(_class.containsSTUT() || _class.containsSWB()){
				tempfeature = 2;
			}
			if(_class.containsSP()){
				tempfeature = 1;
			}
		}
		 
		
		features[0]=  tempfeature;
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
					new double[]{ models[idxOffset+i].getAppCurrent()[0]}, weights); 
			
			tempfeature=0;
			_class = null;
			if(evaluator.measureFeatures(min_NSPIKES_for_measure))
			{
				_class = evaluator.getSpEvalHolder().patternClassifier.getSpikePatternClass();
				if(_class.containsSTUT() || _class.containsSWB()){
					tempfeature = 2;
				}
				if(_class.containsSP()){
					tempfeature = 1;
				}
			}
			
			features[idxOffset+i]= tempfeature;
			//System.out.println(fitnesses[idxOffset+i]);
			preParmVal = models[idxOffset+i].getParm(id)[0];
			percentOfCompletion = ((i)*1.0f)/(nSteps*1.0f);
			if(displayStatus){
				displayStatus(percentOfCompletion);	
			}
		}
		DataSetOfModels dataSet = new DataSetOfModels(models, features, "featLandscape");
		return dataSet;
	}
	
	static DataSetOfModels constructDataSetWithStepModelsAndFeature(Izhikevich9pModelMC model, 
			ModelParameterID id1, float stepSize1, int nSteps1,
			ModelParameterID id2, float stepSize2, int nSteps2){
	
		float percentOfCompletion = 0;
		DataSetOfModels dataSet2d = new DataSetOfModels("featLandscape");
		
	//	Izhikevich9pModelMC[][] models = new Izhikevich9pModelMC[1+2*nStepsOnBothDirections1][1+2*nStepsOnBothDirections2];
	//	float[][] fitnesses = new float[1+2*nStepsOnBothDirections1][1+2*nStepsOnBothDirections2];		
				
		DataSetOfModels tempDataSet = constructDataSetWithStepModelsAndFeature(model, id2, stepSize2, nSteps2, false);
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
			tempDataSet = constructDataSetWithStepModelsAndFeature(newModel1, id2, stepSize2, nSteps2, false);			
			dataSet2d.add1dModels(tempDataSet.modelsOfStochasticTrials);
			dataSet2d.add1dFitnesses(tempDataSet.fitness);		
			preModel = newModel1;	
			preParmVal = preModel.getParm(id1)[0];
			
			percentOfCompletion = ((i)*1.0f)/(nSteps1*1.0f);			
			displayStatus(percentOfCompletion);			
		}		
		return dataSet2d;
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

