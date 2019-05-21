package ec.app.izhikevich.resonate;

import java.io.FileWriter;
import java.io.IOException;

import ec.app.izhikevich.model.Ermen;
import ec.app.izhikevich.model.ErmenSolver;
import ec.app.izhikevich.model.IzhikevichSolver;
import ec.app.izhikevich.spike.ModelSpikePatternData;
import ec.app.izhikevich.spike.SpikePatternAdapting;
import ec.app.izhikevich.starter.ECJStarterV2;
import ec.app.izhikevich.util.GeneralUtils;
import ec.app.izhikevich.util.ModelFactory;

public class PhasePortrait2 {
	
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
		ECJ_trial = 11;
		
		nComp = ECJStarterV2.N_COMP;
		forConnIdcs = ECJStarterV2.CONN_IDCS;		
		opFolder =phen_category+"/"+phen_num+"/"+Neur+"/";
	}
	
	public static void main(String[] args) {
		int n = 653;
		for(int i=281;i<n;i++) {
			//i=36;
			Ermen model = ModelFactory.readErmenModel(i, false, true);//new Ermen(tau_v, tau_w, m, n, a, b, c, d);
			
			double inputCurrent=600;
			double duration=750;
			double discard_duration = 0;
			
			double initV = 0;
			double initW = 0;
			
			simulateAndRecordStates(model, inputCurrent, duration, discard_duration, 
					initV, initW, "ermen"+i);
		}
		
	}	

	
	private static void simulateAndRecordStates(Ermen model, 
												double inputCurrent, double duration, double discard_duration,
												double initV, double initW, String opfileName){
		PhasePortrait2 pp = new PhasePortrait2();
			
		model.setInputParameters(inputCurrent, 100, duration);			
					
		SpikePatternAdapting spikePattern = pp.getSpikePattern(model, initV, initW);
		if(spikePattern==null ){
			System.out.print("null!");
			return;
		}
		
		double[] spikeTimes = spikePattern.getSpikeTimes();
		System.out.println("\n"+spikeTimes.length+"spike times:");
		GeneralUtils.displayArray(spikeTimes);
		ModelSpikePatternData mspData =spikePattern.getModelSpikePatternData();
			
		double[] time = mspData.getTime();
		double[] v = mspData.getVoltage();
		double[] u = mspData.getRecoveryU();	
		
		double[] vDerivs = mspData.getvDerivs();
		
		int discardPoints = (int) (discard_duration / IzhikevichSolver.SS);
		
		try {
			FileWriter fw = new FileWriter("theory/multibehavior_models/ppsANDtvs/ermen/"+opfileName);
			for(int i=discardPoints;i<time.length;i++){
				fw.write(time[i]+"\t"+v[i]+"\t"+vDerivs[i]+"\t"+u[i]+"\n");///iGenr.getCurrent(time[i])+"\n");
			}
			fw.flush();fw.close();	
			
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public SpikePatternAdapting getSpikePattern(Ermen model, double initV, double initW){
		ErmenSolver solver = new ErmenSolver(model, initV, initW);
		ErmenSolver.RECORD_W = true;
		return solver.getSpikePatternAdapting();
	}
}
