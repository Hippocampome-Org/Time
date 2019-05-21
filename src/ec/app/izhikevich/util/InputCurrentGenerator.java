package ec.app.izhikevich.util;

public class InputCurrentGenerator {

	//sin
	private double amplitude;
	private double phase_factor;	
	
	//ramp
	private double rampSlope;
	
	//step
	private double stepCurrentMag;
	
	//zap
	private static final double F0 = 1d;//Hz
	private static final double FM = 20d; //Hz
	private static final double T = 5; //seconds?
	
	private InputCurrentType iType;
	
	public InputCurrentGenerator(){
		iType = InputCurrentType.STEP;
	}
	public InputCurrentGenerator(double rampSlope){
		iType = InputCurrentType.RAMP;
		this.rampSlope = rampSlope;
	}	
	public InputCurrentGenerator(double amplitude, double phase_factor){
		iType = InputCurrentType.SIN;
		this.amplitude = amplitude;
		this.phase_factor = phase_factor;
	}	
	public InputCurrentGenerator(double amplitude, double f0, double fm, double T){
		this.amplitude = amplitude;
		iType = InputCurrentType.ZAP;
	}	
	public  double getCurrent(double time){	
		if(iType.equals(InputCurrentType.SIN)){
			return amplitude*Math.sin(.001*time*phase_factor);
		}
		
		if(iType.equals(InputCurrentType.RAMP)){
			return time*rampSlope;
		}
		
		if(iType.equals(InputCurrentType.STEP)){
			return getStepCurrentMag();
		}
		
		if(iType.equals(InputCurrentType.ZAP)){
			return getZapCurrent(time);
		}
		return 0;
	}
	
	private double getZapCurrent(double time) {		
		return amplitude*Math.sin(( 2 * Math.PI * getFreq(time) * time*.001d));
		//return amplitude*Math.sin(( 100 * time * time*.000001));
	}
	private double getFreq(double time) {
		double t_inSec = time*.001d;
		if(t_inSec < T)
			return F0+(FM-F0)*t_inSec/T;
		if(t_inSec >= T && t_inSec < 2*T )
			return (FM-(FM-F0)*(t_inSec-T))/T;
		return 0;			
	}
	public InputCurrentType getInputCurrentType(){
		return this.iType;
	}
	public static void main(String[] args) {
		for(double i=0;i<100;i+=.1){
			//System.out.println(i +"\t"+getCurrent(i, 2d));
		}
	}
	public double getStepCurrentMag() {
		return stepCurrentMag;
	}
	public void setStepCurrentMag(double stepCurrentMag) {
		this.stepCurrentMag = stepCurrentMag;
	}
}
