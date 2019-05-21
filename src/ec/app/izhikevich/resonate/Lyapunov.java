package ec.app.izhikevich.resonate;

import ec.app.izhikevich.model.Izhikevich9pModel;
import ec.app.izhikevich.model.IzhikevichSolver;
import ec.app.izhikevich.model.neurontypes.mc.OneNeuronInitializer;
import ec.app.izhikevich.plot.ChaoticSpace;
import ec.app.izhikevich.spike.SpikePatternAdapting;
import ec.app.izhikevich.starter.ECJStarterV2;
import ec.app.izhikevich.util.ModelFactory;

public class Lyapunov {

	private static boolean displayStat = false;
	private static final double TOTAL_SIM_TIME=2000;
	public static final double DISCARD_TIME=1000;
	
			
	private double d0;
	
	public Lyapunov(double d0){			
		this.d0=d0;
	}
	
	public static TimePoint advanceOrbits(Izhikevich9pModel model, TimePoint currentTP, double timeStep){
		IzhikevichSolver solver = new IzhikevichSolver(model);
		solver.setParameters(IzhikevichSolver.SS, currentTP.t, currentTP.t+timeStep, currentTP.v, currentTP.u);				
		IzhikevichSolver.RECORD_U = true;
		
		SpikePatternAdapting pattern = solver.getSpikePatternAdapting();		
		double[] time = pattern.getSpikePatternData().getTime();
		double[] volt = pattern.getSpikePatternData().getVoltage();
		double[] u_ = pattern.getSpikePatternData().getRecoveryU();
		
		TimePoint nextTp = new TimePoint(time[time.length-1], volt[volt.length-1], u_[u_.length-1]);
		return nextTp;
	}
	
	public static void main(String[] args) {
		OneNeuronInitializer.init(ECJStarterV2.N_COMP, ECJStarterV2.CONN_IDCS, ECJStarterV2.PRIMARY_INPUT, ECJStarterV2.iso_comp);
		
		Izhikevich9pModel model0 =  ModelFactory.readModel(ChaoticSpace.FILE_PFX_globalSearch_results, 2);
		Izhikevich9pModel model1 = model0.cloneModel();
		
		double current = 705;		
		model0.setInputParameters(current, 0, 100000);
		model1.setInputParameters(current, 0, 100000);
		
		double d0 = 0.00000001;
		
		double tStep = IzhikevichSolver.SS;		
		TimePoint tp0 = new TimePoint(0, model0.getvR(), 0);
		TimePoint tp1 = new TimePoint(0, model1.getvR(), 0);
		
		//Lyapunov lp = new Lyapunov(0.1);
		
		tp0 = advanceOrbits(model0, tp0, tStep);
		tp1 = new TimePoint(tp1.t, tp1.v+d0, tp1.u);	
		tp1 = advanceOrbits(model1, tp1, tStep);
		
		double exp_sum = 0;
		for(int i=1;i<=1000000;i++){				
			//new separation
			double d1 = Math.sqrt((tp0.v-tp1.v)*(tp0.v-tp1.v) + (tp0.u-tp1.u)*(tp0.u-tp1.u));
			if(i>100000)
			{
				exp_sum += Math.log10(d1/d0);
				System.out.println(exp_sum/(i*1.0d));
			}
			tp0 = advanceOrbits(model0, tp0, tStep);			
			//reInit orbit b (model1)
			//x =v; b=1; a=0; 			
			double xb0 = tp0.v + d0*(tp1.v - tp0.v)/d1;
			double yb0 = tp0.u + d0*(tp1.u - tp0.u)/d1;			
			tp1 = new TimePoint(tp1.t, xb0, yb0);	
			tp1 = advanceOrbits(model1, tp1, tStep);			
		}
		
		
	}
	
	
}
