package ec.app.izhikevich.resonate;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;

import ec.app.izhikevich.evaluator.qualifier.SpikePatternClassifier;
import ec.app.izhikevich.model.Izhikevich9pModel;
import ec.app.izhikevich.model.IzhikevichSolver;
import ec.app.izhikevich.spike.SpikePatternAdapting;
import ec.app.izhikevich.spike.labels.SpikePatternComponent;
import ec.app.izhikevich.util.GeneralFileWriter;
import ec.app.izhikevich.util.ModelFactory;

public class MeasureFeaturesOfModels {
	
	public static final int TOTAL_MODELS=473;

	Izhikevich9pModel[] models;
	Izhikevich9pModel model;
	
	public MeasureFeaturesOfModels(Izhikevich9pModel[] models) {
		this.models = models;
	}
	public MeasureFeaturesOfModels(Izhikevich9pModel model) {
		this.model = model;
	}
	public double[] measureExcitabilities(float currDur, double iMin, double iMax, double incStep) {
		double[] excits = new double[models.length];
		for(int i=0;i<models.length;i++) {
			excits[i] = models[i].getRheo(currDur, iMin, iMax, incStep);
			if(i%100==0) {
				System.out.println((i+1)+" models excits done!");
			}
		}
		return excits;
	}
	
	public double[] measureFSLs(double inputCurrent, double duration) {
		double[] delays = new double[models.length];
		for(int i=0;i<models.length;i++) {
			models[i].setInputParameters(inputCurrent, 0, duration);	
			IzhikevichSolver solver = new IzhikevichSolver(models[i], models[i].getvR(), 0);
			SpikePatternAdapting spikePattern = solver.getSpikePatternAdapting();
			if(spikePattern==null) {// || spikePattern.getNoOfSpikes()<1){
				System.out.print("model "+i+"null! k="+models[i].getK());
				System.exit(0);
				//delays[i]=-1;continue;
			}
			delays[i] = spikePattern.getFSL();
			if(i%100==0) {
				System.out.println((i+1)+" models fsl done!");
			}
		}
		return delays;
	}
	
	public double[] measureSfas(double[] I, double duration) {
		
		double[] sfas = new double[I.length];
		
		for(int i=0; i<I.length; i++) {
			model.setInputParameters(I[i], 0, duration);
			IzhikevichSolver solver = new IzhikevichSolver(model, model.getvR(), 0);
			SpikePatternAdapting spikePattern = solver.getSpikePatternAdapting();
			
			if(spikePattern.getNoOfSpikes()<3) {
				sfas[i]=-1;
				continue;
			}
			
			SpikePatternClassifier classifier = new SpikePatternClassifier(spikePattern);
			classifier.classifySpikePattern(1, true);
			
			/*int nPieceWiseParmsModel = classifier.getSpikePatternClass().getnPieceWiseParms(); 
			if(nPieceWiseParmsModel<1) {
				if(classifier.getSpikePatternClass().contains(SpikePatternComponent.RASP)){
					
					sfas[i] = classifier.getSpikePatternClass().classificationParameters.get(ClassificationParameterID.M_RASP);
					continue;
				}
				//System.out.println(classifier.getSpikePatternClass().toString()+" "+spikePattern.getNoOfSpikes());
				
				sfas[i]=-1;
				continue;
			}*/
			//SolverResultsStat modelStats = classifier.getSolverResultsStats()[nPieceWiseParmsModel-1];
			sfas[i] = spikePattern.calculateSfa(spikePattern.getNoOfSpikes()-1);//modelStats.getM1();
			
		}
		return sfas;
	}
	
	public static Izhikevich9pModel[] readModelsFromCSV(String filename) {
		Izhikevich9pModel[] models = new Izhikevich9pModel[TOTAL_MODELS];
		int modelIdx=0;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String str = br.readLine();
			str = br.readLine();
			while(str!=null) {
				StringTokenizer st = new StringTokenizer(str,",");
				double[] parms = new double[9];
				
				int idx = Integer.parseInt(st.nextToken());
				for(int i=0;i<9;i++)	
					parms[i] = Double.parseDouble(st.nextToken());
				models[modelIdx++]=createModel(parms);
				
				str = br.readLine();
			}
			
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return models;
	}
	private static Izhikevich9pModel createModel(double[] parms) {
		Izhikevich9pModel model = new Izhikevich9pModel();
		model.setK(parms[0]);
		model.setA(parms[1]);
		model.setB(parms[2]);
		model.setD(parms[3]);	
		model.setcM(parms[4]);
		model.setvR(parms[5]);
		model.setvT(parms[6]);		
		model.setvMin(parms[7]);	
        model.setvPeak(parms[8]);
        
		return model;
	}
	// Use negative current
	public double measureTimeConstant(double inputCurrent, double duration) {
		return model.getTimeConstant(inputCurrent, duration, duration-200, duration-100);
	}
	
	//returns ir in ohms
	public double measureIR(double inputCurrent, double duration) {
		double ir;
		double vdef = model.getSSVDef(inputCurrent, duration, duration-200, duration-100);
		System.out.println("vdef: "+vdef);
		
		ir = (vdef*(Math.pow(10, 12)))/(-inputCurrent*1000);
		return ir;
	}
	
	private static void measureSFAEntry() {
		String uniqueID = "3-000";
		Izhikevich9pModel model_ = ModelFactory.getUserDefinedModel(uniqueID);
		MeasureFeaturesOfModels mfm = new MeasureFeaturesOfModels(model_);
		
		double duration = 500;
		double[] I = new double[100];
		double Imin = 0;
		double Imax = 1000;
		int n = 100;
		
		double interval = (Imax-Imin)/n;
		for(int i=0;i<n;i++) {
			I[i]=Imin + (interval * i);
		}
	
		double[] sfas = mfm.measureSfas(I, duration);
		String fileName = uniqueID+"_sfas.csv";
		GeneralFileWriter.write(fileName , I, sfas);
	}
		
	private static void firstEntry() {
		/*
		String filename="theory\\multibehavior_models\\models.csv";
		System.out.println("Reading models from csv...");
		Map<String, Izhikevich9pModel> models =ModelFactory.readModelsFromCSV(filename);
		Izhikevich9pModel model =  models.get("4-036-1");
		
		model.setcM(50);
		
		MeasureFeaturesOfModels feats = new MeasureFeaturesOfModels(model);
		
		double I = -100; //pA
		double duration = 2000; //ms
		
		double R = feats.measureIR(I, duration); //ohm
		double tow = feats.measureTimeConstant(I, duration); //ms
		double C = tow/(R*1000);
		
		//System.out.println("R: "+R);
		//System.out.println("tow: "+tow);
		//System.out.println("C: "+C);
		//double[] excitabilities = rs.measureExcitabilities(5000, 5, 1000, 1);
		//double[] fsls = rs.measureFSLs(700, 6000);
		//GeneralUtils.displayArrayVertical(fsls);
		
		*/
	}
	
	private void writeKvsI() {
		int _class=0;
		
		double kMin = 0;
		double kMax = 5;
		int nK = 100;
		double intervalK = (kMax - kMin)/(1.0d * nK);
		
		double iMin = 100;
		double iMax = 700;
		int nI = 600;
		double intervalI = (iMax - iMin)/(1.0d * nI);
		
		try {
			FileWriter fw = new FileWriter("kvsIv5.csv");
			for(int k=0; k<nK; k++) {
				double _k= kMin + (intervalK * (double)k);
				model.setK(_k);
				for(int i=0; i<nI; i++) {
					double _i=iMin + (intervalI * (double)i);
					_class = 0;
					model.setInputParameters(_i, 0, 500);
					IzhikevichSolver solver = new IzhikevichSolver(model, model.getvR(), 0);
					SpikePatternAdapting spikePattern = solver.getSpikePatternAdapting();
					
					if(spikePattern==null || spikePattern.getISIs() ==null || spikePattern.getISIs().length<3) {
						_class=0;
					}else {
						SpikePatternClassifier classifier = new SpikePatternClassifier(spikePattern);
						classifier.classifySpikePattern(spikePattern.getSwa(), true);
						if(classifier.getSpikePatternClass().containsSP() ) 
						{
							_class=1;
						}
						if(classifier.getSpikePatternClass().containsSTUT() || classifier.getSpikePatternClass().containsSWB()
								|| classifier.getSpikePatternClass().contains(SpikePatternComponent.SLN)) {
							_class=2;
						}
					}
					
					
					fw.write(_k+","+_i+","+_class+"\n");
					fw.flush();
				}
				System.out.println(_k +" done!");
				
			}
			fw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	public static void main(String[] args) {
		String filename="theory/multibehavior_models/models.csv";
		Map<String, Izhikevich9pModel> models =ModelFactory.readModelsFromCSV(filename);
		Izhikevich9pModel model =  models.get("4-080-1");
		
		MeasureFeaturesOfModels mfm = new MeasureFeaturesOfModels(model);
		mfm.writeKvsI();
		
	}

}
