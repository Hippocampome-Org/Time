package ec.app.izhikevich.starter;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import ec.app.izhikevich.model.neurontypes.mc.CA3bPyramidal23223p2;
import ec.eval.Slave;
import ec.util.ParameterDatabase;

public class ECJStarterSlave {
	public static final float PATTERN_COUNT_W = 0f;
	public static final float V_BELOW_VMIN_VREST_W = 0.25f;	

	public static final float EXP_NORM_RATE_NS = 20;
	public static final float EXP_NORM_RATE_SFA = 40;
		
	private static final String tempSlaveParmHolder = "input/tempSlaveParmHolder.params";
	
	public static void main(String[] args) {
		String parmsFile = "input/izhikevichSlave.params";
		
		CA3bPyramidal23223p2.init(3,new int[]{0,0,0},2);
		
        MCParamFile ecjParamFile;
		try {	
			ecjParamFile = new MCParamFile(parmsFile, CA3bPyramidal23223p2.geneParms);
			ParameterDatabase parameterDB = ecjParamFile.getLoadedParameterDB();
		
			String comments = "temporary Slave Parameter holder";
			Writer writer = new FileWriter(tempSlaveParmHolder);
			parameterDB.store(writer, comments);
			
			Slave.main(new String[] {"-file", tempSlaveParmHolder});
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}        
	}
	
	
}


