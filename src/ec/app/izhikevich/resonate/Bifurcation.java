package ec.app.izhikevich.resonate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ec.app.izhikevich.model.Izhikevich9pModel;
import ec.app.izhikevich.model.Izhikevich9pModelMC;
import ec.app.izhikevich.model.IzhikevichSolver;
import ec.app.izhikevich.model.IzhikevichSolverMC;
import ec.app.izhikevich.model.neurontypes.mc.OneNeuronInitializer;
import ec.app.izhikevich.spike.ModelSpikePatternData;
import ec.app.izhikevich.spike.SpikePatternAdapting;
import ec.app.izhikevich.starter.ECJStarterV2;
import ec.app.izhikevich.util.GeneralFileWriter;
import ec.app.izhikevich.util.GeneralUtils;
import ec.app.izhikevich.util.ModelFactory;
import ec.app.izhikevich.util.PyInvoker;
import ec.app.izhikevich.util.analysis.Analysis;
import ec.app.izhikevich.util.analysis.NeuronPageEntries;
import ec.app.izhikevich.util.forportal.ModelDataStructure;
import ec.app.izhikevich.util.forportal.PortalInterface;

public class Bifurcation {

	private static boolean displayStat = false;
	private static final double TOTAL_SIM_TIME=2000;
	public static final double DISCARD_TIME=1000;
	private Izhikevich9pModel model;
	private double I;
	private SpikePatternAdapting spattern;
	
	static boolean  skipI = false;
	public Bifurcation(Izhikevich9pModel model, double I){
		this.model=model;
		this.I=I;
		
		this.model.setInputParameters(this.I, 0, TOTAL_SIM_TIME);
		IzhikevichSolver solver = new IzhikevichSolver(model);
		IzhikevichSolver.RECORD_U = true;
		spattern = solver.getSpikePatternAdapting();
	}
	
	public Bifurcation(Izhikevich9pModel model, double I, double initV, double initU){
		this.model=model;
		this.I=I;
		
		this.model.setInputParameters(this.I, 0, TOTAL_SIM_TIME);
		IzhikevichSolver solver = new IzhikevichSolver(model, initV, initU);
		IzhikevichSolver.RECORD_U = true;
		spattern = solver.getSpikePatternAdapting();
	}
	
	public Bifurcation(Izhikevich9pModelMC model, double I){
		this.model=null;
		this.I=I;
		
		model.setInputParameters(new double[]{this.I, 0}, 0, TOTAL_SIM_TIME);
		IzhikevichSolverMC solver = new IzhikevichSolverMC(model);
		IzhikevichSolverMC.RECORD_U = true;
		SpikePatternAdapting[] spatterns = solver.solveAndGetSpikePatternAdapting();
		if(spatterns!=null)
			spattern = spatterns[0];
		else
			skipI = true;
	}
	
	public static void main_v0() {
		OneNeuronInitializer.init(ECJStarterV2.N_COMP, ECJStarterV2.CONN_IDCS, ECJStarterV2.PRIMARY_INPUT, ECJStarterV2.iso_comp);
		String exp = "10_2_3/B8/7-000/0";//"1";//
		int trial=17;
		String csvFile = "theory/multibehavior_models/models.csv";
		Map<String, Izhikevich9pModel> models = ModelFactory.readModelsFromCSV(csvFile);
		
		String[] UnqiueIDS = {"7-000"};//{"4-036-1", "4-080-1",	"4-080-2","4-012-1","4-011-2","3-006","2-000-1","2-005-1","2-019"};
		double[] Imin      = {400};//{600, 403, 310, 400, 1100, 0, 700, 0 ,320};
		double[] Imax      = {650};//{700, 412, 322, 600, 1300, 0, 750, 0 ,380};
		double Cm=950;
		double b=-12;
		
		int n_neurons=1;
		
		for(int i=0;i<n_neurons;i++) {
			int N = 1000;
			String bifFile = "C:\\Users\\sivav\\Dropbox\\HCO\\Manuscript_III\\Fig1\\4012.dat";
			//"/Users/sivaven/Projects/TIMES/theory/multibehavior_models/PSTUTvsCHAOS/periodicityInSpace/zoomedin/"+UnqiueIDS[i]+"_C"+Cm+"_b"+b+"_bif_tr.dat";
			//"theory/multibehavior_models/bif_diagrams/"+UnqiueIDS[i]+"_bif_tr_nrwr.dat";
			Izhikevich9pModel model = ModelFactory.getUserDefinedModel();//ModelFactory.readModel(exp, trial);//models.get(UnqiueIDS[i]);//
			//model.setcM(Cm);
			//model.setB(b);
			writePcareMapVals(bifFile, model, Imin[i], Imax[i], N);
		}		
		System.out.println("done");
	}
	public static void main(String[] args) {
		String neuronSubtypeID ="4-012-1";
		//String fileName = "/1Bifurcation/"+neuronSubtypeID+"_tr.bif";
		String bifFile = "C:\\Users\\sivav\\Dropbox\\HCO\\Manuscript_III\\Fig1\\4012_sp_k0.dat";
		double Imin = 0;
		double Imax = 500;
		int N = 500;
		
		/*
		String IndexFileName = "C:\\Users\\sivav\\Dropbox\\HCO\\MCProgress_v3_15_18.xlsx";
		System.out.println("*********\nReading indexfile...");
		List<ModelDataStructure> mdsList_sc = PortalInterface.readFromProgressSheet(IndexFileName, 1);
		System.out.println("*********\nindexfile read!...");
		
		ModelDataStructure mds_scc = NeuronPageEntries.getModelDataStructure(mdsList_sc, neuronSubtypeID);
		*/
		Izhikevich9pModel model = ModelFactory.getUserDefinedModel();
		
		/*
		 * bifurcation
		 */
		writePcareMapVals(bifFile, model, Imin, Imax, N);
		
		/*
		 * plot T vs V
		 */
		/*
		String VOLTAGE_FILENAME_PFX = "C:\\Users\\sivav\\Dropbox\\HCO\\Manuscript_III\\Fig1\\"; 
		String opFile=VOLTAGE_FILENAME_PFX+"4012";
		int nScens=1;		
		int nComp=1;
		
		double[] currents= new double[] {500};
		PyInvoker invoker = new PyInvoker(opFile, nScens, nComp, model.getvR(), model.getvPeak(), currents );			
		invoker.setDisplayErrorStream(true);	
		int[] color_code_idcs = new int[] {2,2};
		invoker.setColorCodeIdcs(color_code_idcs );
		invoker.addClass(" "); invoker.addClass(" ");
		
		for(int i=0;i<nScens;i++) {
			model.setInputParameters(currents[i], 100, 750);
			IzhikevichSolver solver = new IzhikevichSolver(model);
			SpikePatternAdapting spattern = solver.getSpikePatternAdapting();
			
			double[]  times = spattern.getModelSpikePatternData().getTime();
			File tempFileT = new File(opFile+"_t"+i);
			GeneralFileWriter.write(tempFileT.getAbsolutePath(), times);
			
			double[]  vs = spattern.getModelSpikePatternData().getVoltage();				
			File tempFileV = new File(opFile+"_v"+i);				
			GeneralFileWriter.write(tempFileV.getAbsolutePath(), vs);	
		}		
		
		invoker.invokeForSC(opFile, "0");
		
		for(int i=0;i<nScens;i++) {
			File tempFileT = new File(opFile+"_t"+i);
			tempFileT.delete();			
			
			File tempFileV = new File(opFile+"_v"+i);				
			tempFileV.delete();						
		}	
		*/
	}
	
	public static void writePcareMapVals(String fileName, Izhikevich9pModel model, double Imin, double Imax, int N){
		
		double Iinterval = (Imax - Imin)/(double)N;
		try {
			FileWriter fw = new FileWriter(fileName);
			
			double initV = model.getvR();
			double initU = 0;
			
			for(int i=0;i<N;i++){
				double I = Imin + Iinterval*(double)i;
				//double I = Imax - Iinterval*(double)i;
			/*	MersenneTwister mst = new MersenneTwister(System.currentTimeMillis());
				initV = -70+mst.nextFloat(true, true)*50; //vv[vv.length-1];
				initU = mst.nextFloat(true, true)*100; 
				*/
				Bifurcation bf = new Bifurcation(model, I, initV, initU);
				/*
				 * to avoid reset for each I
				 */
				//ModelSpikePatternData mspData = bf.spattern.getModelSpikePatternData();
				//double[] vv = mspData.getVoltage();
				//double[] uu = mspData.getRecoveryU();
				
				/*
				 * 
				 */
				ArrayList<Double> u = bf.cutSSPcareU(model.getvPeak()-20);
				if(u==null) {
					System.out.println("skipped.."+ I+", "+initV+","+initU);
					continue;
				}
				for(double _u: u){
					fw.write(I +"\t"+_u+"\n");
				}
				fw.flush();
				if(i%100==0)
				System.out.println("\t"+I+"pA completed!"+","+initV+","+initU);
			}			
			fw.close();				
		} catch (IOException e) {
			e.printStackTrace();
		}				
	}

	public static void writePcareMapVals(String fileName, Izhikevich9pModelMC model, double Imin, double Imax, int N){
		
		double Iinterval = (Imax - Imin)/(double)N;
		try {
			FileWriter fw = new FileWriter("theory/"+fileName);
			
			for(int i=0;i<N;i++){
				double I = Imin + Iinterval*(double)i;
				//double I = Imax - Iinterval*(double)i;
				
				Bifurcation bf = new Bifurcation(model, I);
				if(skipI){
					System.out.println("skipped "+ I);
					skipI = false;
					continue;					
				}
				ArrayList<Double> u = bf.cutSSPcareU(model.getvPeak()[0]-20);
				for(double _u: u){
					fw.write(I +"\t"+_u+"\n");
				}
				fw.flush();
				System.out.println("\t"+I+"pA completed!");
			}			
			fw.close();				
		} catch (IOException e) {
			e.printStackTrace();
		}				
	}

	public ArrayList<Double> cutSSPcareU(double vPlane){		
		if(spattern==null || spattern.getNoOfSpikes()<2){
			return null;
		}
		ModelSpikePatternData mspData = spattern.getModelSpikePatternData();
		
		//double[] time = mspData.getTime();
		double[] v = mspData.getVoltage();
		double[] u = mspData.getRecoveryU();
		
		int discardPoints = (int) (DISCARD_TIME / IzhikevichSolver.SS);
		//System.out.println("discarded..."+discardPoints +" points!");
		
		ArrayList<Double> pCareCutUs = new ArrayList<Double>();
		double lastV = v[discardPoints-1];
		for(int i=discardPoints; i<u.length; i++ ){
			if(v[i]>=vPlane && lastV <vPlane){
				pCareCutUs.add(u[i]);
			}			
			lastV=v[i];
		}
		return pCareCutUs;
	}
	
	public int identifyPeriod(ArrayList<Double> U){
		int period = 0;
		if(U==null || U.isEmpty())
			return period;
		
		double[] dArr = GeneralUtils.listToArrayDouble(U);
		
		if(!hasCycle(dArr)){
			return period;
		}
		
		//double min = GeneralUtils.findMin(dArr);
		//double max = GeneralUtils.findMax(dArr);
		double normFact = 5;//(max-min)/100d;
		
		ArrayList<Double> uniqueU = new ArrayList<Double>();
		uniqueU.add(U.get(0));
		
		boolean found;
		for(int i=1;i<U.size();i++){
			found = false;
			for(int j=0;j<uniqueU.size();j++){
				if(GeneralUtils.isCloseEnough( U.get(i), uniqueU.get(j), normFact )){
					found = true;
					break;
				}			
			}
			if(!found){
				uniqueU.add(U.get(i));
			}
			
		}
		if(displayStat)
			System.out.println(uniqueU);
		//
		period = uniqueU.size();
		uniqueU = null;
		
		return period;
	}

	private boolean hasCycle(double[] SS_U) {
		double m=0;
		for(int i=1;i<SS_U.length;i++){
			if(SS_U[i] >= SS_U[i-1] )
				m+=1;
			else
				m-=1;
		}
		double avgM = Math.abs(m / ((SS_U.length-1)*1.0d));
		if(avgM < 0.5)
			return true;
		else
			return false;
	}
	public SpikePatternAdapting getSpattern(){
		return this.spattern;
	}
}
