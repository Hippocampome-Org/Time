package ec.app.izhikevich.resonate;

import java.io.FileWriter;
import java.io.IOException;

import ec.app.izhikevich.model.Izhikevich9pModel;
import ec.app.izhikevich.model.IzhikevichSolver;
import ec.app.izhikevich.spike.ModelSpikePatternData;
import ec.app.izhikevich.spike.SpikePatternAdapting;
import ec.app.izhikevich.util.ModelFactory;

public class VelocityUV {

	public static void main(String[] args) {
		Izhikevich9pModel model = ModelFactory.getUserDefinedModel();
		double I = 500;
		double initV = -67.7;
		double initU = 0;
		model.setInputParameters(I, 0, 1000);
		IzhikevichSolver solver = new IzhikevichSolver(model, initV, initU);
		IzhikevichSolver.RECORD_U = true;
		SpikePatternAdapting spattern = solver.getSpikePatternAdapting();
		
		ModelSpikePatternData mspData = spattern.getModelSpikePatternData();
		
		double[] time = mspData.getTime();
		double[] v = mspData.getVoltage();
		double[] u = mspData.getRecoveryU();	
		double vectorMag = 0;
		
		String fileName = "/Users/sivaven/Documents/workspace/Dynamics/velv2.dat";
		try {
			FileWriter fw = new FileWriter(fileName);
			for(int i=0;i<v.length;i++) {
				double[] dy=new double[2];
				double[] y =new double[] {v[i], u[i]};
				model.computeDerivatives(time[i], y, dy);
				vectorMag = Math.sqrt(dy[0]*dy[0]+dy[1]*dy[1]);
				
				fw.write(v[i]+"\t"+u[i]+"\t"+vectorMag+"\n");
			}	
			fw.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		
	}
}
