package ec.app.izhikevich.plot;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ec.app.izhikevich.ModelEvaluatorWrapper;
import ec.app.izhikevich.evaluator.ModelEvaluatorMC;
import ec.app.izhikevich.inputprocess.labels.ModelParameterID;
import ec.app.izhikevich.model.Izhikevich9pModelMC;
import ec.app.izhikevich.model.neurontypes.mc.OneNeuronInitializer;
import ec.app.izhikevich.starter.ECJStarterV2;
import ec.app.izhikevich.util.DisplayUtilMcwSyn;

public class ParmSpacePlotter {
	
	//public static int[] fitnessLine;	
	public static int[] nJobs;
	public static int[] nGens;
	public static String[] opFolder;
	public static String[] description;
	//private static String yRange;
	
	static {
		String phen_category = "10_2_3";
		String[] phen_num = new String[] {"Prb", "P2"};//, "P2"};//, "P2", "P2", "P5"};
		String[] Neur = new String[] 	 {"N0", "N3"};//, "N3"};//, "N3", "N4", "N1"};	
		String[] exp = new String[] 	 {"1",  "0"};//, "clss/mid"};//,  "0",  "2",  "0"};
		
		nJobs =new int[] 				 {40,   40, 40};//,    40,   40,   40};
		nGens = new int[] 				 {2000,2000, 2000};//,   2000, 2000, 3000};
		
		String[] description_partial = new String[] {"rebound", "delayed_spiking", "clss/mid"};//, "D_NASP", "D_NASP", "NASP"};
		
		description = new String[description_partial.length];
		for(int i=0;i<description.length;i++){
			description[i]=description_partial[i];//+" -"+phen_num[i]+"."+Neur[i];
		}
		
	//	boolean multiObj = false;
		
		/*fitnessLine = new int[nGens.length];
		for(int i=0;i<fitnessLine.length;i++){
			if(!multiObj)		fitnessLine[i] = nGens[i]*7 + 5 ;//+1;
			//+1; // if single objective terminated in the middle
			else		fitnessLine[i] = nGens[i]*11 - 1;
		}*/
		
		opFolder = new String[exp.length];
		for(int i=0;i<opFolder.length;i++){
			opFolder[i] =//"local";
					phen_category+"/"+phen_num[i]+"/"+Neur[i]+"/"+exp[i];	
		}			 
	}
	public static void main(String[] args) {
		OneNeuronInitializer.init(1, null, ECJStarterV2.PRIMARY_INPUT,false);		
	
		/*
		 * plot model parms in space: go through all exps results and do a single plot
		 */
		DataSetOfModels.nParmsToPlot=3;
	
		DataSetOfModels.x = ModelParameterID.D;
		DataSetOfModels.y= ModelParameterID.A;	
		DataSetOfModels.z= ModelParameterID.B;
		
		//DataSetOfModels[] dataSets = new DataSetOfModels[opFolder.length];
		for(int i=0;i<opFolder.length;i++){
			DataSetOfModels dataSet = 	constructDataSetOfModels(opFolder[i],description[i],nJobs[i], nGens[i]);
			dataSet.WriteModelParmsFor3dPlot("C:\\matPlots\\rbnd\\"+dataSet.description);
			//dataSets[i] = dataSet;
		}
		
		//plotModelParmsWithFitnessInSpace(dataSets);		
	}
	
	public static void plotModelParmsInSpace(DataSetOfModels[] dataSets,
								String xRange,
								String yRange
								){
		String x = DataSetOfModels.x.name();
		String y = DataSetOfModels.y.name();		
		PlotGnu plotter = new PlotGnu("Izh. Parm. Space with "+x+" and "+y, "'"+x+"'", "'"+y+"'", xRange, yRange);
		
		for(int i=0;i<dataSets.length;i++){
			double[][] datasetPoints = constructDataSetPointsFromDataSet(dataSets[i]);
			plotter.addDataSet(datasetPoints, dataSets[i].description);
		}
		
		plotter.plotDataSetPoints();
	}
	
	
	
	public static void plotModelParmsInSpace(DataSetOfModels[] dataSets,
			String xRange,
			String yRange,
			String zRange
			){
		String x = DataSetOfModels.x.name();
		String y = DataSetOfModels.y.name();	
		String z = DataSetOfModels.z.name();
		
		PlotGnu plotter = new PlotGnu("Izh. Parm. Space", 
				"'"+x+"'", "'"+y+"'", "'"+z+"'",
				xRange, yRange, zRange);
		
		for(int i=0;i<dataSets.length;i++){
		double[][] datasetPoints = constructDataSetPointsFromDataSet(dataSets[i]);
		plotter.addDataSet(datasetPoints, dataSets[i].description);
		}
		
		plotter.plotDataSetPoints();
	}
	
	public static void plotModelParmsWithFitnessInSpace(DataSetOfModels[] dataSets){
		String x = DataSetOfModels.x.name();
		String y = "fitness";	
		
		PlotGnu plotter = new PlotGnu("ttt", 
				"'"+x+"'", "'"+y+"'");
		
		for(int i=0;i<dataSets.length;i++){
		double[][] datasetPoints = constructDataSetPointsWithFitness(dataSets[i]);
		plotter.addDataSet(datasetPoints, dataSets[i].description);
		}
		
		plotter.plotDataSetPoints();
	}
	
	private static double[][] constructDataSetPointsFromDataSet(DataSetOfModels dataSet){
		double[][] modelParms = new double[dataSet.modelsOfStochasticTrials.length][DataSetOfModels.nParmsToPlot];
		for(int i=0;i<modelParms.length;i++){			
			modelParms[i][0] = dataSet.modelsOfStochasticTrials[i].getParm(DataSetOfModels.x)[0];
			modelParms[i][1] = dataSet.modelsOfStochasticTrials[i].getParm(DataSetOfModels.y)[0];
			if(DataSetOfModels.nParmsToPlot>2)
				modelParms[i][2] = dataSet.modelsOfStochasticTrials[i].getParm(DataSetOfModels.z)[0];
		}
		return modelParms;
	}
	
	private static double[][] constructDataSetPointsWithFitness(DataSetOfModels dataSet){
		double[][] modelParms = new double[dataSet.modelsOfStochasticTrials.length][DataSetOfModels.nParmsToPlot];
		for(int i=0;i<modelParms.length;i++){			
			modelParms[i][0] = dataSet.modelsOfStochasticTrials[i].getParm(DataSetOfModels.x)[0];
			modelParms[i][1] = dataSet.fitness[i];			
		}
		return modelParms;
	}
	private static DataSetOfModels constructDataSetOfModels(String opFolder, String description, 
			int nJobs, int nGens){
		Izhikevich9pModelMC[] modelsOfStochasticTrials= new Izhikevich9pModelMC[1];
		List<Izhikevich9pModelMC> modelsList = new ArrayList<>();
		List<Float> fitnessesList = new ArrayList<>();
		float[] fitnesses;
		
		for(int trial=0;trial<nJobs;trial++){					
			Izhikevich9pModelMC model = DisplayUtilMcwSyn.readModel(opFolder, trial);
			if(model!=null){
				
			
				modelsList.add(model);
			
			//
			ModelEvaluatorMC evaluator = new ModelEvaluatorMC(model,
					ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS,
					ModelEvaluatorWrapper.INPUT_PHENOTYPE_CONSTRAINT,
					ModelEvaluatorWrapper.INPUT_PAT_REP_WEIGHTS,
					ModelEvaluatorWrapper.INPUT_MC_CONS,
					new double[]{model.getAppCurrent()[0]}, 
					null);  	
			fitnessesList.add(evaluator.getFitness());	
			}
	      }
		modelsOfStochasticTrials = modelsList.toArray(modelsOfStochasticTrials);
		fitnesses = new float[fitnessesList.size()];
		for(int i=0;i<fitnessesList.size();i++)
			fitnesses[i]= fitnessesList.get(i);
		
			DataSetOfModels dataSetOfModels= new DataSetOfModels(modelsOfStochasticTrials, 
					fitnesses,
					description);
			return dataSetOfModels;
	}
}

class DataSetOfModels {
	public static int nParmsToPlot;
	public static ModelParameterID x;
	public static ModelParameterID y;
	public static ModelParameterID z;	
	
	/*
	 * 1d matrix of models (general purpose) and along with fitness with 1d landscape plot
	 */
	Izhikevich9pModelMC[] modelsOfStochasticTrials;
	public float[] fitness;
	String description;
	
	/*
	 * 2d matrix of models and fitness evaluations for 3d landscape plot
	 */
	List<Izhikevich9pModelMC[]> models2d;
	List<float[]> fitness2d;
	
	DataSetOfModels(Izhikevich9pModelMC[] modelsOfStochasticTrials, 					
			String description
			){
		this.modelsOfStochasticTrials = modelsOfStochasticTrials;		
		this.description = description;
	}
	
	DataSetOfModels(Izhikevich9pModelMC[] modelsOfStochasticTrials,
			float[] fitness,
			String description
			){
		this.modelsOfStochasticTrials = modelsOfStochasticTrials;	
		this.fitness= fitness;
		this.description = description;
	}
	
	
	DataSetOfModels(String description){
		this.models2d = new ArrayList<>();	
		this.fitness2d= new ArrayList<>();
		this.description = description;
	}
	
	void add1dModels(Izhikevich9pModelMC[] models){
		this.models2d.add(models);
	}
	
	void add1dFitnesses(float[] fitnesses){
		this.fitness2d.add(fitnesses);
	}
	
	double[][] mergeFitnessAndModelParmsFor3dPlot(){
		double[][] points = new double[models2d.size()*models2d.get(0).length][3];
		int pIdx=0;
		for(int i=0;i<models2d.size();i++){
			for(int j=0;j<models2d.get(i).length;j++){
				points[pIdx][0]=models2d.get(i)[j].getParm(x)[0];
				points[pIdx][1]=models2d.get(i)[j].getParm(y)[0];
				points[pIdx++][2]=fitness2d.get(i)[j];
			}
		}			
		return points;
	}
	
	void mergeFitnessAndModelParmsFor3dPlot(String dataFileName){
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(dataFileName));				
			for(int i=0;i<models2d.size();i++){				
				for(int j=0;j<models2d.get(i).length;j++){					
					bw.write(models2d.get(i)[j].getParm(x)[0]+
					" "+models2d.get(i)[j].getParm(y)[0]+
					" "+fitness2d.get(i)[j]+"\n"
							);
				}
				bw.write("\n");
			}
			bw.flush();bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	/*
	 * for matlab surf
	 */
	void WriteDatFor3dPlot(String fitFileName){
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(fitFileName+"/z.dat"));	
			BufferedWriter bwx = new BufferedWriter(new FileWriter(fitFileName+"/x.dat"));	
			BufferedWriter bwy = new BufferedWriter(new FileWriter(fitFileName+"/y.dat"));	
			for(int i=0;i<models2d.size();i++){	
				bwx.write(models2d.get(i)[0].getParm(x)[0]+" ");
				for(int j=0;j<models2d.get(i).length;j++){		
					if(i==0){
						bwy.write(models2d.get(i)[j].getParm(y)[0]+" ");
					}
					bw.write(fitness2d.get(i)[j]+" ");
				}
				bw.write("\n");
				bw.flush();
				bwx.flush();
				bwy.flush();
			}
			bw.close();
			bwx.close();
			bwy.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	void WriteModelParmsFor3dPlot(String fileName){
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileName+".dat"));	
			
			for(int i=0;i<modelsOfStochasticTrials.length;i++){	
				bw.write(modelsOfStochasticTrials[i].getParm(x)[0]+" "+
						+modelsOfStochasticTrials[i].getParm(y)[0]+" "
						+modelsOfStochasticTrials[i].getParm(z)[0]+" "
					//	+this.fitness[i]
								);			
				bw.write("\n");
			}
			bw.flush();
			bw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	void mergeFitnessAndModelParmsFor2dPlot(String dataFileName){
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(dataFileName));						
				for(int i=0;i<modelsOfStochasticTrials.length;i++){					
					bw.write(modelsOfStochasticTrials[i].getParm(x)[0]+
					" "+fitness[i]+"\n"
							);
				}							
			bw.flush();bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	double[][] mergeFitnessAndModelParmsFor2dPlot(){
		double[][] points = new double[modelsOfStochasticTrials.length][2];
		for(int i=0;i<points.length;i++){
			points[i][0]= modelsOfStochasticTrials[i].getParm(x)[0];
			points[i][1]=fitness[i];	
		}
		return points;
	}
	
}