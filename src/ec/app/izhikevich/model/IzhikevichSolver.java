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

public class IzhikevichSolver {
	//Default values for solver parameters
	public static final double SS = 0.1d; // step size
	private  static final double T0 = 0d ; // initial time
//	public  static final double TN = 1400d;
	private  static final double V0 =-30.31272623619253; // model's resting potential  
	private  static final double U0 = 20;//-1.07e-05; 
	//
	private double sS ; // step size
	private double t0 ; // initial time
	private double tN ;	
	private double v0 ;  //initial condition
	private double u0 ;  //initial condition
	
	private IzhikevichModel model;
	private EPSPHandler epspHandler;
	
	public static boolean RECORD_U = false;
	
	public IzhikevichSolver(IzhikevichModel model) {
		//5p model
		this.model = model;
		//override TN
		double tn = (model.getTimeMin()*2) + model.getDurationOfCurrent();
		
		double _v0 = V0;
		double _u0 = U0;
		
		this.setParameters(SS, T0, tn, _v0, _u0);
		this.epspHandler = null;
	}
	public IzhikevichSolver(IzhikevichModel model, EPSPHandler epspHandler) {
		//5p model
		this.model = model;
		//override TN
		double tn = (model.getTimeMin()*2) + model.getDurationOfCurrent();
		this.setParameters(SS, T0, tn, V0, U0);
		this.epspHandler = epspHandler;
	}
	
	public IzhikevichSolver(Izhikevich9pModel model) {
		//9p model
		this.model = model;
		//override TN
		double tn = (model.getTimeMin()*2) + model.getDurationOfCurrent();
		this.setParameters(SS, T0, tn, model.getvR(), U0);
		this.epspHandler = null;
	}
	
	public IzhikevichSolver(Izhikevich9pModel model, double initV, double initU) {
		//9p model
		this.model = model;
		//override TN
		double tn = (model.getTimeMin()*2) + model.getDurationOfCurrent();
		this.setParameters(SS, T0, tn, initV, initU);
		this.epspHandler = null;
	}
	
	public IzhikevichSolver(Izhikevich9pModel model, EPSPHandler epspHandler) {
		//9p model
		this.model = model;
		//override TN
		double tn = (model.getTimeMin()*2) + model.getDurationOfCurrent();
		this.setParameters(SS, T0, tn, model.getvR(), U0);
		this.epspHandler = epspHandler;
	}
	
	public void setParameters(double ss, double t0, double tn, double v0, double u0) {
		this.setsS(ss);
		this.t0 = t0;
		this.tN = tn;
		this.v0 = v0;
		this.u0 = u0;
	}
	
	
	
	public SpikePatternAdapting getSpikePatternAdapting() {
		ODESolution solution = this.solveModelUsingRKInt();
		if(solution !=null) {
	        ModelSpikePatternData spike_pattern_data = new  ModelSpikePatternData(solution.getTime(), solution.getVoltage(), solution.getSpikeTimes());
	        if(RECORD_U) {
	        	spike_pattern_data.setRecoveryU(solution.getRecoveryU());
	        	spike_pattern_data.setvDerivs(solution.getVoltageDerivs());
	        }
	        SpikePatternAdapting model_spike_pattern = new SpikePatternAdapting(spike_pattern_data, 
	        																		((Izhikevich9pModel)model).current,
	        																		((Izhikevich9pModel)model).timeMin,
	        																		((Izhikevich9pModel)model).durationOfCurrent,
	        																		((Izhikevich9pModel)model).vR);
	        return model_spike_pattern;
		}
		else return null;
	}
	
	public ODESolution solveModelUsingRKInt() {
		ClassicalRungeKuttaIntegrator rkt = new ClassicalRungeKuttaIntegrator(getsS());
		FirstOrderDifferentialEquations ode = model;		
		
		double[] y0 = new double[] {v0, u0 }; // initial state
		double[] y = new double[y0.length];		
		CustomStepHandler stepHandler = new CustomStepHandler();	
		rkt.addStepHandler(stepHandler);
		CustomEventHandler eventHandler= new CustomEventHandler(model.getvPeak(), model.getC(), model.getD());		
		
		
		/*
		 * Event handler parameters : IMPORTANT!!
		 */
		double maxCheckInterval = 0.01; // maximal time interval between switching function checks 
									  //(this interval prevents missing sign changes in case the integration steps becomes very large)
		double convergenceThreshold = 0.001; //  convergence threshold in the event time search
		int maxIterationCount = 100; // upper limit of the iteration count in the event time search

		//rkt.addEventHandler(eventHandler, 10, 0.5, 100);
		rkt.addEventHandler(eventHandler, maxCheckInterval, convergenceThreshold , maxIterationCount); // Univariate Solver - default root finding algorithm; 5th parm
		if(epspHandler!=null) {
			rkt.addEventHandler(epspHandler, maxCheckInterval, convergenceThreshold , maxIterationCount); 
		}
		
		try{
			rkt.integrate(ode, t0, y0, tN, y);
		}catch(NoBracketingException nbe) {
			return null;		
		}
		ODESolution solution = null;
		if(!RECORD_U){
			solution = new ODESolution(stepHandler.getXi(), stepHandler.getYi(), eventHandler.spikeTimes);
		}else{
			solution = new ODESolution(stepHandler.getXi(), stepHandler.getYi(), stepHandler.getZi(),  eventHandler.spikeTimes);
		}	 
		
		return solution;
	}
	
	public static EPSPHandler getEPEpspHandler(Izhikevich9pModel model, double time, double mag) {
		EPSPHandler epspHandler = new EPSPHandler(model.getvR(), time, mag);
		return epspHandler;
	}
	/*
	public static EPSPHandler getEPEpspHandler(IzhikevichModel model, double time, double mag) {
		EPSPHandler epspHandler = new EPSPHandler(-65, time, mag);
		return epspHandler;
	}
	*/
	public double getsS() {
		return sS;
	}

	public void setsS(double sS) {
		this.sS = sS;
	}
}
/*
 * continuous output model of numeric solver
 */
class CustomStepHandler implements StepHandler {
	ArrayList<Double> xI; 
	ArrayList<Double> yI;	
	//ArrayList<Double> dyI;
	ArrayList<Double> zI;
	
	public CustomStepHandler() {
		xI = new ArrayList<>();
		yI = new ArrayList<>();	
		//dyI = new ArrayList<>();	
		
		if(IzhikevichSolver.RECORD_U)
			zI = new ArrayList<>();
		
		
	}		
	@Override
	public void handleStep(StepInterpolator interpolator, boolean isLast)
			throws MaxCountExceededException {		
		xI.add(interpolator.getCurrentTime());
        yI.add(interpolator.getInterpolatedState()[0]);
       // dyI.add(interpolator.getInterpolatedDerivatives()[0]);
        if(IzhikevichSolver.RECORD_U)
        	zI.add(interpolator.getInterpolatedState()[1]);		
	}
	@Override
	public void init(double arg0, double[] arg1, double arg2) {	}	
    public double[] getXi() {  	return GeneralUtils.listToArrayDouble(xI);   }    
    public double[] getYi() { 	return GeneralUtils.listToArrayDouble(yI);   }
   // public double[] getdYi() { 	return GeneralUtils.listToArrayDouble(dyI);   }
    public double[] getZi() { 	return GeneralUtils.listToArrayDouble(zI);   }
}

class CustomEventHandler implements EventHandler {
	double c;
	double d;
	double vPeak;	
	ArrayList<Double> spikeTimes;
	
	public CustomEventHandler(double vpeak, double c, double d) {
		this.c = c;
		this.d = d;
		this.vPeak = vpeak;
		spikeTimes = new ArrayList<>();
	}
	
	@Override
	public Action eventOccurred(double t, double[] y, boolean increasing) {		
		//return Action.CONTINUE;
		
		if(!increasing)
			return Action.RESET_STATE;
		else {
			return Action.CONTINUE;		}
	}

	@Override
	public double g(double t, double[] y) {
		return vPeak - y[0];
	}

	@Override
	public void init(double arg0, double[] arg1, double arg2) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void resetState(double t, double[] y) {
		spikeTimes.add(t);
		y[0] = c;
		y[1] += d;
	}	
}


class EPSPHandler implements EventHandler {
	double vr;
	double epspTime;
	double epspMag;
	//double vPeak;	
	
	public EPSPHandler(double vr, double epsp_time, double epsp_mag) {
		this.vr = vr;
		this.epspTime = epsp_time;
		this.epspMag = epsp_mag;		
	}
	@Override
	public Action eventOccurred(double t, double[] y, boolean increasing) {		
		//epspTime = 100000;
		//if(!increasing)
			return Action.RESET_STATE;
		//else {
		//	return Action.CONTINUE;		}
	}

	@Override
	public double g(double t, double[] y) {
		return epspTime - t;
	}

	@Override
	public void init(double arg0, double[] arg1, double arg2) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void resetState(double t, double[] y) {
	//	System.out.println(t);
		y[0] =  epspMag;
		//y[1] += d;
	}	
}
/*
 * to test spike time detection! with izh.
 */
/*class CustomEventHandlerErmen2 implements EventHandler {
	final static double dvThresh = 10;
	boolean threshold_crossed;
	CustomStepHandler stepHandler;
	ArrayList<Double> spikeTimes;
	
	public CustomEventHandlerErmen2(CustomStepHandler stepHandler) {
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
		
		double dy_current = stepHandler.currentDY;
		System.out.println( t+" "+dy_current);
		if(dy_current > dvThresh) {
			//System.out.println( t+" "+dy_current);
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
}*/
