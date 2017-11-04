package ec.app.izhikevich.exputils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import ec.app.izhikevich.inputprocess.InputSpikePatternConstraint;
import ec.app.izhikevich.inputprocess.labels.MCConstraintAttributeID;
import ec.app.izhikevich.inputprocess.labels.MCConstraintType;
import ec.app.izhikevich.inputprocess.labels.ModelParameterID;
import ec.app.izhikevich.inputprocess.labels.PatternFeatureID;
import ec.app.izhikevich.spike.PatternFeature;
import ec.app.izhikevich.spike.labels.SpikePatternClass;
import ec.app.izhikevich.util.GeneralUtils;

public class ConstraintConstructor {

	private static final String filePrefix = "input/10_2_1/";
	String fileName;
	Map<NeuronType, List<SpikePatternTrace>> ephysData;
	
	JSONObject PT_n;
	
	JSONObject parmRanges;
	JSONArray spikePatternConstraints;
	JSONArray patternRepairWeights;
	JSONArray mcConstraints;
	
	ConstraintConstructor(String fileName, Map<NeuronType, List<SpikePatternTrace>> ephys_data){
		this.fileName = filePrefix+fileName;
		this.ephysData = ephys_data;
		PT_n = new JSONObject();
		
		parmRanges = new JSONObject();
		spikePatternConstraints = new JSONArray();
		mcConstraints = new JSONArray();
		patternRepairWeights = new JSONArray();
		PT_n.put("parameter_ranges", parmRanges);	
		PT_n.put("spike_pattern_constraints", spikePatternConstraints);
		PT_n.put("multi_comp_constraints", mcConstraints);
		PT_n.put("pattern_repair_weights", patternRepairWeights);	
	}
	
	private void addParameterRanges(){			
		parmRanges.put(ModelParameterID.K.name(), modelParmRangeAsArray(1, 12));
		parmRanges.put(ModelParameterID.A.name(), modelParmRangeAsArray(0, 0.005));
		parmRanges.put(ModelParameterID.B.name(), modelParmRangeAsArray(-25, 25));
		parmRanges.put(ModelParameterID.D.name(), modelParmRangeAsArray(0, 120));
		
		parmRanges.put(ModelParameterID.CM.name(), modelParmRangeAsArray(10, 600));
		parmRanges.put(ModelParameterID.VR.name(), modelParmRangeAsArray(-60, -56));
		parmRanges.put(ModelParameterID.VT.name(), modelParmRangeAsArray(15, 18));
		parmRanges.put(ModelParameterID.VMIN.name(), modelParmRangeAsArray(20, 24));
		parmRanges.put(ModelParameterID.VPEAK.name(), modelParmRangeAsArray(75, 80));
		
		parmRanges.put(ModelParameterID.G.name(), modelParmRangeAsArray(5, 200));
		parmRanges.put(ModelParameterID.P.name(), modelParmRangeAsArray(0.1, 0.9));
		parmRanges.put(ModelParameterID.W.name(), modelParmRangeAsArray(0, 1));		
	}
	
	private JSONArray modelParmRangeAsArray(double min, double max){
		JSONArray kArray = new JSONArray();
		kArray.put(0, min);
		kArray.put(1, max);
		return kArray;
	}
	/*
	 * not used?
	 */
	private void addPatternRepairWeights(){		
		patternRepairWeights.put(0, 1.0);
		patternRepairWeights.put(1, 0.0);
		patternRepairWeights.put(2, 0.0);
	}
	private void addMcConstraints(){
		JSONObject excitability = new JSONObject();
		excitability.put(MCConstraintAttributeID.type.name(), MCConstraintType.EXCITABILITY);
		
		JSONObject inpRes = new JSONObject();
		inpRes.put(MCConstraintAttributeID.type.name(), MCConstraintType.INP_RES);
		
		JSONObject prop = new JSONObject();
		prop.put(MCConstraintAttributeID.type.name(), MCConstraintType.PROPAGATION);
		
		JSONObject syn = new JSONObject();
		syn.put(MCConstraintAttributeID.type.name(), MCConstraintType.SYN_STIM_EPSP);
		
		mcConstraints.put(0, excitability);
		mcConstraints.put(1, inpRes);
		mcConstraints.put(2, prop);
		mcConstraints.put(3, syn);
	}
	
	private void addSpikePatternConstraints(InputSpikePatternConstraint spikeConstraint){	
		JSONObject biggerObj = new JSONObject();		
		HashMap<PatternFeatureID, PatternFeature> feats = spikeConstraint.getFeatures();		
		Iterator it = feats.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            PatternFeatureID ID = (PatternFeatureID) pair.getKey();
            PatternFeature feat = (PatternFeature) pair.getValue();           
            biggerObj.put(ID.name(), feat.convertToJsonObject());      
                           
        }        
        biggerObj.put(PatternFeatureID.INCLUDE.name(), "true");
        JSONArray idx_noteArray = new JSONArray();
        idx_noteArray.put(spikeConstraint.getIndex());
        idx_noteArray.put(spikeConstraint.getNeuronTypeName());
        biggerObj.put(PatternFeatureID.IDX_NOTE.name(), idx_noteArray);
        biggerObj.put(PatternFeatureID.pattern_class.name(), spikeConstraint.getSpikePatternClass().toString());
        biggerObj.put(PatternFeatureID.compartment.name(), spikeConstraint.getCompartment());
        biggerObj.put(PatternFeatureID.type.name(), spikeConstraint.getType());
        biggerObj.put(PatternFeatureID.current_duration.name(), spikeConstraint.getCurrentDuration());
        biggerObj.put(PatternFeatureID.time_min.name(), spikeConstraint.getTimeMin());
        biggerObj.put(PatternFeatureID.pattern_weight.name(), spikeConstraint.getPatternWeight());
        
        biggerObj.put(PatternFeatureID.eval.name(), spikeConstraint.getFeatsToEvaluate());
        
		spikePatternConstraints.put(spikePatternConstraints.length(),biggerObj);
	}
	
	private List<InputSpikePatternConstraint> retrieveConstraintsForNeuron(String uniqueID, List<PatternFeatureID> features	){		
			List<InputSpikePatternConstraint> inpSpikePatternConsObjs = new ArrayList<>();
	        Iterator it = ephysData.entrySet().iterator();
	        while (it.hasNext()) {
	            Map.Entry pair = (Map.Entry)it.next();
	            NeuronType nt = (NeuronType) pair.getKey();
	            if(!nt.getUniqueID().equals(uniqueID)){
	            	continue;
	            }
	          //  nt.display();
	            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();
	            int idx = -1;
	            for(SpikePatternTrace _trace: traces){
	            	idx++;
	            	//for a trace, depending on pattern class, retrieve excel labels accordingly! (different ms, and bs for different classes)	            	
	            	double[] sfa = {0,0,0,0,0,0};
	    			double fsl=0, pss=0, current=0, current_dur = 0;     
	            	for(PatternFeatureID featID: features){
	    				ExcelLabel excelLabel = ExcelLabel.mapToExcelLabels(featID, _trace.getPatternClass());
	    				String item = _trace.getMappedData().get(excelLabel);
	    				if(featID.equals(PatternFeatureID.fsl))
	    					fsl = Double.valueOf(item);
	    				if(featID.equals(PatternFeatureID.pss))
	    					pss = Double.valueOf(item);
	    				if(featID.equals(PatternFeatureID.current)){
	    					if(GeneralUtils.isNumeric(item))
	    						current = Double.valueOf(item);
	    					else
	    						current = -7777;
	    				}	    					
	    				if(featID.equals(PatternFeatureID.current_duration))
	    					current_dur = Double.valueOf(item);
	    				
	    				
	    				if(featID.equals(PatternFeatureID.sfa_linear_m1))
	    					sfa[0] = Double.valueOf(item);
	    				if(featID.equals(PatternFeatureID.sfa_linear_b1))
	    					sfa[1] = Double.valueOf(item);
	    				if(featID.equals(PatternFeatureID.n_sfa_isis1))
	    					sfa[2] = Double.valueOf(item);
	    				
	    				if(featID.equals(PatternFeatureID.sfa_linear_m2))
	    					sfa[3] = Double.valueOf(item);
	    				if(featID.equals(PatternFeatureID.sfa_linear_b2))
	    					sfa[4] = Double.valueOf(item);
	    				if(featID.equals(PatternFeatureID.n_sfa_isis2))
	    					sfa[5] = Double.valueOf(item);
	    			}	 
	            	inpSpikePatternConsObjs.add(
	            			InputSpikePatternConstraint.
	            			constructInputSpikeConstraintObject(idx, nt.getName(), _trace.getPatternClass(), 
	            					current, current_dur, 
	            					fsl, pss, sfa)
	            					);
	            }	 
	  }
	        return inpSpikePatternConsObjs;
	}
	
	public void write(){
		FileWriter fw;
		try {
			fw = new FileWriter(new File(fileName));
			fw.write(PT_n.toString(3));
			
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static List<PatternFeatureID> getPreConstructedFeatIDList(){
		
		List<PatternFeatureID> featIDs = new ArrayList<>();
		featIDs.add(PatternFeatureID.fsl); 
		featIDs.add(PatternFeatureID.pss);
		featIDs.add(PatternFeatureID.current);
		featIDs.add(PatternFeatureID.current_duration);
			featIDs.add(PatternFeatureID.sfa_linear_m1);
			featIDs.add(PatternFeatureID.sfa_linear_b1);
			featIDs.add(PatternFeatureID.n_sfa_isis1);
			featIDs.add(PatternFeatureID.n_sfa_isis2);
				
		return featIDs;
	}
	public static void main(String[] args) {
		Map<NeuronType, List<SpikePatternTrace>> map = EphysData.readExcelData();
		//map = EphysData.fetchSingleBehaviorTypes(map);
		//SpikePatternClass _class = new SpikePatternClass("TSWB.SLN", ".");
		//map = EphysData.fetchAllByClass(map, _class);
		map = EphysData.fetchAsMapByUniqueID(map, "4-036");
		
		Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            NeuronType nt = (NeuronType) pair.getKey();
            List<SpikePatternTrace> _trace	 = (List<SpikePatternTrace>)pair.getValue();
            nt.display();
            String uniqueID = nt.getUniqueID();    		
            String fileName = "B2/"+uniqueID+".json";
    		ConstraintConstructor cons = new ConstraintConstructor(fileName, map);
    		cons.addParameterRanges();
    		cons.addPatternRepairWeights();
    		cons.addMcConstraints();    		
    		List<InputSpikePatternConstraint> spikePAtternConstraintsForNeuron = cons.retrieveConstraintsForNeuron(uniqueID,getPreConstructedFeatIDList());
    		for(InputSpikePatternConstraint constraint: spikePAtternConstraintsForNeuron){
    			cons.addSpikePatternConstraints(constraint);
    		}
    		cons.write();           
        }
        
			
		
		/*double current = 400;
		double current_duration = 1000;
		double fsl = 10.20;
		double pss = 40.50;
		double[] sfa = {0.123, 1.2, 7, 0.012, 2.2, 40};		
		InputSpikePatternConstraint consObj = InputSpikePatternConstraint.constructInputSpikeConstraintObject(0, _class, 
				current, current_duration, fsl, pss, sfa);
		*/
				
		
	}

}
