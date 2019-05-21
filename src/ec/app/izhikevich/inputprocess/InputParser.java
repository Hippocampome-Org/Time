
package ec.app.izhikevich.inputprocess;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ec.app.izhikevich.inputprocess.labels.BurstFeatureID;
import ec.app.izhikevich.inputprocess.labels.MCConstraintAttributeID;
import ec.app.izhikevich.inputprocess.labels.MCConstraintType;
import ec.app.izhikevich.inputprocess.labels.ModelParameterID;
import ec.app.izhikevich.inputprocess.labels.PatternFeatureID;
import ec.app.izhikevich.inputprocess.labels.PhenotypeConstraintAttributeID;
import ec.app.izhikevich.inputprocess.labels.PhenotypeConstraintType;
import ec.app.izhikevich.spike.BurstFeature;
import ec.app.izhikevich.spike.PatternFeature;
import ec.app.izhikevich.spike.labels.SpikePatternClass;

public class InputParser {

	private static JSONObject readASPISIs (String fileName) {
		JSONObject jsonObj = null; 
		try {
			String content = new Scanner(new File(fileName)).useDelimiter("\\Z").next();
			jsonObj = new JSONObject(content);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonObj;
	}
	
	/*public static PatternFeatureID[] getFeaturesToEvaluate(String fileName) {	
		PatternFeatureID[] featuresToEvaluate = null;
		JSONObject jsonObj = readASPISIs(fileName);
		JSONArray jsonArray = jsonObj.getJSONArray("features_to_evaluate");		
		if(jsonArray.length() > 0) {
			featuresToEvaluate = new PatternFeatureID[jsonArray.length()];
			
			for(int idx = 0; idx<featuresToEvaluate.length; idx++) {				
				featuresToEvaluate[idx] = PatternFeatureID.valueOf(jsonArray.getString(idx));			
			}
		}
		return featuresToEvaluate;
	}*/
	
	public static InputSpikePatternConstraint[] getExpSpikePatternData(String fileName) {	
		InputSpikePatternConstraint[] expSpikeData = null;
		JSONObject jsonObj = readASPISIs(fileName);
		JSONArray jsonArray = jsonObj.getJSONArray("spike_pattern_constraints");
		
		if(jsonArray.length() > 0) {
			int includedLength = 0;
			for(int idx = 0; idx<jsonArray.length(); idx++) {
				JSONObject spikePattern = jsonArray.getJSONObject(idx);
				if(spikePattern.getBoolean(PatternFeatureID.INCLUDE.name())){
					includedLength++;
				}
			}
			expSpikeData = new InputSpikePatternConstraint[includedLength];
			int idx = 0;
			for(int ij = 0; ij<jsonArray.length(); ij++) {
				JSONObject spikePattern = jsonArray.getJSONObject(ij);
				if(!spikePattern.getBoolean(PatternFeatureID.INCLUDE.name())){
					continue;
				}
				expSpikeData[idx] = new InputSpikePatternConstraint();
				
				/*
				 * Five essential inputs
				 */				
				expSpikeData[idx].setIndex(spikePattern.getJSONArray(PatternFeatureID.IDX_NOTE.name()).getInt(0));
				expSpikeData[idx].setType(spikePattern.getString(PatternFeatureID.type.name()));
				expSpikeData[idx].setCompartment(spikePattern.getInt((PatternFeatureID.compartment.name())));
				expSpikeData[idx].setTimeMin(spikePattern.getDouble(PatternFeatureID.time_min.name()));
				expSpikeData[idx].setCurrentDuration(spikePattern.getDouble(PatternFeatureID.current_duration.name()));
				expSpikeData[idx].setPatternWeight((float)spikePattern.getDouble(PatternFeatureID.pattern_weight.name()));
				//PAttern class is also essential going forward!
				String pattern_class = spikePattern.getString(PatternFeatureID.pattern_class.name());	
				SpikePatternClass spikePatternClass = new SpikePatternClass(pattern_class, ".");
				expSpikeData[idx].setSpikePatternClass(spikePatternClass);
				/*
				 * some cases, don't have current input: e.g. Hemmond 2008 CA3bP burst cells
				 */
				
				if(spikePattern.has(PatternFeatureID.current.name())) {		
					PatternFeature current = buildPatternFeature(spikePattern.getJSONObject(PatternFeatureID.current.name()),
							PatternFeatureID.current);
					expSpikeData[idx].setCurrent(current);
				}
				
				if(spikePattern.has(PatternFeatureID.valid_max_v.name())) {		
					PatternFeature validMaxV = buildPatternFeature(spikePattern.getJSONObject(PatternFeatureID.valid_max_v.name()),
							PatternFeatureID.valid_max_v);
					expSpikeData[idx].setValidMaxV(validMaxV);
				}

				if(spikePattern.has("dend_mirror_vpeaks")) {		
					JSONArray vpeaks = spikePattern.getJSONArray("dend_mirror_vpeaks");
					double[] vPeaks = new double[vpeaks.length()];
					for(int i=0;i<vpeaks.length();i++){
						vPeaks[i] = vpeaks.getDouble(i);
					}
					expSpikeData[idx].setDendMirrorVpeak(vPeaks);
				}
				
				/*
				 * Another essential input: eval features
				 */
				if(spikePattern.has(PatternFeatureID.eval.name())) {	
					JSONArray jsonEvalFeats = spikePattern.getJSONArray(PatternFeatureID.eval.name());
					for(int i=0;i<jsonEvalFeats.length();i++){
						String featID = jsonEvalFeats.getString(i);						
						expSpikeData[idx].addFeatureToEvaluate(PatternFeatureID.valueOf(featID));
					}
				}
				/*
				 * Other optional (loosely 'Optional'); Following are mostly the Constraints
				 */
			/*	PatternFeature[] isis = null;
				if(spikePattern.has(PatternFeatureID.isis.name())) {
					JSONObject isiswithW = spikePattern.getJSONObject(PatternFeatureID.isis.name());
					JSONArray json_isis = isiswithW.getJSONArray("V"); //no ranges here, yet, for ISIs
					isis = new PatternFeature[json_isis.length()];
					for(int i=0;i<isis.length; i++) {
						isis[i] = new PatternFeature(PatternFeatureID.isis, json_isis.getDouble(i), isiswithW.getDouble("W"));
					}
				}*/
								
				if(spikePattern.has(PatternFeatureID.n_spikes.name())) {
					PatternFeature nSpikes = buildPatternFeature(spikePattern.getJSONObject(PatternFeatureID.n_spikes.name()),
							PatternFeatureID.n_spikes);
					expSpikeData[idx].setnSpikes(nSpikes);
				}
				
				if(spikePattern.has(PatternFeatureID.n_sfa_isis0.name())) {
					PatternFeature sfa_isis = buildPatternFeature(spikePattern.getJSONObject(PatternFeatureID.n_sfa_isis0.name()),
							PatternFeatureID.n_sfa_isis0);
					expSpikeData[idx].setNSfaISIs0(sfa_isis);
				}
								
				if(spikePattern.has(PatternFeatureID.n_sfa_isis1.name())) {
					PatternFeature sfa_isis = buildPatternFeature(spikePattern.getJSONObject(PatternFeatureID.n_sfa_isis1.name()),
							PatternFeatureID.n_sfa_isis1);
					expSpikeData[idx].setNSfaISIs1(sfa_isis);
				}
				
				if(spikePattern.has(PatternFeatureID.n_sfa_isis2.name())) {
					PatternFeature sfa_isis = buildPatternFeature(spikePattern.getJSONObject(PatternFeatureID.n_sfa_isis2.name()),
							PatternFeatureID.n_sfa_isis2);
					expSpikeData[idx].setNSfaISIs2(sfa_isis);
				}
				
				if(spikePattern.has(PatternFeatureID.fsl.name())) {
					PatternFeature fsl = buildPatternFeature(spikePattern.getJSONObject(PatternFeatureID.fsl.name()),
							PatternFeatureID.fsl);
					expSpikeData[idx].setFsl(fsl);
				}
				
				
				if(spikePattern.has(PatternFeatureID.pss.name())) {
					PatternFeature pss = buildPatternFeature(spikePattern.getJSONObject(PatternFeatureID.pss.name()),
							PatternFeatureID.pss);
					expSpikeData[idx].setPss(pss);
				}
				
				
				if(spikePattern.has(PatternFeatureID.avg_isi.name())) {
					PatternFeature avg_isi = buildPatternFeature(spikePattern.getJSONObject(PatternFeatureID.avg_isi.name()),
							PatternFeatureID.avg_isi);
					expSpikeData[idx].setAvgISI(avg_isi);
				}
				
				if(spikePattern.has(PatternFeatureID.sfa_linear_b0.name())) {
					PatternFeature sfa_linear_b = buildPatternFeature(spikePattern.getJSONObject(PatternFeatureID.sfa_linear_b0.name()),
							PatternFeatureID.sfa_linear_b0);
					expSpikeData[idx].setSfaLinearb0(sfa_linear_b);
				}
				if(spikePattern.has(PatternFeatureID.sfa_linear_b1.name())) {
					PatternFeature sfa_linear_b = buildPatternFeature(spikePattern.getJSONObject(PatternFeatureID.sfa_linear_b1.name()),
							PatternFeatureID.sfa_linear_b1);
					expSpikeData[idx].setSfaLinearb1(sfa_linear_b);
				}
				if(spikePattern.has(PatternFeatureID.sfa_linear_b2.name())) {
					PatternFeature sfa_linear_b = buildPatternFeature(spikePattern.getJSONObject(PatternFeatureID.sfa_linear_b2.name()),
							PatternFeatureID.sfa_linear_b2);
					expSpikeData[idx].setSfaLinearb2(sfa_linear_b);
				}
				
				if(spikePattern.has(PatternFeatureID.sfa_linear_m0.name())) {
					PatternFeature sfaLinearM = buildPatternFeature(spikePattern.getJSONObject(PatternFeatureID.sfa_linear_m0.name()),
							PatternFeatureID.sfa_linear_m0);
					expSpikeData[idx].setSfaLinearM0(sfaLinearM);
				}
				if(spikePattern.has(PatternFeatureID.sfa_linear_m1.name())) {
					PatternFeature sfaLinearM = buildPatternFeature(spikePattern.getJSONObject(PatternFeatureID.sfa_linear_m1.name()),
							PatternFeatureID.sfa_linear_m1);
					expSpikeData[idx].setSfaLinearM1(sfaLinearM);
				}				
				if(spikePattern.has(PatternFeatureID.sfa_linear_m2.name())) {
					PatternFeature sfaLinearM = buildPatternFeature(spikePattern.getJSONObject(PatternFeatureID.sfa_linear_m2.name()),
							PatternFeatureID.sfa_linear_m2);
					expSpikeData[idx].setSfaLinearM2(sfaLinearM);
				}	
				
				if(spikePattern.has(PatternFeatureID.non_sfa_avg_isi.name())) {
					PatternFeature nonSfaAvgISI = buildPatternFeature( spikePattern.getJSONObject(PatternFeatureID.non_sfa_avg_isi.name()),
							PatternFeatureID.non_sfa_avg_isi);
					expSpikeData[idx].setNonSfaAvgISI(nonSfaAvgISI);
				}
				
				if(spikePattern.has(PatternFeatureID.sub_ss_voltage.name())) {
					PatternFeature ss_sub_voltage = buildPatternFeature( spikePattern.getJSONObject(PatternFeatureID.sub_ss_voltage.name()),
							PatternFeatureID.sub_ss_voltage);
					expSpikeData[idx].setSsSubVoltage(ss_sub_voltage);
				}
				
				if(spikePattern.has(PatternFeatureID.sub_ss_voltage_sd.name())) {
					PatternFeature ss_sub_voltage_sd = buildPatternFeature( spikePattern.getJSONObject(PatternFeatureID.sub_ss_voltage_sd.name()),
							PatternFeatureID.sub_ss_voltage_sd);
					expSpikeData[idx].setSsSubVoltageSd(ss_sub_voltage_sd);
				}
				
				if(spikePattern.has(PatternFeatureID.vmin_offset.name())) {
					PatternFeature vMinOffset = buildPatternFeature(spikePattern.getJSONObject(PatternFeatureID.vmin_offset.name()),
							PatternFeatureID.vmin_offset);
					expSpikeData[idx].setvMinOffset(vMinOffset);
				}	
				
				if(spikePattern.has(PatternFeatureID.time_const.name())) {
					PatternFeature timeConst = buildPatternFeature(spikePattern.getJSONObject(PatternFeatureID.time_const.name()),
							PatternFeatureID.time_const);
					expSpikeData[idx].setTimeConst(timeConst);
				}	
				
				if(spikePattern.has(PatternFeatureID.nbursts.name())) {
					PatternFeature nBursts = buildPatternFeature(spikePattern.getJSONObject(PatternFeatureID.nbursts.name()),
							PatternFeatureID.nbursts);
					expSpikeData[idx].setnBursts(nBursts);
				}
				
				if(spikePattern.has(PatternFeatureID.bursts.name())) {
					BurstFeature burstFeatures = buildBurstFeatures(spikePattern.getJSONObject(PatternFeatureID.bursts.name()));
					expSpikeData[idx].setBurstFeatures(burstFeatures);
				}
				/*
				 * stuts distinguished just to add vmin validity error constraint in spikepatternevaluator class
				 */
				if(spikePattern.has(PatternFeatureID.stuts.name())) {
					BurstFeature burstFeatures = buildBurstFeatures(spikePattern.getJSONObject(PatternFeatureID.stuts.name()));
					expSpikeData[idx].setBurstFeatures(burstFeatures);
				}
				
				if(spikePattern.has(PatternFeatureID.rebound_VMax.name())) {
					PatternFeature rBound = buildPatternFeature(spikePattern.getJSONObject(PatternFeatureID.rebound_VMax.name()),
							PatternFeatureID.rebound_VMax);
					expSpikeData[idx].setReboundVmax(rBound);
				}	
				
				if(spikePattern.has(PatternFeatureID.swa.name())) {
					PatternFeature swa = buildPatternFeature(spikePattern.getJSONObject(PatternFeatureID.swa.name()),
							PatternFeatureID.swa);
					expSpikeData[idx].setSwa(swa);
				}	
				
				if(spikePattern.has(PatternFeatureID.period.name())) {
					PatternFeature period = buildPatternFeature(spikePattern.getJSONObject(PatternFeatureID.period.name()),
							PatternFeatureID.period);
					expSpikeData[idx].setPeriod(period);
				}
				
				idx++;
			}
		}
		return expSpikeData;
	}
	
	private static BurstFeature buildBurstFeatures(JSONObject object) {
		BurstFeature burstFeature = null;	
		if(object!=null) {
			ArrayList<HashMap<BurstFeatureID, Double>> features = new ArrayList<>();
			if(object.get("V") instanceof JSONArray) {
				JSONArray bursts = object.getJSONArray("V");				
				for(int i=0;i<bursts.length();i++) {
					features.add(singleBurstFeatures(bursts.getJSONObject(i)));
				}
			}
				double totW=0;
				if(!object.has("tot_w")){
					System.out.println("Input error: Enter tot_w in bursts:{...}\t\t");
					System.exit(-1);
				}
				totW = object.getDouble("tot_w");
				
				if(!object.has("feat_w")){
					System.out.println("Input error: Enter feat_w for burst features inside bursts:{...}\t\t");
					System.exit(-1);
				}
				// resue value reading for weight reading in this case:
				HashMap<BurstFeatureID, Double> featW = singleBurstFeatures(object.getJSONObject("feat_w"));
				
				burstFeature = new BurstFeature( features, featW, totW);
			
		}		
		return burstFeature;
	}
	private static HashMap<BurstFeatureID, Double> singleBurstFeatures(JSONObject object){
		HashMap<BurstFeatureID, Double> singleBurst=null;
		if(object!=null){
			singleBurst = new HashMap<>();
			if(object.has(BurstFeatureID.nspikes.name())){
				singleBurst.put(BurstFeatureID.nspikes, object.getDouble(BurstFeatureID.nspikes.name()));
			}
			if(object.has(BurstFeatureID.nsfa.name())){
				singleBurst.put(BurstFeatureID.nsfa, object.getDouble(BurstFeatureID.nsfa.name()));
			}
			if(object.has(BurstFeatureID.sfa_m.name())){
				singleBurst.put(BurstFeatureID.sfa_m, object.getDouble(BurstFeatureID.sfa_m.name()));
			}
			if(object.has(BurstFeatureID.sfa_b.name())){
				singleBurst.put(BurstFeatureID.sfa_b, object.getDouble(BurstFeatureID.sfa_b.name()));
			}
			if(object.has(BurstFeatureID.b_w.name())){
				singleBurst.put(BurstFeatureID.b_w, object.getDouble(BurstFeatureID.b_w.name()));
			}
			if(object.has(BurstFeatureID.pbi.name())){
				singleBurst.put(BurstFeatureID.pbi, object.getDouble(BurstFeatureID.pbi.name()));
			}
			if(object.has(BurstFeatureID.pbi_vmin_offset.name())){
				singleBurst.put(BurstFeatureID.pbi_vmin_offset, object.getDouble(BurstFeatureID.pbi_vmin_offset.name()));
			}
		}
		return singleBurst;
		
	}
	private static PatternFeature buildPatternFeature(JSONObject object, PatternFeatureID feature) {
		PatternFeature consFeature = null;	
		if(object!=null) {
			if(object.get("V") instanceof JSONArray) {
				JSONArray values = object.getJSONArray("V");
				double W=0;
				if(!PatternFeature.W_NOT_REQUIRED.contains(feature)){
					W = object.getDouble("W");
				}				
				consFeature = new PatternFeature( values.getDouble(0), values.getDouble(1), W);
			}else{
				double value = object.getDouble("V");
				double W=0;
				if(!PatternFeature.W_NOT_REQUIRED.contains(feature)){
					W = object.getDouble("W");
				}	
				consFeature = new PatternFeature( value, W);
			}
		}
		return consFeature;
	}
	public static PhenotypeConstraint[] getPhenoTypeConstraintData(String fileName) {	
		PhenotypeConstraint[] phenConsData = null;
		
		try{
			JSONObject jsonObj = readASPISIs(fileName);
			JSONArray jsonArray = jsonObj.getJSONArray("phenotype_constraints");
			int includedLength = 0;
			for(int idx = 0; idx<jsonArray.length(); idx++) {
				JSONObject phenConstraint = jsonArray.getJSONObject(idx);
				if(phenConstraint.getBoolean(PhenotypeConstraintAttributeID.INCLUDE.name())){
					includedLength++;
				}
			}
			
			phenConsData = new PhenotypeConstraint[includedLength];
			int idx=0;
			for(int ij = 0; ij<jsonArray.length(); ij++) {				
				JSONObject phenCons = jsonArray.getJSONObject(ij);
				if(!phenCons.getBoolean(PhenotypeConstraintAttributeID.INCLUDE.name())){
					continue;
				}
				
				phenConsData[idx] = new PhenotypeConstraint(PhenotypeConstraintType.valueOf(
						phenCons.getString(MCConstraintAttributeID.type.name())));
				//excitability	
				
				PhenotypeConstraintAttributeID attID = PhenotypeConstraintAttributeID.current_min;
				phenConsData[idx].addAttribute(attID, phenCons.getDouble(attID.name()));				
				attID = PhenotypeConstraintAttributeID.current_max;
				phenConsData[idx].addAttribute(attID, phenCons.getDouble(attID.name()));
				attID = PhenotypeConstraintAttributeID.current_duration;
				phenConsData[idx].addAttribute(attID, phenCons.getDouble(attID.name()));
				attID = PhenotypeConstraintAttributeID.current_step;
				phenConsData[idx].addAttribute(attID, phenCons.getDouble(attID.name()));
				attID = PhenotypeConstraintAttributeID.min_freq;
				phenConsData[idx].addAttribute(attID, phenCons.getDouble(attID.name()));
				attID = PhenotypeConstraintAttributeID.cons_weight;
				phenConsData[idx].addAttribute(attID, phenCons.getDouble(attID.name()));
				
				idx++;
			}
			
		}catch(JSONException e){
			//System.out.println(e.getMessage());
		}	
		
		return phenConsData;
	}
	public static InputMCConstraint[] getMCConstraintData(String fileName) {	
		InputMCConstraint[] mcConsData = null;
		JSONObject jsonObj = readASPISIs(fileName);
		JSONArray jsonArray = jsonObj.getJSONArray("multi_comp_constraints");
		
		if(jsonArray.length() > 0) {
			int includedLength = 0;
			for(int idx = 0; idx<jsonArray.length(); idx++) {
				JSONObject mcConstraint = jsonArray.getJSONObject(idx);
				if(mcConstraint.getBoolean(PatternFeatureID.INCLUDE.name())){
					includedLength++;
				}
			}
			
			mcConsData = new InputMCConstraint[includedLength];
			int idx=0;
			for(int ij = 0; ij<jsonArray.length(); ij++) {				
				JSONObject mcCons = jsonArray.getJSONObject(ij);
				if(!mcCons.getBoolean(PatternFeatureID.INCLUDE.name())){
					continue;
				}
				
				mcConsData[idx] = new InputMCConstraint(MCConstraintType.valueOf(
															mcCons.getString(MCConstraintAttributeID.type.name())));
				//excitability	
				addAttributeIfPresent(mcConsData[idx], mcCons, MCConstraintAttributeID.current_min);
				addAttributeIfPresent(mcConsData[idx], mcCons, MCConstraintAttributeID.current_max);
				addAttributeIfPresent(mcConsData[idx], mcCons, MCConstraintAttributeID.current_duration);
				addAttributeIfPresent(mcConsData[idx], mcCons, MCConstraintAttributeID.current_step);
				addAttributeIfPresent(mcConsData[idx], mcCons, MCConstraintAttributeID.rheo_diff);
				//IR					
				addAttributeIfPresent(mcConsData[idx], mcCons, MCConstraintAttributeID.v_at_time);
				addAttributeIfPresent(mcConsData[idx], mcCons, MCConstraintAttributeID.current);
				
				//forward propagation
				addAttributeIfPresent(mcConsData[idx], mcCons, MCConstraintAttributeID.dend_current_min);
				addAttributeIfPresent(mcConsData[idx], mcCons, MCConstraintAttributeID.dend_current_max);
				addAttributeIfPresent(mcConsData[idx], mcCons, MCConstraintAttributeID.dend_current_time_min);
				addAttributeIfPresent(mcConsData[idx], mcCons, MCConstraintAttributeID.dend_current_duration);
				addAttributeIfPresent(mcConsData[idx], mcCons, MCConstraintAttributeID.dend_current_step);
				addAttributeIfPresent(mcConsData[idx], mcCons, MCConstraintAttributeID.dend_target_spike_freq);
				addAttributeIfPresent(mcConsData[idx], mcCons, MCConstraintAttributeID.spike_prop_rate_min);				
				//syn stimulated epsp
				addAttributeIfPresent(mcConsData[idx], mcCons, MCConstraintAttributeID.sim_duration);
				addAttributeIfPresent(mcConsData[idx], mcCons, MCConstraintAttributeID.ampa_epsp);
				addAttributeIfPresent(mcConsData[idx], mcCons, MCConstraintAttributeID.ampa_tau);
				addAttributeIfPresent(mcConsData[idx], mcCons, MCConstraintAttributeID.nmda_epsp);
				addAttributeIfPresent(mcConsData[idx], mcCons, MCConstraintAttributeID.nmda_tau);
				//common
				addAttributeIfPresent(mcConsData[idx], mcCons, MCConstraintAttributeID.cons_weight);
				idx++;
			}
		}
		return mcConsData;
	}
	
	private static void addAttributeIfPresent(InputMCConstraint mcConsData, JSONObject mcCons, MCConstraintAttributeID attID){
		if(mcCons.has(attID.name())) {	
			/*
			 * flow for mcCons ranged attribute check is a little different from pattern features
			 */
			if(!mcConsData.doesRangedAttrListContain(attID))
				mcConsData.addAttribute(attID, mcCons.getDouble(attID.name()));
			else
				mcConsData.addAttributeWrange(attID, buildMcConsRangedFeatArray(mcCons.getJSONObject(attID.name())));
		}
	}
	
	private static double[] buildMcConsRangedFeatArray(JSONObject object) {
		double[] RangedFeatArray = null;
		if(object!=null) {
			if(object.get("V") instanceof JSONArray) {
				JSONArray values = object.getJSONArray("V");							
				RangedFeatArray = new double[] { values.getDouble(0), values.getDouble(1)};
			}
		}
		return RangedFeatArray;
	}
	
	public static double[] getPatternRepairWeights(String fileName) {	
		double[] patternWeights = null;
		JSONObject jsonObj = readASPISIs(fileName);
		JSONArray jsonArray = jsonObj.getJSONArray("pattern_repair_weights");
		
		if(jsonArray.length() > 0) {
			patternWeights= new double[3];
			for(int idx=0;idx<3;idx++){
				patternWeights[idx]=jsonArray.getDouble(idx);
			}
		}
		return patternWeights;
	}
	
	public static InputModelParameterRanges getInputModelParameterRanges(String fileName) {	
		InputModelParameterRanges ranges = new InputModelParameterRanges();
		JSONObject jsonObj = readASPISIs(fileName);
		JSONObject parm_ranges = jsonObj.getJSONObject("parameter_ranges");
		
		ModelParameterID parmID = null;
		
		parmID = ModelParameterID.K;
		JSONArray minMax = parm_ranges.getJSONArray(parmID.name());
		ranges.addRange(parmID, ""+minMax.getDouble(0), ""+minMax.getDouble(1));
		
		parmID = ModelParameterID.A;
		minMax = parm_ranges.getJSONArray(parmID.name());
		ranges.addRange(parmID, ""+minMax.getDouble(0), ""+minMax.getDouble(1));
		
		parmID = ModelParameterID.B;
		minMax = parm_ranges.getJSONArray(parmID.name());
		ranges.addRange(parmID, ""+minMax.getDouble(0), ""+minMax.getDouble(1));
		
		parmID = ModelParameterID.D;
		minMax = parm_ranges.getJSONArray(parmID.name());
		ranges.addRange(parmID, ""+minMax.getInt(0), ""+minMax.getInt(1));
		
		parmID = ModelParameterID.CM;
		minMax = parm_ranges.getJSONArray(parmID.name());
		ranges.addRange(parmID, ""+minMax.getInt(0), ""+minMax.getInt(1));
		
		parmID = ModelParameterID.VR;
		minMax = parm_ranges.getJSONArray(parmID.name());
		ranges.addRange(parmID, ""+minMax.getDouble(0), ""+minMax.getDouble(1));
		
		parmID = ModelParameterID.VMIN;
		minMax = parm_ranges.getJSONArray(parmID.name());
		ranges.addRange(parmID, ""+minMax.getDouble(0), ""+minMax.getDouble(1));
		
		parmID = ModelParameterID.VT;
		minMax = parm_ranges.getJSONArray(parmID.name());
		ranges.addRange(parmID, ""+minMax.getDouble(0), ""+minMax.getDouble(1));
		
		parmID = ModelParameterID.VPEAK;
		minMax = parm_ranges.getJSONArray(parmID.name());
		ranges.addRange(parmID, ""+minMax.getDouble(0), ""+minMax.getDouble(1));
		
		parmID = ModelParameterID.G;
		minMax = parm_ranges.getJSONArray(parmID.name());
		ranges.addRange(parmID, ""+minMax.getInt(0), ""+minMax.getInt(1));
		
		parmID = ModelParameterID.P;
		minMax = parm_ranges.getJSONArray(parmID.name());
		ranges.addRange(parmID, ""+minMax.getDouble(0), ""+minMax.getDouble(1));
		
		parmID = ModelParameterID.W;
		minMax = parm_ranges.getJSONArray(parmID.name());
		ranges.addRange(parmID, ""+minMax.getDouble(0), ""+minMax.getDouble(1));
		
		return ranges;
	}
	
	/*
	 * for later: to use enum feature
	 */
	/*
	private static PatternFeature buildConstraintFeature(PatternFeatureID feature, JSONObject object) {
		PatternFeature consFeature = null;	
		if(object!=null) {
			if(object.get("V") instanceof JSONArray) {
				JSONArray values = object.getJSONArray("V");
				double W = object.getDouble("W");
				consFeature = new PatternFeature(feature, values.getDouble(0), values.getDouble(1), W);
			}else{
				double value = object.getDouble("V");
				double W = object.getDouble("W");
				consFeature = new PatternFeature(feature, value, W);
			}
		}
		return consFeature;
	}
	*/
	
}
