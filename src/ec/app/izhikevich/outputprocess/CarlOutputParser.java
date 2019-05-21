package ec.app.izhikevich.outputprocess;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CarlOutputParser {
	
	JSONObject phenTypeObject;
	
	public CarlOutputParser(String fileName, boolean deleteFile){
		try {
			File file = new File(fileName);
			Scanner scanner = new Scanner(file);
			String content = scanner.useDelimiter("\\Z").next();
			phenTypeObject = new JSONObject(content);
			scanner.close();
			if(deleteFile)
				file.delete();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	public CarlOutputParser(String fileName){
		this(fileName, true);
	}
		
	public CarlSpikePattern[] extractCarlSomaPatterns (){
		CarlSpikePattern[] somaPatterns = null;
		
		//JSONObject jsonObj = readASPISIs();
		JSONArray jsonArray = phenTypeObject.getJSONArray("soma_patterns");
		somaPatterns = new CarlSpikePattern[jsonArray.length()];
		
		for(int i=0;i<somaPatterns.length;i++){
			somaPatterns[i]=constructCarlSomaPattern(jsonArray.getJSONObject(i));
		}
		
		return somaPatterns;
	}

	public CarlMcSimData extractCarlMcSimData(){
		CarlMcSimData mcSimData = null;
		//JSONObject jsonObj = readASPISIs();
		JSONObject multi_comp_sim = phenTypeObject.getJSONObject("multi_comp_sim");
		
		if(multi_comp_sim.has("epspsAll_0")) {
			double[] epsps = jsonArrayToDouble(multi_comp_sim.getJSONArray(CarlMcSimDataLabels.epsps.name()));
			double[][] epspsAll = new double[epsps.length][epsps.length+1];
			
			for(int i=0;i<epsps.length;i++) {
				epspsAll[i] = jsonArrayToDouble(multi_comp_sim.getJSONArray(CarlMcSimDataLabels.epspsAll.name()+"_"+i));
			}
			
			mcSimData = new CarlMcSimData(jsonArrayToDouble(multi_comp_sim.getJSONArray(CarlMcSimDataLabels.ramp_rheos.name())),
					jsonArrayToDouble(multi_comp_sim.getJSONArray(CarlMcSimDataLabels.v_defs.name())),
					jsonArrayToFloat(multi_comp_sim.getJSONArray(CarlMcSimDataLabels.spike_proped.name())),
					epsps,
					epspsAll
					);
			
		}else {
			mcSimData = new CarlMcSimData(jsonArrayToDouble(multi_comp_sim.getJSONArray(CarlMcSimDataLabels.ramp_rheos.name())),
					jsonArrayToDouble(multi_comp_sim.getJSONArray(CarlMcSimDataLabels.v_defs.name())),
					jsonArrayToFloat(multi_comp_sim.getJSONArray(CarlMcSimDataLabels.spike_proped.name())),
					jsonArrayToDouble(multi_comp_sim.getJSONArray(CarlMcSimDataLabels.epsps.name()))				
					);
		}		
		return mcSimData;
	}
	private CarlSpikePattern constructCarlSomaPattern(JSONObject jObject){
		CarlSpikePattern somaPattern = null;
		try{
			float I = (float) jObject.getDouble(CarlSomaPatternLabels.I.name());
			float Idur = (float) jObject.getDouble(CarlSomaPatternLabels.I_dur.name());
			float tStep = (float) jObject.getDouble(CarlSomaPatternLabels.t_step.name());		
			double[] vTrace = jsonArrayToDouble(jObject.getJSONArray(CarlSomaPatternLabels.v_trace.name()));
			double[] spikeTimes = jsonArrayToDouble(jObject.getJSONArray(CarlSomaPatternLabels.spike_times.name()));		
			
			somaPattern = new CarlSpikePattern();
			somaPattern.setI(I);
			somaPattern.setIDur(Idur);
			somaPattern.settStep(tStep);
			somaPattern.setvTrace(vTrace);
			somaPattern.setSpikeTimes(spikeTimes);
		}catch(JSONException e){
			e.printStackTrace();
			System.out.println("CARL output parser error!");
		}
		return somaPattern;
	}
	
	private double[] jsonArrayToDouble(JSONArray array){
		double[] doubleArray = null;
		if(array!=null){
			doubleArray = new double[array.length()];
			for(int i=0;i<doubleArray.length;i++){
				doubleArray[i] = array.getDouble(i);
			}
		}	
		return doubleArray;
	}
	private float[] jsonArrayToFloat(JSONArray array){
		float[] floatArray = null;
		if(array!=null){
			floatArray = new float[array.length()];
			for(int i=0;i<floatArray.length;i++){
				floatArray[i] = (float)array.getDouble(i);
			}
		}	
		return floatArray;
	}
	private boolean[] jsonArrayToBoolean(JSONArray array){
		boolean[] boolArray = null;
		if(array!=null){
			boolArray = new boolean[array.length()];
			for(int i=0;i<boolArray.length;i++){
				boolArray[i] = array.getBoolean(i);
			}
		}	
		return boolArray;
	}
	
	public static void main(String[] args){
		
		String fileName = "output/0_phenotype";
		
		CarlOutputParser parser = new CarlOutputParser(fileName);
		CarlSpikePattern[] somaPAtterns = parser.extractCarlSomaPatterns();
		for(CarlSpikePattern pattern: somaPAtterns)
			pattern.getODESolutionFormat().display();
		
	}
}
