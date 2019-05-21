package ec.app.izhikevich.outputprocess;

import ec.app.izhikevich.util.GeneralUtils;

public class CarlMcSimData {
	private double[] rampRheos;
	private double[] vDefs;
	private float[] spikePropRates;
	private double[] epsps;
	private double[][] epspsAll;
	
	public CarlMcSimData(double[] _rampRheos, double[] _vDefs, float[] _spikePropRates, double[] _epsps){
		rampRheos = _rampRheos;
		vDefs = _vDefs;
		spikePropRates = _spikePropRates;
		epsps = _epsps;
	}
	
	public CarlMcSimData(double[] _rampRheos, double[] _vDefs, float[] _spikePropRates, double[] _epsps, double[][] _epspsAll){
		rampRheos = _rampRheos;
		vDefs = _vDefs;
		spikePropRates = _spikePropRates;
		epsps = _epsps;
		epspsAll = _epspsAll;
	}
	
	public double[] getRampRheos(){
		return rampRheos;
	}
	public double[] getVdefs(){
		return vDefs;
	}
	public float[] getSpikePropRates(){
		return spikePropRates;
	}
	public double[] getEpsps(){
		return epsps;
	}
	public double[][] getEpspsAll(){
		return epspsAll;
	}
	public void display(){
		System.out.print("Ramp Rheos.\t"); GeneralUtils.displayArray(rampRheos);
		System.out.print("V Defs.\t"); GeneralUtils.displayArray(vDefs);
		System.out.print("spike Proped? .\t"); GeneralUtils.displayArray(spikePropRates);
		System.out.print("soma Epsps.\t"); GeneralUtils.displayArray(epsps);
	}
	
}
