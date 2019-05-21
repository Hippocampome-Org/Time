package ec.app.izhikevich.model.neurontypes.sc;

public interface GAGenes {
	/*
	 * 1. Vmin is relative to Vrest
	
	 */
	public static final int K = 0;
	public static final int A = 1;
	public static final int B = 2;
	public static final int D = 3;
	
	public static final int Cm = 4;
	public static final int Vr = 5;	
	public static final int Vt = 6;
	public static final int Vmin = 7;
//	public static final int Vpeak = 8;
	
	
	public float getK();
	public float getA();
	public float getB();
	public float getD();
	
	public float getCm();
	public float getVr();
	public float getVt();
	public float getVmin();
	public float getVpeak();
	
	//public float getI();
}
