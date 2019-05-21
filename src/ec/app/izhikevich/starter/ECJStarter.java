package ec.app.izhikevich.starter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import ec.EvolutionState;
import ec.Evolve;
import ec.app.izhikevich.inputprocess.labels.ModelParameterID;
import ec.app.izhikevich.model.neurontypes.mc.CA3bPyramidal23223p2;
import ec.app.izhikevich.model.neurontypes.mc.EAGenes;
import ec.util.Output;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

public class ECJStarter {
//	public static final float PATTERN_COUNT_W = 0f;
//	public static final float V_BELOW_VMIN_W = 0.40f;	
//	public static final float V_BELOW_VR_W = 0.00f;	

//	public static final float PATTERN_W = 0.60f;	

	

	
		
	public static void main(String[] args) {
		String parmsFile = "input/izhikevich.params";
		//3 comps
		CA3bPyramidal23223p2.init(3, new int[]{0,0,0}, 2);
		//4 comps
		//CA3bPyramidal23223p2.init(4, new int[]{0,0,0,2}, 2);
		
        MCParamFile ecjParamFile;
		try {	
			/*
			 * setup Gene parms - neuron type dependent
			 */
			ecjParamFile = new MCParamFile(parmsFile, CA3bPyramidal23223p2.geneParms);
			ParameterDatabase parameterDB = ecjParamFile.getLoadedParameterDB();
			
		//	ecjParamFile.displayParameterDB();
			int nJobs = ecjParamFile.getParameterFromDB("jobs");
			if(args.length==0){
				//on Krasnow
			}else{	
				//on ARGO
				File opDir = new File("output//"+args[0]);
				if(!opDir.exists()){
					opDir.mkdir();
				}			
			}
				
				
			for(int i=0; i<nJobs; i++)
				{
					Output output = Evolve.buildOutput();
					if(args.length>0){
						//on ARGO
						output.setFilePrefix(args[0]+"//job."+args[1]+".");
					}else{
						//on KRASNOW
						output.setFilePrefix("job."+i+".");
					}
					
					final EvolutionState state = Evolve.initialize(parameterDB, i+1, output );	
					state.run(EvolutionState.C_STARTED_FRESH);
					
				}
	    
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
}



