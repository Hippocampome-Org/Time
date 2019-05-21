package ec.app.izhikevich.util.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import ec.app.izhikevich.util.GeneralUtils;

public class FitFileStructure {

	String spikePatternClass;	
	double InputCurrent;
	double currentDuration;
	double patternError;
	
	double[] fsl;
	double[] pss;
	boolean hasPSS;
	
	int[] nSpikes;
	boolean hasNSpikes;
	
	double[] sfa_m0;
	double[] sfa_c0;
	int[] nISIs_0;
	boolean hasRASP;
	
	double[] sfa_m1;
	double[] sfa_m2;
	double[] sfa_c1;
	double[] sfa_c2;
	int[] nISIs_1;
	int[] nISIs_2;
	boolean hasSP;
	
	int[] nBursts;
	List<Double>[] bw;
	List<Double>[] burst_n_spikes;
	List<Double>[] pbi;	
	boolean hasBURST;
	
	
	
	FitFileStructure(){
		fsl = new double[2];
		pss = new double[2];
		hasPSS = false;
		nSpikes = new int[2];
		hasNSpikes = false;
		
		sfa_m0 = new double[2];
		sfa_c0 = new double[2];
		nISIs_0 = new int[2];
		hasRASP = false;
		
		sfa_m1 = new double[2];
		sfa_m2 = new double[2];
		sfa_c1 = new double[2];
		sfa_c2 = new double[2];
		nISIs_1 = new int[2];
		nISIs_2 = new int[2];	
		hasSP = false;
		
		nBursts = new int[2];
		
		bw = new ArrayList[2];
		burst_n_spikes = new ArrayList[2];
		pbi = new ArrayList[2];			
		for(int i=0;i<bw.length;i++) {
			bw[i]=new ArrayList<>();
			burst_n_spikes[i]=new ArrayList<>();
			pbi[i]=new ArrayList<>();
		}
		hasBURST=false;
	}
	
	private double formatNumeric(double value) {
		return Double.parseDouble(GeneralUtils.formatThreeDecimal(value));
	}
	
	public JSONObject getJObject() {
		JSONObject jObject = new JSONObject();
		jObject.put("input_current", formatNumeric(InputCurrent));
		jObject.put("current_duration", formatNumeric(currentDuration));
		jObject.put("pattern_class", spikePatternClass);
		
		JSONObject jObject_fsl = new JSONObject();
		jObject_fsl.put("exp", formatNumeric(fsl[0]));
		jObject_fsl.put("model", formatNumeric(fsl[1]));		
		jObject.put("fsl",jObject_fsl);
		
		if(hasNSpikes) {
			JSONObject jObject_nspikes = new JSONObject();
			jObject_nspikes.put("exp", formatNumeric(nSpikes[0]));
			jObject_nspikes.put("model", formatNumeric(nSpikes[1]));		
			jObject.put("n_spikes",jObject_nspikes);
		}
		
		if(hasRASP) {
			JSONObject jObject_sfa0 = new JSONObject();
			JSONArray jArrExp0 = new JSONArray();
			JSONArray jArrMod0 = new JSONArray();
			jArrExp0.put("Y = "+formatNumeric(sfa_m0[0])+"X + "+formatNumeric(sfa_c0[0]));
			jArrExp0.put("n_ISIs="+nISIs_0[0]);
			jArrMod0.put("Y = "+formatNumeric(sfa_m0[1])+"X + "+formatNumeric(sfa_c0[1]));
			jArrMod0.put("n_ISIs="+nISIs_0[1]);		
			jObject_sfa0.put("exp", jArrExp0);
			jObject_sfa0.put("model", jArrMod0);		
			jObject.put("sfa_0",jObject_sfa0);
		}		
		
		if(hasSP) {
			JSONObject jObject_sfa1 = new JSONObject();
			JSONArray jArrExp1 = new JSONArray();
			JSONArray jArrMod1 = new JSONArray();
			jArrExp1.put("Y = "+formatNumeric(sfa_m1[0])+"X + "+formatNumeric(sfa_c1[0]));
			jArrExp1.put("n_ISIs="+nISIs_1[0]);
			jArrMod1.put("Y = "+formatNumeric(sfa_m1[1])+"X + "+formatNumeric(sfa_c1[1]));
			jArrMod1.put("n_ISIs="+nISIs_1[1]);		
			jObject_sfa1.put("exp", jArrExp1);
			jObject_sfa1.put("model", jArrMod1);		
			jObject.put("sfa_1",jObject_sfa1);
			
			JSONObject jObject_sfa2 = new JSONObject();
			JSONArray jArrExp2 = new JSONArray();
			JSONArray jArrMod2 = new JSONArray();
			jArrExp2.put("Y = "+formatNumeric(sfa_m2[0])+"X + "+formatNumeric(sfa_c2[0]));
			jArrExp2.put("n_ISIs="+nISIs_2[0]);
			jArrMod2.put("Y = "+formatNumeric(sfa_m2[1])+"X + "+formatNumeric(sfa_c2[1]));
			jArrMod2.put("n_ISIs="+nISIs_2[1]);		
			jObject_sfa2.put("exp", jArrExp2);
			jObject_sfa2.put("model", jArrMod2);		
			jObject.put("sfa_2",jObject_sfa2);
		}
		
		if(hasBURST) {			
			JSONObject jObject_bursts = new JSONObject();
			
			JSONObject jObject_nbursts = new JSONObject();
			jObject_nbursts.put("exp", formatNumeric(nBursts[0]));
			jObject_nbursts.put("model", formatNumeric(nBursts[1]));			
			jObject_bursts.put("n_bursts", jObject_nbursts);
			
			JSONArray jArray_burstFeats = new JSONArray();	
			
			int smallerNbursts = (nBursts[0] < nBursts[1]) ? nBursts[0]:nBursts[1];
			for(int i=0;i<smallerNbursts;i++) {
				JSONObject burstObject = constructSingleBurstObject(i, smallerNbursts);
				jArray_burstFeats.put(burstObject);
			}
			jObject_bursts.put("burst_features", jArray_burstFeats);
			
			jObject.put("bursts",jObject_bursts);
		}
		
		if(hasPSS) {
			JSONObject jObject_pss = new JSONObject();
			jObject_pss.put("exp", formatNumeric(pss[0]));
			jObject_pss.put("model", formatNumeric(pss[1]));		
			jObject.put("pss",jObject_pss);
		}
		
		jObject.put("pattern_error", formatNumeric(patternError));
		
		return jObject;
	}
	private JSONObject constructSingleBurstObject(int i, int nBursts) {
		JSONObject burstObject = new JSONObject();
		
		JSONObject bwObject = new JSONObject();
		bwObject.put("exp", bw[0].get(i));
		bwObject.put("model", bw[1].get(i));		
		burstObject.put("bw", bwObject);
		
		JSONObject nspikesObj = new JSONObject();
		nspikesObj.put("exp", burst_n_spikes[0].get(i));
		nspikesObj.put("model", burst_n_spikes[1].get(i));		
		burstObject.put("n_spikes", nspikesObj);
		
		if(i!=nBursts-1) {
			JSONObject pbiObject = new JSONObject();
			pbiObject.put("exp", pbi[0].get(i));
			pbiObject.put("model", pbi[1].get(i));		
			burstObject.put("pbi", pbiObject);
		}		
		
		return burstObject;
	}
}
