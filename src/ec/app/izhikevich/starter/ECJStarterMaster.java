package ec.app.izhikevich.starter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import ec.EvolutionState;
import ec.Evolve;
import ec.app.izhikevich.inputprocess.labels.ModelParameterID;
import ec.app.izhikevich.model.neurontypes.mc.CA3bPyramidal23223p2;
import ec.app.izhikevich.model.neurontypes.mc.EAGenes;
import ec.eval.Slave;
import ec.util.Output;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

public class ECJStarterMaster {
	public static final float PATTERN_COUNT_W = 0f;
	public static final float V_BELOW_VMIN_VREST_W = 0.25f;	

	public static final float EXP_NORM_RATE_NS = 20;
	public static final float EXP_NORM_RATE_SFA = 40;
		
	public static void main(String[] args) {
		String parmsFile = "input/izhikevichMaster.params";
		CA3bPyramidal23223p2.init(3,new int[]{0,0,0},2);
		
        MCParamFile ecjParamFile;
		try {	
			/*
			 * setup Gene parms - neuron type dependent
			 */
			ecjParamFile = new MCParamFile(parmsFile, CA3bPyramidal23223p2.geneParms);
			ParameterDatabase parameterDB = ecjParamFile.getLoadedParameterDB();
			
		//	ecjParamFile.displayParameterDB();
			int nJobs = ecjParamFile.getParameterFromDB("jobs");
			
			for(int i=0; i<nJobs; i++)
				{
					Output output = Evolve.buildOutput();
					output.setFilePrefix("job."+i+".");
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


