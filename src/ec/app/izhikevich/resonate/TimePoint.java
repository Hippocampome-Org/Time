package ec.app.izhikevich.resonate;

public class TimePoint {

	double t;
	double v;
	double u;
	
	public TimePoint(double T, double V, double U){
		this.t = T;
		this.v = V;
		this.u = U;
	}
	public void display(){
		System.out.println(this.t +"\t"+this.v);
	}
}
