package ec.app.izhikevich.evaluator.qualifier;

import ec.app.izhikevich.util.GeneralUtils;


public class SolverResultsStat {
	
	private double m1;
	private double c1;
	
	private double m2;
	private double c2;

	private double[] fitResidualsAbs;
	private int breakPoint; //for 3 and 4 parms
	
	private double rms;
	private double evaluations;
	private double iterations;
	
	private long timeTaken;
	
	public SolverResultsStat(double c, double[] fitResAbs){		
		c1 = c;
		this.setFitResidualsAbs(fitResAbs);
		m1=0;m2=0;c2=0;
		if(fitResAbs!=null)
			breakPoint = fitResAbs.length-1;
		else
			breakPoint = -1;
	}
	
	public SolverResultsStat(double m, double c, double[] fitResAbs){
		m1 = m;
		c1 = c;
		this.setFitResidualsAbs(fitResAbs);
		m2=0;c2=0;
		if(fitResAbs!=null)
			breakPoint = fitResAbs.length-1;
		else
			breakPoint = -1;
	}
	
	public SolverResultsStat(double m1, double c1, double c2, double[] fitResAbs, int break_point){
		this.m1 = m1;
		this.c1 = c1;
		this.c2 = c2;
		breakPoint = break_point;
		this.setFitResidualsAbs(fitResAbs);
		m2=0;
	}
	public SolverResultsStat(double m1, double c1, double m2, double c2, double[] fitResAbs, int break_point){
		this.m1 = m1;
		this.c1 = c1;
		this.m2 = m2;
		this.c2 = c2;
		breakPoint = break_point;		
		this.setFitResidualsAbs(fitResAbs);
	}
	
	public void display(){
		System.out.println("m1: "+ GeneralUtils.formatThreeDecimal(m1)+"\t"+
				"c1: "+GeneralUtils.formatThreeDecimal(c1)+"\t"+
				"m2: "+GeneralUtils.formatThreeDecimal(m2)+"\t"+
				"c2: "+GeneralUtils.formatThreeDecimal(c2)+"\t"+
				"bP: "+breakPoint);
		System.out.println("rms: "+rms+"\tevaluations: "+evaluations+"\titerations: "+iterations+"\ttimeTaken: "+timeTaken);
		//GeneralUtils.displayArrayVertical(fitResidualsAbs);
	}

	public void setSolverStats(double rms, double evals, double iters){
		this.rms = rms;
		this.evaluations = evals;
		this.iterations = iters;
	}
	public double getRms(){
		return rms;
	}
	public double getEvals(){
		return evaluations;
	}
	public double getIterations(){
		return iterations;
	}
	public void setTimeTaken(long timeTaken) {
		this.timeTaken = timeTaken;
	}
	public double getM1(){
		return m1;
	}
	public double getC1(){
		return c1;
	}
	public double getM2(){
		return m2;
	}
	public double getC2(){
		return c2;
	}

	public double[] getFitResidualsAbs() {
		return fitResidualsAbs;
	}

	public void setFitResidualsAbs(double[] fitResidualsAbs) {
		this.fitResidualsAbs = fitResidualsAbs;
	}

	public int getBreakPoint() {
		return breakPoint;
	}

}
