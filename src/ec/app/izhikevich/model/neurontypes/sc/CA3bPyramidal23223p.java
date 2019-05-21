package ec.app.izhikevich.model.neurontypes.sc;

/*
 * should match with ECJ input parms file genome definitions
 */
public class CA3bPyramidal23223p implements GAGenes{
	public static final String INPUT = "input/CA3bPyramidal23223p.input";
	public static final int START_IDX_FOR_CONST = 8;
	/*
	 * 1. Vmin is relative to Vrest
	 * 2. Vthreshold is 20 mv above Vrest
	 * 3. Vpeak is 60 mv above Vrest
	 */
	public static final int I1 = 8;
	public static final int I2 = 9;
	public static final int I3 = 10;
	
	private float[] genes;
	public CA3bPyramidal23223p(float[] parms) {
		this.genes = parms;
	}
	
	public float getK() { return genes[K];}
	public float getA() { return genes[A];}
	public float getB() { return genes[B];}
	public float getD() { return genes[D];}
	
	public float getCm() { return genes[Cm];}
	public float getVr() { return genes[Vr];}
	public float getVmin() { return getVr()+genes[Vmin];
	//return getVr()+genes[6];
	}
	
//	public float getRheoCurrent() { return genes[CURRENT2];}
	
	public float getVt() { 
		return getVmin()+genes[Vt];
		//return getVr()+20;
		}
	public float getVpeak() {return 2;}//getVr() + 107;}
		
	public float getI1() {
		return genes[I1];
	}
	public float getI2() {
		return genes[I2];
	}
	public float getI3() {
		return genes[I3];
	}
	public float getG() {
		return genes[I2+1];
	}
}
