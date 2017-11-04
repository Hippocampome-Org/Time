package ec.app.izhikevich.model;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;

/*
 * Simple 5 parameter izhikevich model
 */
public class IzhikevichModel implements FirstOrderDifferentialEquations{
	
	//DEFAULT values	
/*	public static final double CURRENT = 800d;  //input	
	protected static final double TIME_MIN = 100d;	//stimulation start time
	protected static final double TIME_MAX = 900d; 
	public static final double DURATION_OF_CURRENT = TIME_MAX-TIME_MIN;
	
	//EP parms : common to both 5p and 9p models
	public static final double V_PEAK = 3d;	
	protected static final double V_MIN = -55d; // C
		
	//Izh parms		
	protected static final double A = 0.025d; //0.25
	protected static final double B = -5d;
	protected static final double C = V_MIN;   //Vmin
	protected static final double D = 120d;
	*/
	//Constants of 5 parameter model:
	private static final double K1 = 0.04d;
	private static final double K2 = 5.0d;
	private static final double K3 = 140d;
		
	//attributes
	protected   double current;
	protected	double durationOfCurrent;
	protected   double timeMin;
	protected	double timeMax; // set based on the duration of current
		
	protected   double a ; 
	protected   double b ;
	protected   double c ; //vmin
	protected   double d;
	
	protected   double vPeak ;	
	protected   double vMin;
	
	public IzhikevichModel(){
		//set default values when the object is instantiated
	//	this.setInputParameters(CURRENT, TIME_MIN, DURATION_OF_CURRENT);
	/*	this.current = current;
		this.timeMin = timeMin;
		this.durationOfCurrent = durationOfCurrent;
		this.timeMax = timeMin + durationOfCurrent;
		*/
//		this.setEpParameters(V_PEAK, V_MIN);
//		this.setModelParameters(A, B, D);
	}
	
	public void setInputParameters(double current, double time_min, double duration_of_current) {
		this.current = current;
		this.timeMin = time_min;
		this.timeMax = time_min + duration_of_current;	
		this.durationOfCurrent = duration_of_current;
	}
	
	public void setEpParameters(double vpeak, double vmin) {
		this.vPeak = vpeak;		
		this.vMin = vmin;		
		this.c = vmin;
	}	
	
	public void setModelParameters(double a, double b, double d) {		
		this.setA(a);
		this.setB(b);		
		this.setD(d);
	}
		
	public double getCurrent() { return current;}
	public void setCurrent(double current) {this.current = current;	}	
	public double getA() {	return a;}
	public void setA(double a) {this.a = a;	}
	public double getB() {	return b;}
	public void setB(double b) {this.b = b;}
	public double getC() {return c;}
	public void setC(double c) {this.c = c;	}
	public double getD() {	return d;}
	public void setD(double d) {this.d = d;}
	public double getvPeak() {return vPeak;	}
	public void setvPeak(double vPeak) {this.vPeak = vPeak;}	
	public void setvMin(double vMin) {this.c = vMin; this.vMin = vMin;}
	public double getDurationOfCurrent() {	return durationOfCurrent;}	
	public void setDurationOfCurrent(double duration) {this.durationOfCurrent = duration;}		
	public double getTimeMin() {return this.timeMin;}
	
	@Override
	public void computeDerivatives(double t, double[] y, double[] dy)
			throws MaxCountExceededException, DimensionMismatchException {		
		double appCurrent;
		if(t>=timeMin && t<=timeMax) {
			appCurrent = current;
		}else
			appCurrent = 0;		
		
		/*if(y[0] >= vPeak) {
			y[0] = c;
			y[1] += d;					
		}*/	
		double V = y[0];
		double U = y[1];	
					
		dy[0] = (K1 * V * V) + (K2 * V) + K3 - U + appCurrent;  		
		dy[1] = a * ((b*V) - U);
	}

	@Override
	public int getDimension() {		
		return 2;
	}

}


