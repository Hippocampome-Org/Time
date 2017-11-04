package ec.app.izhikevich.inputprocess;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONArray;

import ec.app.izhikevich.inputprocess.labels.PatternFeatureID;
import ec.app.izhikevich.spike.BurstFeature;
import ec.app.izhikevich.spike.PatternFeature;
import ec.app.izhikevich.spike.PatternType;
import ec.app.izhikevich.spike.labels.SpikePatternClass;

public class InputSpikePatternConstraint {
	
	private PatternType type; //0: rheobase; 1: >rheobase; -1: <rheobase
	private SpikePatternClass spikePatternClass;
	private int compartment;
	private int index;
	private float patternWeight;
	//inputs
	//private double current;
	private double currentDuration;
	private double timeMin;
	
	/*
	 * new additions; 8/24/2014 -- for 3CA; not yet added to features
	 */
	private double[] dendMirrorVpeak;
	/*
	//Constraints
	private ConstraintFeature nSpikes;	
	private ConstraintFeature[] ISIs; // range for individual ISI's not allowed yet
	private ConstraintFeature avgISI;
	
	private ConstraintFeature fsl;
	private ConstraintFeature pss;
	
	//private ConstraintFeature sfa;
	private ConstraintFeature nSfaISIs; //this will be used as the indicator for SFA; i.e, with 'W'... 
	private ConstraintFeature sfaLinearM; // 'W' is ignored - cuz, error is distance taking into account both m & b
	private ConstraintFeature sfaLinearb; // 'W' is ignored
	private ConstraintFeature nonSfaAvgISI;
	
	private ConstraintFeature ssSubVoltage;	
	private ConstraintFeature vMinOffset;
	*/
	
	private HashMap<PatternFeatureID, PatternFeature> features;
	private HashSet<PatternFeatureID> featuresToEvaluate;
	private BurstFeature burstFeatures;
	private String neuronTypeName;
	
	public InputSpikePatternConstraint() {
		features = new HashMap<>();
		featuresToEvaluate = new HashSet<>();
	}
	/*public ExpSpikePatternData(int type, double current, double duration, 
			double timeMin, int nSpikes, int sfaSpikes, double[] ISIs,
			double fsl, double psi, double avgISI, double sfa,
			double type_1Voltage, double vMinOffset) {
		this.setType(type);
		this.setCurrent(current);
		this.setCurrentDuration(duration);
		this.setTimeMin(timeMin);
		this.setnSpikes(nSpikes);
		this.setSfaISIs(sfaSpikes);
		this.setISIs(ISIs);
		this.setFsl(fsl);
		this.setPsi(psi);
		this.setAvgISI(avgISI);
		this.setSfa(sfa);
		this.setType_1Voltage(type_1Voltage);
		this.setvMinOffset(vMinOffset);
	}
	public ExpSpikePatternData getCloneWithNewFsl(double new_fsl) {
		return new ExpSpikePatternData(type, current, currentDuration, timeMin, nSpikes, sfaISIs, ISIs, new_fsl, pss, avgISI, sfa, ssSubVoltage, vMinOffset);
	}*/
	public PatternFeature getCurrent() {
		return features.get(PatternFeatureID.current);
	}
	public void setCurrent(PatternFeature current) {
		this.features.put(PatternFeatureID.current, current);
	}
	public PatternFeature getValidMaxV() {
		return features.get(PatternFeatureID.valid_max_v);
	}
	public void setValidMaxV(PatternFeature validMaxV) {
		this.features.put(PatternFeatureID.valid_max_v, validMaxV);
	}
	public double getCurrentDuration() {
		return currentDuration;
	}
	public void setCurrentDuration(double currentDuration) {
		this.currentDuration = currentDuration;
	}
	public double getTimeMin() {
		return timeMin;
	}
	public void setTimeMin(double timeMin) {
		this.timeMin = timeMin;
	}
	public PatternFeature getnSpikes() {
		return features.get(PatternFeatureID.n_spikes);
	}
	public void setnSpikes(PatternFeature nSpikes) {
		this.features.put(PatternFeatureID.n_spikes, nSpikes);
	}
	public PatternFeature getnBursts() {
		return features.get(PatternFeatureID.nbursts);
	}
	public void setnBursts(PatternFeature nBursts) {
		this.features.put(PatternFeatureID.nbursts, nBursts);
	}
	public PatternFeature getPss() {
		return features.get(PatternFeatureID.pss);
	}
	public void setPss(PatternFeature pss) {
		features.put(PatternFeatureID.pss,pss);
	}
	public PatternFeature getFsl() {
		return features.get(PatternFeatureID.fsl);
	}
	public void setFsl(PatternFeature fsl) {
		features.put(PatternFeatureID.fsl, fsl);
	}
	public PatternFeature getReboundVmax() {
		return features.get(PatternFeatureID.rebound_VMax);
	}
	public void setReboundVmax(PatternFeature rboundVmax) {
		features.put(PatternFeatureID.rebound_VMax, rboundVmax);
	}
	public void setSwa(PatternFeature swa) {
		features.put(PatternFeatureID.swa, swa);
	}
	public PatternFeature getSwa() {
		return features.get(PatternFeatureID.swa);
	}
	/*
	public double[] calculateSpikeTimesFromISIs(){		
		double[] spikeTimes = null;
		if(ISIs!=null) {
			spikeTimes = new double[ISIs.length+1];
			spikeTimes[0] = timeMin  + fsl.getValue();
			for(int i=0;i<ISIs.length;i++) {
				spikeTimes[i+1] = spikeTimes[i] + ISIs[i].getValue();
			}
		}
		return spikeTimes;
	}*/
	public PatternFeature getNSfaISIs0() {
		return features.get(PatternFeatureID.n_sfa_isis0);
	}
	public PatternFeature getNSfaISIs1() {
		return features.get(PatternFeatureID.n_sfa_isis1);
	}
	public PatternFeature getNSfaISIs2() {
		return features.get(PatternFeatureID.n_sfa_isis2);
	}
	public void setNSfaISIs0(PatternFeature sfaSpikes) {
		features.put(PatternFeatureID.n_sfa_isis0, sfaSpikes);
	}
	public void setNSfaISIs1(PatternFeature sfaSpikes) {
		features.put(PatternFeatureID.n_sfa_isis1, sfaSpikes);
	}
	public void setNSfaISIs2(PatternFeature sfaSpikes) {
		features.put(PatternFeatureID.n_sfa_isis2, sfaSpikes);
	}
	public PatternFeature getAvgISI() {
		return features.get(PatternFeatureID.avg_isi);
	}
	public void setAvgISI(PatternFeature avgISI) {
		features.put(PatternFeatureID.avg_isi, avgISI);
	}
	
	public PatternType getType() {
		return type;
	}
	public void setType(String type) {
		this.type = PatternType.valueOf(type);
	}
	public PatternFeature getSsSubVoltage() {
		return features.get(PatternFeatureID.sub_ss_voltage);
	}
	public void setSsSubVoltage(PatternFeature ssSubVoltage) {
		features.put(PatternFeatureID.sub_ss_voltage, ssSubVoltage);
	}
	public PatternFeature getSsSubVoltageSd() {
		return features.get(PatternFeatureID.sub_ss_voltage_sd);
	}
	public void setSsSubVoltageSd(PatternFeature ssSubVoltage_sd) {
		features.put(PatternFeatureID.sub_ss_voltage_sd, ssSubVoltage_sd);
	}
	public PatternFeature getvMinOffset() {
		return features.get(PatternFeatureID.vmin_offset);
	}
	public void setvMinOffset(PatternFeature vMinOffset) {
		features.put(PatternFeatureID.vmin_offset, vMinOffset);
	}
	public PatternFeature getTimeConst() {
		return features.get(PatternFeatureID.time_const);
	}
	public void setTimeConst(PatternFeature timeConst) {
		features.put(PatternFeatureID.time_const, timeConst);
	}
	
	public PatternFeature getPeriod(){
		return features.get(PatternFeatureID.period);
	}
	public void setPeriod(PatternFeature period){
		features.put(PatternFeatureID.period, period );
	}
	/*public void displayAll() {
		String displayString = "type:"+ type +
		"\tI:"+ current+
		"\tDur:"+ currentDuration+
		"\ttimeMin:"+ timeMin+
		"\tnSpikes:"+ nSpikes+
		"\tsfaISIs:"+ nSfaISIs+
		"\tISIs:"+ GeneralUtils.flattenArray(PatternFeature.convertToDoubleArray(ISIs))+
		"\tavgISI:"+ avgISI+
		"\tfsl:"+ fsl+
		"\tpsi:"+ pss+
	//	"\tsfa:"+ sfa+
		"\ttype_1Voltage:"+ ssSubVoltage+
		"\tvMinOffset:"+ vMinOffset;
		
		System.out.println(displayString);
	}*/
	public PatternFeature getSfaLinearM0() {
		return features.get(PatternFeatureID.sfa_linear_m0);
	}
	public PatternFeature getSfaLinearM1() {
		return features.get(PatternFeatureID.sfa_linear_m1);
	}
	public void setSfaLinearM0(PatternFeature sfaLinearM) {
		features.put(PatternFeatureID.sfa_linear_m0, sfaLinearM);
	}
	public void setSfaLinearM1(PatternFeature sfaLinearM) {
		features.put(PatternFeatureID.sfa_linear_m1, sfaLinearM);
	}
	public PatternFeature getSfaLinearb0() {
		return features.get(PatternFeatureID.sfa_linear_b0);
	}
	public PatternFeature getSfaLinearb1() {
		return features.get(PatternFeatureID.sfa_linear_b1);
	}
	public void setSfaLinearb0(PatternFeature sfaLinearb) {
		this.features.put(PatternFeatureID.sfa_linear_b0, sfaLinearb);
	}
	public void setSfaLinearb1(PatternFeature sfaLinearb) {
		this.features.put(PatternFeatureID.sfa_linear_b1, sfaLinearb);
	}
	public PatternFeature getSfaLinearM2() {
		return features.get(PatternFeatureID.sfa_linear_m2);
	}
	public void setSfaLinearM2(PatternFeature sfaLinearM) {
		features.put(PatternFeatureID.sfa_linear_m2, sfaLinearM);
	}
	public PatternFeature getSfaLinearb2() {
		return features.get(PatternFeatureID.sfa_linear_b2);
	}
	public void setSfaLinearb2(PatternFeature sfaLinearb) {
		this.features.put(PatternFeatureID.sfa_linear_b2, sfaLinearb);
	}
	public PatternFeature getNonSfaAvgISI() {
		return features.get(PatternFeatureID.non_sfa_avg_isi);
	}
	public void setNonSfaAvgISI(PatternFeature nonSfaAvgISI) {
		features.put(PatternFeatureID.non_sfa_avg_isi, nonSfaAvgISI);
	}	
	public Set<PatternFeatureID> getFeaturesToEvaluate() {
		return featuresToEvaluate;
	}
	
	public void addFeatureToEvaluate(PatternFeatureID featureID) {
		this.featuresToEvaluate.add(featureID);
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public float getPatternWeight() {
		return patternWeight;
	}
	public void setPatternWeight(float patternWeight) {
		this.patternWeight = patternWeight;
	}
	public double[] getDendMirrorVpeak() {
		return dendMirrorVpeak;
	}
	public void setDendMirrorVpeak(double[] dendMirrorVpeak) {
		this.dendMirrorVpeak = dendMirrorVpeak;
	}
	public int getCompartment() {
		return compartment;
	}
	public void setCompartment(int compartment) {
		this.compartment = compartment;
	}
	public BurstFeature getBurstFeatures() {
		return burstFeatures;
	}
	public void setBurstFeatures(BurstFeature burstFeatures) {
		this.burstFeatures = burstFeatures;
	}
	public SpikePatternClass getSpikePatternClass() {
		return spikePatternClass;
	}
	public void setSpikePatternClass(SpikePatternClass spikePatternClass) {
		this.spikePatternClass = spikePatternClass;		
	}
	public HashMap<PatternFeatureID, PatternFeature> getFeatures(){
		return this.features;
	}
	public static InputSpikePatternConstraint constructInputSpikeConstraintObject(int idx, String neuronName, SpikePatternClass patternClass,
			double current, double current_duration,
			double fsl, double pss, double[] sfa){
		InputSpikePatternConstraint spikeConstraint = new InputSpikePatternConstraint();
		spikeConstraint.setIndex(idx);
		spikeConstraint.setNeuronTypeName(neuronName);
		spikeConstraint.setSpikePatternClass(patternClass);		
		spikeConstraint.setCurrentDuration(current_duration);
		spikeConstraint.setCurrent(new PatternFeature((int)current-10, (int)current+10, -1));
		spikeConstraint.setFsl(new PatternFeature(fsl, 0));
		spikeConstraint.setPss(new PatternFeature(pss, 0));
		spikeConstraint.setSfaLinearM1(new PatternFeature(sfa[0], 0));
		spikeConstraint.setSfaLinearb1(new PatternFeature(sfa[1], 0));
		spikeConstraint.setNSfaISIs1(new PatternFeature(sfa[2], 0));
		spikeConstraint.setSfaLinearM2(new PatternFeature(sfa[3], 0));
		spikeConstraint.setSfaLinearb2(new PatternFeature(sfa[4], 0));
		spikeConstraint.setNSfaISIs2(new PatternFeature(sfa[5], 0));
		
		spikeConstraint.setCompartment(0);
		//spikeConstraint.setnSpikes(nSpikes);
		spikeConstraint.setPatternWeight(1.0f);
		spikeConstraint.setTimeMin(0);
		spikeConstraint.setValidMaxV(new PatternFeature(200, -1));
		spikeConstraint.setType("SPIKES");
		
		spikeConstraint.addFeatureToEvaluate(PatternFeatureID.fsl);
		spikeConstraint.addFeatureToEvaluate(PatternFeatureID.pss);
		spikeConstraint.addFeatureToEvaluate(PatternFeatureID.n_sfa_isis1);
		
		return spikeConstraint;
	}
	public JSONArray getFeatsToEvaluate(){
		JSONArray evalArray = new JSONArray();	
		for (PatternFeatureID id : featuresToEvaluate) {
			 evalArray.put(id.name());
		}
		return evalArray;
	}
	public String getNeuronTypeName() {
		return neuronTypeName;
	}
	public void setNeuronTypeName(String neuronTypeName) {
		this.neuronTypeName = neuronTypeName;
	}
	
}
