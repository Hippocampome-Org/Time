package ec.app.izhikevich.starter;

public class GAParameter {

	int popSize;
	int tourSize;
	
	String xOverType;
	float xOverProb;
	
	float mutSD;
	float mutProb;
	
	public GAParameter(int pop, int tour, String xover, float xprob, float mutsd, float mutprob) {
		this.popSize = pop;
		this.tourSize = tour;
		this.xOverType = xover;
		this.xOverProb = xprob;
		this.mutProb = mutprob;
		this.mutSD = mutsd;
	}
	public String[] getParms() {
		String[] parms = new String[12];
		parms[0] = parms[2] = parms[4] = parms[6] = parms[8] = parms[10] = "-p";
	
		parms[1] = "pop.subpop.0.size="+popSize;
		parms[3] = "select.tournament.size="+tourSize;
		parms[5] = "pop.subpop.0.species.crossover-type="+xOverType;
		parms[7] = "base.likelihood="+ Float.toString(xOverProb);
		parms[9] = "pop.subpop.0.species.mutation-stdev="+Float.toString(mutSD);
		parms[11] = "pop.subpop.0.species.mutation-prob="+Float.toString(mutProb);
		
		return parms;
	}
	
	public String getFlatParms() {		
		return popSize+"\t"+tourSize+"\t"+xOverType+"\t"+Float.toString(xOverProb)+"\t"+Float.toString(mutSD)+"\t"+Float.toString(mutProb);
	}
}
