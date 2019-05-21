package ec.app.izhikevich.model.neurontypes.sc;

/*
 * should match with ECJ input parms file genome definitions
 */
public class GAGenesCA3LM implements GAGenes{
	
	private float[] genes;
	public GAGenesCA3LM(float[] parms) {
		this.genes = parms;
	}
	
	public float getK() { return genes[K];}
	public float getA() { return genes[A];}
	public float getB() { return genes[B];}
	public float getD() { return genes[D];}
	
	public float getCm() { return genes[Cm];}
	public float getVr() { return genes[Vr];}
	
	public float getVt() { 
		return getVmin() + genes[Vt];
		}
	
	public float getVmin() { 
		return getVr() + genes[Vmin];
		}
	/*
	public float getI1() { return genes[I1]; }
	public float getI2() { return genes[I2]; }
	public float getI3() { return genes[I3]; }
	*/
	public float getVpeak() {
		return getVr()+78f;
		}
	
}
