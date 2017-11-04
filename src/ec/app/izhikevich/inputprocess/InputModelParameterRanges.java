package ec.app.izhikevich.inputprocess;

import java.util.HashMap;
import java.util.Map;

import ec.app.izhikevich.inputprocess.labels.ModelParameterID;

public class InputModelParameterRanges {	
	private Map<ModelParameterID, String[]> range;

	public InputModelParameterRanges() {
		range = new HashMap<>();
	}
	public void addRange(ModelParameterID parmID, String min, String max){
		range.put(parmID, new String[]{min, max});
	}
	public String[] getMinMax(ModelParameterID id){
		return this.range.get(id);
	}
	
	public Map<ModelParameterID, String[]> getRanges() {
		return range;
	}	
	
}
