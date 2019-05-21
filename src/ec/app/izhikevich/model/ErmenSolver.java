package ec.app.izhikevich.model;

import java.util.ArrayList;

import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.events.EventHandler.Action;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;

import ec.app.izhikevich.spike.ModelSpikePatternData;
import ec.app.izhikevich.spike.SpikePatternAdapting;
import ec.app.izhikevich.util.GeneralUtils;

public class ErmenSolver {
	public static final double SS = 0.1d; // step size
	public static final double T0 = 0; //initial time
	private double tN ;	
	private double v0 ;  //initial condition
	private double w0 ;  //initial condition
	
	private Ermen model;
	
	public static boolean RECORD_W = false;

	public ErmenSolver(Ermen model, double initV, double initW) {
		this.model = model;
		this.tN = model.getTimeMin()*2 + model.getDurationOfCurrent();
		this.v0=initV;
		this.w0=initW;
	}
	
	
	public SpikePatternAdapting getSpikePatternAdapting() {
		ODESolution solution = this.solveModelUsingRKInt();
		if(solution !=null) {
	        ModelSpikePatternData spike_pattern_data = new  ModelSpikePatternData(solution.getTime(), solution.getVoltage(), solution.getSpikeTimes());
	       spike_pattern_data.setvDerivs(solution.getVoltageDerivs());
	        if(RECORD_W)
	        	spike_pattern_data.setRecoveryU(solution.getRecoveryU());
	        
	        SpikePatternAdapting model_spike_pattern = new SpikePatternAdapting(spike_pattern_data, 
	        																		model.I,
	        																		model.timeMin,
	        																		model.getDurationOfCurrent(),
	        																		model.getVr());
	        return model_spike_pattern;
		}
		else return null;
	}
	
	public ODESolution solveModelUsingRKInt() {
		ClassicalRungeKuttaIntegrator rkt = new ClassicalRungeKuttaIntegrator(SS);
		FirstOrderDifferentialEquations ode = model;		
		
		double[] y0 = new double[] {v0, w0 }; // initial state
		double[] y = new double[y0.length];		
		CustomStepHandlerErmen stepHandler = new CustomStepHandlerErmen();	
		rkt.addStepHandler(stepHandler);
		
		/*CustomEventHandlerErmen eventHandler = new CustomEventHandlerErmen(stepHandler);
		double maxCheckInterval = 0.01; // maximal time interval between switching function checks 		 
		double convergenceThreshold = 0.001; //  convergence threshold in the event time search
		int maxIterationCount = 100;

		rkt.addEventHandler(eventHandler, maxCheckInterval, convergenceThreshold, maxIterationCount);
		*/
		try{
			rkt.integrate(ode, T0, y0, tN, y);
		}catch(NoBracketingException nbe) {
			nbe.printStackTrace();
			System.exit(0);
			//return null;		
		}
		ODESolution solution = null;
		if(!RECORD_W){
			solution = new ODESolution(stepHandler.getXi(), stepHandler.getYi(), stepHandler.getdYi(), true);
		}else{
			solution = new ODESolution(stepHandler.getXi(), stepHandler.getYi(), stepHandler.getZi(), stepHandler.getdYi(), true);
		}	 
		
		return solution;
	}
	
}

class CustomStepHandlerErmen implements StepHandler {
	ArrayList<Double> xI; 
	ArrayList<Double> yI;	
	ArrayList<Double> dyI;
	
	ArrayList<Double> zI;
	
	public CustomStepHandlerErmen() {
		xI = new ArrayList<>();
		yI = new ArrayList<>();	
		dyI = new ArrayList<>();
		
		if(ErmenSolver.RECORD_W)
			zI = new ArrayList<>();
	}		
	@Override
	public void handleStep(StepInterpolator interpolator, boolean isLast)
			throws MaxCountExceededException {		
		xI.add(interpolator.getCurrentTime());
        yI.add(interpolator.getInterpolatedState()[0]);
        dyI.add(interpolator.getInterpolatedDerivatives()[0]);
        if(ErmenSolver.RECORD_W)
        	zI.add(interpolator.getInterpolatedState()[1]);		
	}
	@Override
	public void init(double arg0, double[] arg1, double arg2) {	}	
    public double[] getXi() {  	return GeneralUtils.listToArrayDouble(xI);   }    
    public double[] getYi() { 	return GeneralUtils.listToArrayDouble(yI);   }
    public double[] getdYi() { 	return GeneralUtils.listToArrayDouble(dyI);   }
    public double[] getZi() { 	return GeneralUtils.listToArrayDouble(zI);   }
}
/*
class CustomEventHandlerErmen implements EventHandler {
	final static double dvThresh = 10;
	boolean threshold_crossed;
	CustomStepHandlerErmen stepHandler;
	ArrayList<Double> spikeTimes;
	
	public CustomEventHandlerErmen(CustomStepHandlerErmen stepHandler) {
		this.stepHandler = stepHandler;
		spikeTimes = new ArrayList<>();
		threshold_crossed = false;
	}
	
	@Override
	public Action eventOccurred(double t, double[] y, boolean increasing) {		
		threshold_crossed = false;
		spikeTimes.add(t);
		return Action.CONTINUE;		
	}

	@Override
	public double g(double t, double[] y) {
		double[] dy = stepHandler.getdYi();
		double dy_current = dy[dy.length-1];
		if(dy_current > dvThresh) {
			threshold_crossed = true;
		}
		if(threshold_crossed) {
			return dy_current - dvThresh;	
		}
		
		return +1;
	}

	@Override
	public void init(double arg0, double[] arg1, double arg2) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void resetState(double t, double[] y) {
		spikeTimes.add(t);
		
	}	
}
*/