package ec.app.izhikevich.resonate;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import ec.app.izhikevich.model.Izhikevich9pModel;
import ec.app.izhikevich.model.Izhikevich9pModel2CISO;
import ec.app.izhikevich.model.Izhikevich9pModelMC;
import ec.app.izhikevich.model.IzhikevichSolver;
import ec.app.izhikevich.model.IzhikevichSolverMC;
import ec.app.izhikevich.model.neurontypes.mc.OneNeuronInitializer;
import ec.app.izhikevich.spike.ModelSpikePatternData;
import ec.app.izhikevich.spike.SpikePatternAdapting;
import ec.app.izhikevich.starter.ECJStarterV2;
import ec.app.izhikevich.util.ModelFactory;

public class PhasePortrait_couple {
	
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
		ECJ_trial = 33;
		
		nComp = ECJStarterV2.N_COMP;
		forConnIdcs = ECJStarterV2.CONN_IDCS;		
		opFolder =phen_category+"/"+phen_num+"/"+Neur+"/"+exp;
	}
	
	public static void main(String[] args) {
		OneNeuronInitializer.init(ECJStarterV2.N_COMP, ECJStarterV2.CONN_IDCS, ECJStarterV2.PRIMARY_INPUT, ECJStarterV2.iso_comp);

		String csvFile = "theory/multibehavior_models/models.csv";
		Map<String, Izhikevich9pModel> models = ModelFactory.readModelsFromCSV(csvFile);
		
		String unique_id = "4-012-1";
		double inputCurrent = 500;
		double duration = 1000;
		double discard_duration = 0;
		
		Izhikevich9pModel model = models.get(unique_id);//ModelFactory.readModel(opFolder, ECJ_trial);//ModelFactory.getUserDefinedModel();//
		
		
		Izhikevich9pModelMC modelC = new Izhikevich9pModelMC(2);
		modelC.setK(new double[] {model.getK(), model.getK()});
		modelC.setA(new double[] {model.getA(), model.getA()});
		modelC.setB(new double[] {model.getB(), model.getB()});
		modelC.setD(new double[] {model.getD(), model.getD()});
		modelC.setcM(new double[] {model.getcM(), model.getcM()});
		modelC.setvR(model.getvR());
		modelC.setvT(new double[] {model.getvT(), model.getvT()});
		modelC.setvPeak(new double[] {model.getvPeak(), model.getvPeak()});
		modelC.setvMin(new double[] {model.getC(), model.getC()});
		modelC.setG(new double[] {0});
		modelC.setP(new double[] {0.5});
		
		simulateAndRecordStates(modelC , inputCurrent, duration, discard_duration, 
								unique_id+"_"+inputCurrent+"_2c");
	}	

	
	private static void simulateAndRecordStates(Izhikevich9pModelMC model, 
												double inputCurrent, double duration, double discard_duration,
												String opfileName){
		
		double[] currents = new double[] {inputCurrent, inputCurrent};
		PhasePortrait_couple pp = new PhasePortrait_couple();
			
		model.setInputParameters(currents, 0, duration);			
					
		SpikePatternAdapting[] spikePattern = pp.getSpikePattern(model);
		if(spikePattern==null || spikePattern[0]==null){
			System.out.print("xnull!");
			return;
		}
		//GeneralUtils.displayArray(spikePattern.getSpikeTimes());
		
		ModelSpikePatternData som =spikePattern[0].getModelSpikePatternData();
		ModelSpikePatternData dend =spikePattern[1].getModelSpikePatternData();
		
		double[] time = som.getTime();
		double[] v = som.getVoltage();
		//double[] u = som.getRecoveryU();	
		double[] time2 = dend.getTime();
		double[] v2 = dend.getVoltage();
		
		int discardPoints = (int) (discard_duration / IzhikevichSolverMC.SS);
		
		try {
			FileWriter fw = new FileWriter("theory/multibehavior_models/ppsANDtvs/"+opfileName);
			for(int i=discardPoints;i<time.length;i++){
				fw.write(time[i]+"\t"+v[i]+"\t"+time2[i]+"\t"+v2[i]+"\n");//"\t"+vderivs[i]+"\t"+u[i]+"\n");///iGenr.getCurrent(time[i])+"\n");
			}
			fw.flush();fw.close();	
			
		} catch (IOException e) {
			e.printStackTrace();
		}	
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
	
	public SpikePatternAdapting[] getSpikePattern(Izhikevich9pModelMC model){
		IzhikevichSolverMC solver = new IzhikevichSolverMC(model);
		IzhikevichSolverMC.RECORD_U = true;
		return solver.solveAndGetSpikePatternAdapting();
	}
}
