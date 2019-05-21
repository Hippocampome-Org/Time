package ec.app.izhikevich.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import ec.app.izhikevich.evaluator.MultiCompConstraintEvaluator;
import ec.app.izhikevich.model.neurontypes.mc.EAGenes;
import ec.app.izhikevich.model.neurontypes.mc.OneNeuronInitializer;

public class CARL_PostECJWrapper1 {
	public static final int N_SUBPOP = 10;
	private static final String MASTER_DIR = "/scratch/siyappan/output/mcdone_noplot/";//"/home/siyappan/4c2/";//"/home/siyappan/4c2_noplot/"; // 
	private static final String MASTER_INPUT = "post_ecj_wrapper_input"; //"completed_mc_input";//
	private static final int DEVICE_ID = 0;
	File ecjopdir;
	File resultsdir;
	
	public CARL_PostECJWrapper1(String ecjopdir, String resultsdir, String raw_primary_input, String carlWrapperScript, String nthSubpop){
		this.ecjopdir = new File(MASTER_DIR+ecjopdir);
		this.resultsdir = new File(MASTER_DIR+resultsdir);
		
		if(!this.ecjopdir.exists()) {
			throw new IllegalStateException(this.ecjopdir.getAbsolutePath()+" doesn't exist!");			
		}
		if(!this.resultsdir.exists()) {
			boolean success = this.resultsdir.mkdir();
			if(!success) {
				throw new IllegalStateException(this.resultsdir.getAbsolutePath()+" creation unsuccessful!");	
			}
		}	
		int nth_sp = Integer.valueOf(nthSubpop);
		if(!(nth_sp>-1))
			generateBestGenes0(readBestParmsS());
		else
			generateBestGenes0(readBestParmsS()[nth_sp]);
		
		String bestGenesFile = this.resultsdir.getAbsolutePath()+"/bestGenes0";
		String resultsDir =  this.resultsdir.getAbsolutePath();
		invokeCARL(bestGenesFile, resultsDir, DEVICE_ID, true, carlWrapperScript);
		
		DisplayUtilwCarlPhenoType.main(new String[] {raw_primary_input, resultsDir, nthSubpop});
		moveFinalResults();
	}
	
	private double[][] readBestParmsS(){
		return ECStatOutputReader.readBestSolutionS(ecjopdir.getAbsolutePath()+"/job.0.Full", 50, N_SUBPOP);
	}
	private void generateBestGenes0(double[][] parms) {
		EAGenes genes = new EAGenes(parms[0],  false);
		try{
			FileWriter fw = new FileWriter(new File(resultsdir.getAbsolutePath()+"/bestGenes0"));	
			for(int n=0;n<parms.length;n++){
				for(int i=0;i<EAGenes.geneLength;i++){
					fw.write(String.valueOf(parms[n][i]));
					if(i<EAGenes.geneLength-1)
						fw.write(",");				
				}
				fw.write("\n");
			}			
			fw.close();
			
			File copyecjopfile = new File(ecjopdir.getAbsolutePath()+"/job.0.Full");
			File destfile =  new File(resultsdir.getAbsolutePath()+"/job.0.Full");
			Files.copy(copyecjopfile.toPath(), destfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			
		}catch(IOException io){
			io.printStackTrace();
		}		
	}
	
	private void generateBestGenes0(double[] parms) {
		EAGenes genes = new EAGenes(parms,  false);
		try{
			FileWriter fw = new FileWriter(new File(resultsdir.getAbsolutePath()+"/bestGenes0"));			
				for(int i=0;i<EAGenes.geneLength;i++){
					fw.write(String.valueOf(parms[i]));
					if(i<EAGenes.geneLength-1)
						fw.write(",");				
				}
				fw.write("\n");
						
			fw.close();
			
			File copyecjopfile = new File(ecjopdir.getAbsolutePath()+"/job.0.Full");
			File destfile =  new File(resultsdir.getAbsolutePath()+"/job.0.Full");
			Files.copy(copyecjopfile.toPath(), destfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			
		}catch(IOException io){
			io.printStackTrace();
		}		
	}
	private void invokeCARL(String bestGenesFile, String resultsDir, int deviceID, boolean displayErrStream, String carlScript) {
		List<String> command = new ArrayList<String>();
		command.add("bash");
		command.add("-c");
		//command.add("cat "+bestGenesFile+" | "+"/home/siyappan/CARLsim3.1/projects/tuneIzh9p/"+carlScript+" -device "+deviceID);
		command.add("cat "+bestGenesFile+" | "+"/scratch/siyappan/projects/tuneIzh9p/"+carlScript+" -device "+deviceID);

		//command.add("-device");
		//command.add(""+deviceID);
		
		ProcessBuilder pb = new ProcessBuilder(command);
		String errorString = "";
		try {
			Process p = pb.start();		
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while(true){
				String retVal = in.readLine();				
				if(retVal!=null) {
					errorString+=retVal;	
				}else{
					break;
				}	
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}	
		if(displayErrStream)
			System.out.println(errorString);
	}
	
	private void moveFinalResults() {
		File file = new File("results");
		File[] files = file.listFiles();
		try {			
			for(File f:files) {
				String fname = f.getName();
				File destfile =  new File(resultsdir.getAbsolutePath()+"/"+fname);				
				Files.copy(f.toPath(), destfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				f.delete();
			}
			}catch (IOException e) {
				e.printStackTrace();
		}		
	}

	public static void main(String[] args) {
	/* 
	 * 1. Read raw_primary input
	 * 2. make directory if doesn't exist
	 *     - generate bestGenes_0
	 *     - copy job.0.full
	 * 3. call DisplayUtilWCA-- .main(raw_primary input)
	 */
		int nlines = Integer.valueOf(args[0]);		
		try {
			BufferedReader br = new BufferedReader(new FileReader(MASTER_INPUT));				
			for(int n=0;n<nlines;n++) {
				String masterline = br.readLine();
				StringTokenizer st = new StringTokenizer(masterline, ";");
				String ecjopdir = st.nextToken();
				String raw_primary_input = st.nextToken();
				String nthsubpop = "-1";
				if(st.hasMoreTokens()) {
					nthsubpop=st.nextToken();
				}
				StringTokenizer st2 = new StringTokenizer(raw_primary_input, ",");
				String phen_category = st2.nextToken();
				String phen_num = st2.nextToken();
				String Neur = st2.nextToken();
				///System.out.println(st.nextToken());
				int nComp = Integer.valueOf(st2.nextToken());
				int[] forConnIdcs = new int[nComp];
				for(int i=0;i<nComp;i++)
					forConnIdcs[i]=Integer.valueOf(st2.nextToken());
				boolean iso_comp = false;
				if(st.hasMoreTokens()){
					iso_comp = Boolean.valueOf(st2.nextToken());
				}
				String primary_input_json = "input/"+phen_category+"/"+phen_num+"/"+Neur+".json";
				OneNeuronInitializer.init(nComp, forConnIdcs, primary_input_json, iso_comp);	
				
				
				CARL_PostECJWrapper1 wrapper = new CARL_PostECJWrapper1(ecjopdir, "results_"+ecjopdir, raw_primary_input, carlWrapperScript(nComp), nthsubpop);
				
			}
			br.close();
		}catch(Exception io) {
			io.printStackTrace();
		}

	}
	
	private static String carlWrapperScript(int nComp) {
		String script = "";
		
		if(nComp ==4 && MultiCompConstraintEvaluator.forwardConnectionIdcs[2]==1) {
			script = "carlsim_tuneIzh9p_4c1_wrapper";
		}
		if(nComp ==4 && MultiCompConstraintEvaluator.forwardConnectionIdcs[2]==0) {
			script = "carlsim_tuneIzh9p_4c2_wrapper";
		}
		
		if(nComp ==3 && MultiCompConstraintEvaluator.forwardConnectionIdcs[2]==1) {
			script = "carlsim_tuneIzh9p_3c1_wrapper";
		}
		if(nComp ==3 && MultiCompConstraintEvaluator.forwardConnectionIdcs[2]==0) {
			script = "carlsim_tuneIzh9p_3c2_wrapper";
		}
		
		if(nComp == 2)
			script = "carlsim_tuneIzh9p_2c_wrapper";
		if(nComp == 1)
			script = "carlsim_tuneIzh9p_1c_wrapper";
		return script;
	}

	public static int mclayoutcode(int nComp) {
		int layoutcode=-1;
		if(nComp == 1)
			layoutcode = 0;
		
		if(nComp == 2)
			layoutcode = 1;
		
		if(nComp ==3 && MultiCompConstraintEvaluator.forwardConnectionIdcs[2]==1) { //3c1
			layoutcode = 2;
		}
		if(nComp ==3 && MultiCompConstraintEvaluator.forwardConnectionIdcs[2]==0) { //3c2
			layoutcode = 3;
		}
		
		if(nComp ==4 && MultiCompConstraintEvaluator.forwardConnectionIdcs[2]==1) { //4c1
			layoutcode = 4;
		}
		if(nComp ==4 && MultiCompConstraintEvaluator.forwardConnectionIdcs[2]==0) { //4c2
			layoutcode = 5;
		}
		
		return layoutcode;
	}
}
