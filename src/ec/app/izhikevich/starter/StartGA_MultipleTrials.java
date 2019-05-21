package ec.app.izhikevich.starter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ec.Evolve;

public class StartGA_MultipleTrials {
	public static final float PATTERN_COUNT_W = 0f;
	public static final float V_BELOW_VMIN_VREST_W = 0.5f;	

	public static final float EXP_NORM_RATE_NS = 20;
	public static final float EXP_NORM_RATE_SFA = 40;
	
	private static void runGAForFitnessWeightCombinations(String[] args) {
		String[] args2 = new String[args.length + 4];
		//base parm file
		args2[0] = args[0];
		args2[1] = args[1];
		
	//	int combination = 0;
		File combFile = new File("combFile_final");
		try {
			FileWriter fw = new FileWriter(combFile);
			
		for(int w_fsl=0; w_fsl < FSL_WEIGHT.length; w_fsl++)
			for(int w_isi0=0; w_isi0 < ISI0_WEIGHT.length; w_isi0++)
				for(int w_sfa=0; w_sfa < SFA_WEIGHT.length; w_sfa++)
					for(int w_lastnisi=0; w_lastnisi < LASTNISI_WEIGHT.length; w_lastnisi++)
						for(int w_psi=0; w_psi < PSI_WEIGHT.length; w_psi++)
					{		
						fsl_weight = FSL_WEIGHT[w_fsl];
						isi0_weight = ISI0_WEIGHT[w_isi0];
						sfa_weight = SFA_WEIGHT[w_sfa];
						lastnisi_weight = LASTNISI_WEIGHT[w_lastnisi];
						psi_weight = PSI_WEIGHT[w_psi];
						
				//		float[] fitness = new float[N_TRIALS];
					//	for(int trial = 0; trial <N_TRIALS; trial++) 
						{						
							args2[2] = "-p";
							args2[3] = "stat.file=../output/$Full_";
							
							args2[4] = "-p";
							args2[5] = "stat.child.0.file=../output/$Stat_";
							
							Evolve.main(args2);		
				/*			fitness[trial] = ECStatOutputReader.readBestFitness(ECStatOutputReader.fileNamePrefix+"Full_"+combination+"_"+trial, 5);
						
							float[] parms = ECStatOutputReader.readBestSolution(ECStatOutputReader.fileNamePrefix+"Full_"+combination+"_"+trial, 6, 8);	
							String result = ASPDisplayUtil.avgFSLSFAError(parms, ModelEvaluator.EXP_SPIKE_PATTERN_DATA[0]);
							
							fw.write(combination+"."+trial+
									"\t" +fsl_weight+","+isi0_weight+","+sfa_weight+","+lastnisi_weight+","+psi_weight+
									"\t" + fitness[trial]+
									"\t"+result+"\n");								
							fw.flush();
							*/
							//fitness[trial] = ECStatOutputReader.readBestFitness(ECStatOutputReader.fileNamePrefix+"Full_"+trial, 5);
							
							//	float[] parms = ECStatOutputReader.readBestSolution(ECStatOutputReader.fileNamePrefix+"Full_"+"_"+trial, 6, 8);	
							//	String result = ASPDisplayUtil.avgFSLSFAError(parms, RegularSpikingAdaptingEvaluator.EXP_SPIKE_PATTERN_DATA[0]);
						}
					/*	for(int i=0; i<fitness.length; i++) {
							System.out.println(i + "\t" + fitness[i]);
						}*/
				//		GeneralUtils.displayArray(RegularSpikingAdaptingEvaluator.tempExpSampleISIs);
				//		System.out.println("Exp-sd:\t"+StatUtil.calculateStandardDeviation(RegularSpikingAdaptingEvaluator.tempExpSampleISIs));
						//combination++;
					}
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void runGASimple(String[] args) {
		String[] args2 = new String[args.length + 4];
		//base parm file
		args2[0] = args[0];
		args2[1] = args[1];
			
			args2[2] = "-p";
			args2[3] = "stat.file=../output/Full";
			
			args2[4] = "-p";
			args2[5] = "stat.child.0.file=../output/Stat";
			
			Evolve.main(args2);		
	
	}
	
	public static void main(String[] args) {
		runGASimple(args);
	}
	
	
	
	public static final float[] FSL_WEIGHT = {0.25f};
	public static float fsl_weight;
	
	public static final float[] ISI0_WEIGHT = {0.5f};
	public static float isi0_weight;
	
	public static final float[] SFA_WEIGHT = {0.25f};
	public static float sfa_weight;
	
	public static final float[] LASTNISI_WEIGHT = {0.5f};
	public static float lastnisi_weight;
	
	public static final float[] PSI_WEIGHT = {0.5f};
	public static float psi_weight;
}
