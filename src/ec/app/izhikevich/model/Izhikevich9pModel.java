package ec.app.izhikevich.model;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;

import ec.app.izhikevich.inputprocess.labels.ModelParameterID;
import ec.app.izhikevich.spike.SpikePatternAdapting;
import ec.app.izhikevich.util.GeneralUtils;
import ec.app.izhikevich.util.InputCurrentGenerator;
import ec.app.izhikevich.util.InputCurrentType;

/*
 * inherits parms from 5p model, and 4 additional parameters
 */
public class Izhikevich9pModel extends IzhikevichModel{
	//Default values
/*	private static final double K = 0.55d;
	public static final double V_R = -58d;
	private static final double V_T = -38d;	
	private static final double CM = 457.61;	
	*/
	//Attributes
	protected   double k;	
	protected   double vR ;
	protected   double vT ;
	protected   double cM ;	
	
	private boolean isStepCurrent;
	private InputCurrentGenerator iGenr;
	
	public Izhikevich9pModel() {	
		
		//Default model parameters while model initialization
		super();
		isStepCurrent = true;
	//	this.setEpParameters(V_PEAK, V_MIN, V_R, V_T, CM);
	//	this.setModelParameters(K, A, B, D);
	}
	
	public void setEpParameters(double vpeak, double vmin, double vrest, double vthresh, double cm) {
		super.setEpParameters(vpeak, vmin);
		this.vR = vrest;		
		this.vT = vthresh;		
		this.cM = cm;
	}	
	
	public void setModelParameters(double k, double a, double b, double d) {		
		super.setModelParameters(a, b, d);
		this.setK(k);
	}	
	
	@Override
	public void computeDerivatives(double t, double[] y, double[] dy)
			throws MaxCountExceededException, DimensionMismatchException {
		double appCurrent;
		if(t>=timeMin && t<=timeMax) {
			if(isStepCurrent){
				appCurrent = current;
			}else{
				appCurrent = iGenr.getCurrent(t);
			}
		}else
			appCurrent = 0;		
		
		/*if(y[0] >= vPeak) {
			y[0] = c;
			y[1] += d;					
		}*/	
		double V = y[0];
		double U = y[1];		
		dy[0] = ((k * (V - vR) * (V - vT))  - U + appCurrent) / cM;
		dy[1] = a * ((b * (V - vR)) - U);		
		
			
	}
	
	public double getK() {	return k;}
	public void setK(double k) {this.k = k;	}
	public double getvR() {	return vR;}
	public void setvR(double vR) {	this.vR = vR;}
	public double getvT() {	return vT;}
	public void setvT(double vT) {this.vT = vT;}
	public double getcM() {	return cM;}
	public void setcM(double cM) {this.cM = cM;	}
	public double getvMinOffset() {return this.vMin-this.vR;}
	
	public double  getRheo(float currDur, double iMin, double iMax, double incStep)
	   {
	     double rheo = Float.MAX_VALUE;
	     double holdIMax = iMax;
	     int nSpikes = 0;
	     while(iMin <= iMax)    {	
	    	rheo = (iMin + iMax) / 2;	
			this.setInputParameters(rheo, timeMin, currDur);
			
			IzhikevichSolver solver = new IzhikevichSolver(this);
			//solver.setsS(1.0);
			SpikePatternAdapting modelSpikePattern = solver.getSpikePatternAdapting();			
			if(modelSpikePattern == null){				
				iMin = rheo + incStep;   
				continue;
				//return Float.MAX_VALUE;	 										
			}
			
			nSpikes = modelSpikePattern.getNoOfSpikes();
			
			/*if( nSpikes == 1){
				return rheo;
			}else{*/
				 if (nSpikes >= 1){                                             
		              iMax = rheo - incStep;   
		         } else {                                                        
		              iMin = rheo + incStep;   
		         }		       
			//}			
	     } 
	   if(GeneralUtils.isCloseEnough(holdIMax, iMax, 1.0)) {
		   return Float.MAX_VALUE;
	   }
	   return rheo;	         
	  }

	public double  getRheo_rb(float currDur, double i_max, double i_min, double incStep)
	   {
	     double rheo = Float.MAX_VALUE;
	     double holdImin = i_min;
	     int nSpikes = 0;
	     while(i_min <= i_max)    {	
	    	rheo = (i_min + i_max) / 2;	
			this.setInputParameters(rheo, 100, currDur);
			
			IzhikevichSolver solver = new IzhikevichSolver(this);
			//solver.setsS(1.0);
			SpikePatternAdapting modelSpikePattern = solver.getSpikePatternAdapting();			
			if(modelSpikePattern == null){				
				i_max = rheo - incStep;   
				continue;
				//return Float.MAX_VALUE;	 										
			}
			
			nSpikes = modelSpikePattern.getNoOfSpikes();
			
			/*if( nSpikes == 1){
				return rheo;
			}else{*/
				 if (nSpikes >= 1){          
					 i_min = rheo + incStep;  
		         } else {                                                        
		             i_max = rheo - incStep;   
		         }		       
			//}			
	     } 
	   if(GeneralUtils.isCloseEnough(holdImin, i_min, 1.0)) {
		   return -Float.MAX_VALUE;
	   }
	   return rheo;	         
	  }
	
	public double  getRampRheo(float currDur, double iMin, double iMax, double incStep)
	   {
		double rheo = Float.MAX_VALUE;
	     int nSpikes = 0;
	    for(double I=iMin; I<iMax; I+=incStep)   {	    		
			this.setInputParameters(I, timeMin, currDur);			
			IzhikevichSolver solver = new IzhikevichSolver(this);
			//solver.setsS(1.0);
			SpikePatternAdapting modelSpikePattern = solver.getSpikePatternAdapting();			
			if(modelSpikePattern == null){				
				continue;										
			}			
			nSpikes = modelSpikePattern.getNoOfSpikes();			
			 if (nSpikes >= 1){                                             
	             rheo = I;
	             break;
	         }		       
			//}			
	     } 	  
	   return rheo;	         
	  }
	/*
	 * for fast spikers check
	 */
	public float  get2SpikeRheo(float currDur, float iMin, float iMax, float incStep)
	   {
	     float rheo = Float.MAX_VALUE;
	     float holdIMax = iMax;
	     int nSpikes = 0;
	     while(iMin <= iMax)    {	
	    	rheo = (iMin + iMax) / 2;	
			this.setInputParameters(rheo, timeMin, currDur);
			
			IzhikevichSolver solver = new IzhikevichSolver(this);
			//solver.setsS(1.0);
			SpikePatternAdapting modelSpikePattern = solver.getSpikePatternAdapting();			
			if(modelSpikePattern == null){				
				iMin = rheo + incStep;   
				continue;
				//return Float.MAX_VALUE;	 										
			}
			
			nSpikes = modelSpikePattern.getNoOfSpikes();
			
			/*if( nSpikes == 1){
				return rheo;
			}else{*/
				 if (nSpikes >= 2){                                             
		              iMax = rheo - incStep;   
		         } else {                                                        
		              iMin = rheo + incStep;   
		         }		       
			//}			
	     } 
	   if(GeneralUtils.isCloseEnough(holdIMax, iMax, 1.0)) {
		   return Float.MAX_VALUE;
	   }
	   if(iMax >iMin) rheo = iMax;
	   else rheo = iMin;
	   return rheo;        
	  }
	
	public float getVDefAt(float I, float Idur, float vAT)
	   {
		
		this.setInputParameters(I, timeMin, Idur);			
		IzhikevichSolver solver = new IzhikevichSolver(this);
		//solver.setsS(1.0);
		SpikePatternAdapting modelSpikePattern = solver.getSpikePatternAdapting();			
		if(modelSpikePattern == null){					
			return Float.MAX_VALUE;	 										
		}		
		
		//double voltage_min = modelSpikePattern.getSpikePatternData().getMinVoltage(timeMin, timeMin+Idur, 1);
		//double voltage_max = modelSpikePattern.getSpikePatternData().getPeakVoltage(timeMin, timeMin+Idur, 1);
		
		double voltage_min = modelSpikePattern.getSpikePatternData().getMinVoltage(timeMin+Idur-50, timeMin+Idur-5, 1);
		double voltage_max = modelSpikePattern.getSpikePatternData().getPeakVoltage(timeMin+Idur-50, timeMin+Idur-5, 1);
		
		float def1 = (float) Math.abs(voltage_max - this.vR);
		float def2 = (float) Math.abs(this.vR - voltage_min);
		if(def1>def2) return def1;
		else return def2;		       
	  }

	public float getSSVDef(double I, double Idur, double timeStart, double timeEnd)
	   {
		this.setInputParameters(I, 0, Idur);			
		IzhikevichSolver solver = new IzhikevichSolver(this);
		//solver.setsS(1.0);
		SpikePatternAdapting modelSpikePattern = solver.getSpikePatternAdapting();			
		if(modelSpikePattern == null){					
			System.out.println("null pattern! model.getVDef" );	 										
		}		
		
		double voltage_min = modelSpikePattern.getSpikePatternData().getMinVoltage(timeStart, timeEnd, 1);
		
		float def2 = (float) Math.abs(this.vR - voltage_min);
		return def2;		       
	  }
	
	public double getTimeConstant(double I, double Idur, double timeStart, double timeEnd)
	   {
		this.setInputParameters(I, 0, Idur);			
		IzhikevichSolver solver = new IzhikevichSolver(this);
		//solver.setsS(1.0);
		SpikePatternAdapting modelSpikePattern = solver.getSpikePatternAdapting();			
		if(modelSpikePattern == null){					
			System.out.println("null pattern! model.getVDef" );	 										
		}		
		
		double voltage_min = modelSpikePattern.getSpikePatternData().getMinVoltage(timeStart, timeEnd, 0.1);
		
		float def2 = (float) Math.abs(this.vR - voltage_min);
		double percent=63.2/100d;
		double targetVolt = this.vR-def2*percent;
		
		return modelSpikePattern.getSpikePatternData().getTimeToReachHPI(targetVolt);
	  }
	
	public boolean isStepCurrent() {
		return isStepCurrent;
	}
	
	public void setiGenr(InputCurrentGenerator iGenr) {
		if(iGenr.getInputCurrentType().equals(InputCurrentType.STEP))
			this.isStepCurrent = true;
		else
			this.isStepCurrent = false;
		this.iGenr = iGenr;
	}
	public double getParm(ModelParameterID id){
		if(id.equals(ModelParameterID.K)) return getK();
		if(id.equals(ModelParameterID.A)) return getA();
		if(id.equals(ModelParameterID.B)) return getB();
		if(id.equals(ModelParameterID.D)) return getD();
		if(id.equals(ModelParameterID.CM)) return getcM();
		if(id.equals(ModelParameterID.VR)) return getvR();
		if(id.equals(ModelParameterID.VT)) return getvT();
		if(id.equals(ModelParameterID.VMIN)) return getC();
		if(id.equals(ModelParameterID.VPEAK)) return getvPeak();
		if(id.equals(ModelParameterID.I)) return (double) this.current;
		System.out.println("Not a valid model parameter id-- Izhikevich9pModel -- getParm()");
		System.exit(1);
		return Double.NaN;
	}

	public void setParm(ModelParameterID id, double newVal){
		if(id.equals(ModelParameterID.K)) setK(newVal);
		if(id.equals(ModelParameterID.A)) setA(newVal);
		if(id.equals(ModelParameterID.B)) setB(newVal);
		if(id.equals(ModelParameterID.D)) setD(newVal);
		if(id.equals(ModelParameterID.CM)) setcM(newVal);
		if(id.equals(ModelParameterID.VR)) setvR(newVal);
		if(id.equals(ModelParameterID.VT)) setvT(newVal);
		if(id.equals(ModelParameterID.VMIN)) setvMin(newVal);
		if(id.equals(ModelParameterID.VPEAK)) setvPeak(newVal);
		if(id.equals(ModelParameterID.I)) setCurrent(newVal);
		//System.out.println("Not a valid model parameter id-- Izhikevich9pModelMC -- setParm()");
		//System.exit(1);
	}
	
	public Izhikevich9pModel cloneModel(){
		Izhikevich9pModel model = new Izhikevich9pModel();
		model.setK(getK());
		model.setA(getA());
		model.setB(getB());
		model.setD(getD());	
		model.setcM(getcM());
		model.setvR(getvR());
		model.setvT(getvT());		
		model.setvMin(getC());	
        model.setvPeak(getvPeak());
        
		return model;
	}
	
}
