package ec.app.izhikevich.model.neurontypes.sc;

/*
 * should match with ECJ input parms file genome definitions
 */
public class GAGenesDGBasket implements GAGenes{
	
	
	private float[] genes;
	public GAGenesDGBasket(float[] parms) {
		this.genes = parms;
	}
	
	public float getK() { return genes[K];}
	public float getA() { return genes[A];}
	public float getB() { return genes[B];}
	public float getD() { return genes[D];}
	
	public float getCm() { return genes[Cm];}
	public float getVr() { return genes[Vr];}
	
	public float getVt() { 
		return getVmin()+genes[Vt];
		}
	
	public float getVmin() { 
		return getVr()+ genes[Vmin];
		}
	
	public float getVpeak() {
		return getVr()+86.29f;
		}
	
}
