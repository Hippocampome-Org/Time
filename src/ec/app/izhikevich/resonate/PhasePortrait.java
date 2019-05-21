package ec.app.izhikevich.resonate;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import ec.app.izhikevich.evaluator.qualifier.SpikePatternClassifier;
import ec.app.izhikevich.model.Izhikevich9pModel;
import ec.app.izhikevich.model.IzhikevichSolver;
import ec.app.izhikevich.model.neurontypes.mc.EAGenes;
import ec.app.izhikevich.model.neurontypes.mc.OneNeuronInitializer;
import ec.app.izhikevich.spike.ModelSpikePatternData;
import ec.app.izhikevich.spike.SpikePatternAdapting;
import ec.app.izhikevich.starter.ECJStarterV2;
import ec.app.izhikevich.util.DisplayUtilMcwSyn;
import ec.app.izhikevich.util.InputCurrentGenerator;
import ec.app.izhikevich.util.ModelFactory;
import ec.app.izhikevich.util.StatUtil;

public class PhasePortrait {
	
	private static int[] forConnIdcs;
	
	public static int nJobs;
	public static int nGens;
	public static String opFolder;
	public static boolean iso_comp;
	public static int ECJ_trial;
	
	static int nComp;
	
	static {		
		String phen_category = ECJStarterV2.Phen_Category;
		String phen_num = ECJStarterV2.Phen_Num;
		String Neur = ECJStarterV2.Neur;//"N2";		
		
		iso_comp = ECJStarterV2.iso_comp;
		String exp = "0"; 		
		ECJ_trial = 88;
		
		nComp = ECJStarterV2.N_COMP;
		forConnIdcs = ECJStarterV2.CONN_IDCS;		
		opFolder =phen_category+"/"+phen_num+"/"+Neur+"/"+exp;
	}
	
	public static void main(String[] args) {
		OneNeuronInitializer.init(ECJStarterV2.N_COMP, ECJStarterV2.CONN_IDCS, ECJStarterV2.PRIMARY_INPUT, ECJStarterV2.iso_comp);

		String csvFile = "theory/multibehavior_models/models.csv";
		Map<String, Izhikevich9pModel> models = ModelFactory.readModelsFromCSV(csvFile);
		
		String unique_id = "2-009-1";
		double inputCurrent = -200;
		double duration = 500;
		double discard_duration = 0;
		
		Izhikevich9pModel model =ModelFactory.getUserDefinedModel();////models.get(unique_id);;// ModelFactory.readModel(opFolder, ECJ_trial);//
		//model.setK(4.95);
		String outputPathAndFile = "output/"+opFolder+"/"+unique_id+"_tr"+ECJ_trial+"_I"+inputCurrent+"_Idur"+duration;
		simulateAndRecordStates(model, inputCurrent, duration, discard_duration, 
				model.getvR(), 0, outputPathAndFile);
	}	

	private static void fftOnZap(){
		PhasePortrait pp = new PhasePortrait();
		
		Izhikevich9pModel model = ModelFactory.getMB1a();//pp.createModelWithUserParms();
		double duration = Math.pow(2, 12);
		System.out.println("duration of current: "+duration);
		//model.setInputParameters(538, 0, 1000); //--for single behavior D.NASP : correct transient/ss behavior
		
		
		model.setInputParameters(0, 0, duration);
		
		//sin
		InputCurrentGenerator iGenr = new InputCurrentGenerator(450d, 30);
		
		//ramp
		//InputCurrentGenerator iGenr = new InputCurrentGenerator(slope);
		
		//step
		//InputCurrentGenerator iGenr = new InputCurrentGenerator();
		//iGenr.setStepCurrentMag(model.getCurrent());
		
		//zap
		//InputCurrentGenerator iGenr = new InputCurrentGenerator(5,1,1,1);
		
		model.setiGenr(iGenr );
		
		String opFile = "theory/data.dat";
		String fftSfx = "Wfft";
		String pythonScript = "plotter"+fftSfx+".py";
		ModelSpikePatternData mspData = pp.getSpikePattern(model, 0, 0).getModelSpikePatternData();
			
		double[] time = mspData.getTime();
		double[] v = mspData.getVoltage();
		double[] u = mspData.getRecoveryU();
		
	
		//GeneralUtils.displayArrayVertical(time);
		
		
		double[] newV = pp.powerOfTwoLengthArray(v);
	    
		
		FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
		Complex[] spectrum = transformer.transform(newV, TransformType.FORWARD);
		
		try {
			FileWriter fw = new FileWriter(opFile);
			for(int i=0;i<time.length;i++){
				fw.write(time[i]+"\t"+v[i]+"\t"+u[i]+"\t"+iGenr.getCurrent(time[i])+"\n");
			}
			fw.flush();fw.close();
			
			fw = new FileWriter(opFile+"_fft.dat");
			for(int i=1;i<spectrum.length/2;i++){			
				Complex cx = spectrum[i];
				double scaled_real = cx.getReal();
				double scaled_imaginary = cx.getImaginary();
				
				double mag = Math.sqrt(scaled_real*scaled_real + scaled_imaginary*scaled_imaginary);
				double freq = Index2Freq(i, .1d, spectrum.length);
				fw.write(freq+"\t"+mag+"\n");
			}
			fw.flush();fw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
		pp.plot(opFile, pythonScript);
	}
	private static void simulateAndRecordStates(Izhikevich9pModel model, 
												double inputCurrent, double duration, double discard_duration,
												double initV, double initU, String opfileName){
		PhasePortrait pp = new PhasePortrait();
			
		model.setInputParameters(inputCurrent, 100, duration);			
					
		SpikePatternAdapting spikePattern = pp.getSpikePattern(model, initV, initU);
		if(spikePattern==null ){
			System.out.print("xnull!");
			return;
		}
		//GeneralUtils.displayArray(spikePattern.getSpikeTimes());
		
		ModelSpikePatternData mspData =spikePattern.getModelSpikePatternData();
			
		SpikePatternClassifier classifier = new SpikePatternClassifier(spikePattern);
		double swa = spikePattern.getSwa();
		classifier.classifySpikePattern(swa, true);
		System.out.println(swa+"  "+classifier.getSpikePatternClass().toString());
		double[] time = mspData.getTime();
		double[] v = mspData.getVoltage();
		double[] u = mspData.getRecoveryU();	
		double[] vderivs = mspData.getvDerivs();
		
		int discardPoints = (int) (discard_duration / IzhikevichSolver.SS);
		
		try {
			FileWriter fw = new FileWriter(opfileName);
			for(int i=discardPoints;i<time.length;i++){
				fw.write(time[i]+"\t"+v[i]+"\n");//"\t"+vderivs[i]+"\t"+u[i]+"\n");///iGenr.getCurrent(time[i])+"\n");
			}
			fw.flush();fw.close();	
			
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public static double Index2Freq(int i, double delta_t, int nFFT) {
		double Fs = 1d/(delta_t*.001);
		double delta_f = Fs/(double)nFFT;
				
		if(i<= nFFT/2){
			return (double)i * delta_f;
		}
		  //return (double) (2*Math.PI * i / nFFT);
		return -1;
	}

	
	public void plot(String dataFileName, String pythonScript){
		PyPlotter plotter = new PyPlotter("theory/"+dataFileName);
		plotter.invoke(pythonScript);
	}
	
	private double[] powerOfTwoLengthArray(double[] array){
		double[] newArray = new double[(int) Math.pow(2,StatUtil.log(array.length, 2))];
		for(int i=0;i<newArray.length;i++){
			newArray[i] = array[i];
		}
		return newArray;
	}
	
	
	public Izhikevich9pModel createModelFromECJFile(){		
		OneNeuronInitializer.init(nComp, forConnIdcs, ECJStarterV2.PRIMARY_INPUT, iso_comp);
		
		double[] parms = DisplayUtilMcwSyn.readBestParms(opFolder, ECJ_trial);	
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
        
        return model;
	}
	
	public Izhikevich9pModel createModelWithUserParms(){
		
		double k0;	double a0;	double b0;	double d0;	double C0;	double vR0;	double vT0;	double vPeak0;	double c0;
		
		/*
		 * sb - NASP
		 */
	/*	k0=1.0251718767357474;
		a0=0.04210012839330164;
		b0=-5.072651470247909;
		d0=68.0;
		C0=67.0;
		vR0=-60.02414159959671;
		vT0=-47.97306370693811;
		vPeak0=11.091821733636785;
		c0=-59.38112538014466;
		*/
		/*
		 * mb1a
		 */
		k0=3.0587634381147932;
		a0=0.026098736010710277;
		b0=7.363427418000718;
		d0=4.0;
		C0=208.0;
		vR0=-64.94459578998348;
		vT0=-44.575737574886304;
		vPeak0=-0.4169370067716045;
		c0=-51.08807348679976;
		

		//mb1b		
	/*	k0=3.935030495305116; 
		C0=107.0;
		vR0=-64.67262808336909;
		vT0=-58.74397153986162; 
		a0=0.0019524485375888802;
		b0=16.57957045858656;
		c0=-59.703262575872536;
		d0=19.0;
		vPeak0=-9.928793957976993;
	*/					
		/*
		 * mb2
		 */
		
	/*	
	 	k0=1.909065;
		C0=212;
		vR0=-60.17006; 
		vT0=-48.22413484554066;
		a0=1.8225495740542997E-4; 
		b0=13.797293714514765;
		c0=-51.16572687111852;
		d0=3.0;
		vPeak0=8.731116247879228;
			*/
		/*
		 * rebound spiking
		 */
	/*	k0=0.5124128;
		a0=0.069234304;
		b0=20.460056;
		d0=89.0;
		C0=66.0;
		vR0=-60.793514;
		vT0=-57.448715;
		vPeak0=7.1172295;
		c0=-61.793102;
				*/
		
		// fast spiking asp.sln
	/*	k0=0.9951729;
		a0=0.0038461864;
		b0=9.2642765;
		d0=-6.0;
		C0=45.0;
		vR0=-57.28488;
		vT0=-23.15752;
		vPeak0=18.676178;
		c0=-47.334415;*/
				
		  //single behavior: D.NASP						
		/*		
 		k0=0.6966569;
		a0=0.0010694712;
		b0=-30.649458;
		d0=111.0;
		C0=242.0;
		vR0=-74.14897;
		vT0=-9.195671;
		vPeak0=17.514297;
		c0=-39.442986;
				*/		
		
			//single behavior NASP: 3 point fit, rebound
/*			
		k0=0.52686113;
		a0=0.002233231;	
		b0=6.152924;
		d0=-12.0;
		C0=253.0;
		vR0=-57.24822;
		vT0=-42.7758;
		vPeak0=81.81113;
		c0=-44.97293;
*/
						
				
		Izhikevich9pModel model = new Izhikevich9pModel();
		model.setK(k0);
		model.setA(a0);
		model.setB(b0);
		model.setD(d0);	
		model.setcM(C0);
		model.setvR(vR0);
		model.setvT(vT0);		
		model.setvMin(c0);	
        model.setvPeak(vPeak0);
        
		return model;
		
	}
	/*
	public ModelSpikePatternData getUVData(Izhikevich9pModel model){
		IzhikevichSolver solver = new IzhikevichSolver(model);
		IzhikevichSolver.RECORD_U = true;
		return solver.getSpikePatternAdapting().getModelSpikePatternData();
	}
	*/
	
	public SpikePatternAdapting getSpikePattern(Izhikevich9pModel model, double initV, double initU){
		IzhikevichSolver solver = new IzhikevichSolver(model, initV, initU);
		IzhikevichSolver.RECORD_U = true;
		return solver.getSpikePatternAdapting();
	}
}
