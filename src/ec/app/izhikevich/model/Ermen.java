package ec.app.izhikevich.model;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;


public class Ermen implements FirstOrderDifferentialEquations{
	
	public double tau_v;
	public double tau_w;
	
	public double n; 
	public double m;
	
	public double a;
	public double b;
	public double c;
	public double d;
	
	public double I;
	public double timeMin;
	public double timeMax;
	public double duration;
	
	public Ermen(double tau_v, double tau_w, double m, double n, double a, double b, double c, double d) {	
		this.tau_v = tau_v;
		this.tau_w=tau_w;
		this.m=m;
		this.n=n;
		this.a=a;
		this.b=b;
		this.c=c;
		this.d=d;
	}

	public void setInputParameters(double I, double time_min, double duration) {
		this.I=I;
		this.timeMin=time_min;
		this.duration=duration;
		this.timeMax = this.timeMin+this.duration;
	}
	
	@Override
	public void computeDerivatives(double t, double[] y, double[] dy)
			throws MaxCountExceededException, DimensionMismatchException {
		double appCurrent;
		if(t>=timeMin && t<=timeMax) {
			appCurrent = I;
		}else
			appCurrent = 0;		
		
		double V = y[0];
		double W = y[1];		
		dy[0] = tau_v * (appCurrent + (m+n)*V + (a/2d)*(V*V) - (b/3d)*(V*V*V) - W);
		dy[1] = tau_w * (n*V + (c/2d)*(V*V) - (d/3d)*(V*V*V) - W);		
	}
	
	@Override
	public int getDimension() {
		return 2;
	}
	
	public double getDurationOfCurrent() {
		return this.duration;
	}
	
	public double getTimeMin() {
		return this.timeMin;
	}
	
	public double getTimeMax() {
		return this.timeMax;
	}
	
	public double getVr() {
		return -60d;
	}
}
