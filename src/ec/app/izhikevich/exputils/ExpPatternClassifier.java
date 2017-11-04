package ec.app.izhikevich.exputils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ec.app.izhikevich.evaluator.qualifier.ClassificationParameterID;
import ec.app.izhikevich.evaluator.qualifier.SpikePatternClassifier;
import ec.app.izhikevich.evaluator.qualifier.StatAnalyzer;
import ec.app.izhikevich.spike.SpikePatternAdapting;
import ec.app.izhikevich.spike.labels.SpikePatternClass;
import ec.app.izhikevich.spike.labels.SpikePatternComponent;

public class ExpPatternClassifier {

	 
	public static void main(String[] args) {
		Map<NeuronType, List<SpikePatternTrace>> map = EphysData.readExcelData();
	//	List<SpikePatternTrace> traces = EphysData.fetchSpikePatternTracesByUniqueID(map, "4-000");
		//Map<NeuronType, List<SpikePatternTrace>> mp = EphysData.fetchAllByRegion(map, Region.CA1);
		//Map<NeuronType, List<SpikePatternTrace>> mp2 = EphysData.fetchNeuronTypesByComponent(mp, SpikePatternComponent.EMPTY, false);
		//List<SpikePatternTrace> traces = EphysData.fetchTracesByComponent(mp, SpikePatternComponent.EMPTY, false);
		//int n = EphysData.countEmptyEvidenceNeuronTypes(mp);
		//System.out.println(traces.size());
		//map = EphysData.fetchMultiBehaviorTypes(map);
		map = EphysData.fetchSingleBehaviorTypes(map);
		//List<SpikePatternTrace> traces = EphysData.fetchSubTypeOrMultiBehaviorTraces(map);
		//SpikePatternComponent component = SpikePatternComponent.PSTUT;
		 SpikePatternClass _class =  new SpikePatternClass("ASP.", ".");		
										
										//EphysData.fetchTracesByComponent(map, SpikePatternComponent.PSTUT, false);
		map = EphysData.fetchAllByClass(map, _class);
		
		//map = EphysData.fetchNeuronTypesByComponent(map, component, false);
		// System.out.println(map.size());
		// System.out.println(EphysData.countEmptyEvidenceNeuronTypes(map));
		ExcelLabel[] labelsToPrint = {
										ExcelLabel.NEURON_TYPE, ExcelLabel.UNIQUE_ID, ExcelLabel.subtypes, ExcelLabel.Phen_cat,
										ExcelLabel.J_class,
										ExcelLabel.I, ExcelLabel.I_DUR, ExcelLabel.FSL, ExcelLabel.PSS, ExcelLabel.N_ISI,
							
										ExcelLabel.M_2_1,  ExcelLabel.C_2_1, ExcelLabel.M_3_1, ExcelLabel.C_3_1, ExcelLabel.C_3_2, 
										ExcelLabel.M_4_1, ExcelLabel.C_4_1, ExcelLabel.M_4_2, ExcelLabel.C_4_2
							
										};
		//System.out.println(traces.size());
		//ClassificationParameterID[] parms = ClassificationParameterID.values();	
		//System.out.print("Trace\tClass\t");
		for(int i=0;i<labelsToPrint.length;i++)
			System.out.print(labelsToPrint[i]+",");
		System.out.println();
		EphysData.printMap(map, labelsToPrint);
		//EphysData.printTraces(traces, labelsToPrint);
		//classifyAll(map, parms);		
/*	String[]  ids = {"4-000"};// "1-771", "1-772"};
		for(int i=0;i<ids.length;i++){
			List<SpikePatternTrace> traces = EphysData.fetchSpikePatternTracesByUniqueID(map, ids[i]);  
			System.out.print(ids[i]+"\t");
			classifySingleTrace(traces, 0, parms);		
			System.out.println();
		}
		*/
	}
	
	private static void classifyAll(Map<NeuronType, List<SpikePatternTrace>> map, ClassificationParameterID[] parms){
		Iterator it = map.entrySet().iterator();
	        while (it.hasNext())
	        {
	            Map.Entry pair = (Map.Entry)it.next();
	            NeuronType nt = (NeuronType) pair.getKey();
	            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();
	            for(SpikePatternTrace _trace: traces)
	            {
	            	String excelClass = _trace.getMappedData().get(ExcelLabel.PATTERN_CLASS); 
	            	SpikePatternClass newClass = classify(_trace, false);
	            	{
	            	//	_trace.display(labelsToPrint);
	            		newClass.display(parms);//	 
		    			 System.out.println();
	            	}
	            }	
	           
	        }
	}
	private static void classifySingleTrace(List<SpikePatternTrace> traces, int trace_id, ClassificationParameterID[] parms){
		SpikePatternTrace _trace = traces.get(trace_id);
    	SpikePatternClass newClass = classify(_trace, false);
    		newClass.display(parms);    	 
	}
	private static void displayInRows(Map<NeuronType, List<SpikePatternTrace>> map ){
		Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            NeuronType nt = (NeuronType) pair.getKey();
            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();
            for(SpikePatternTrace _trace: traces){
            	//_trace.display(labelsToPrint);
            	System.out.print("\t");
    			classify(_trace, false).display();//displayWithAllSlopes();
    			 
            }	
        }
	}
	private static SpikePatternClass classify(SpikePatternTrace trace, boolean displayStats){
		//SpikePatternClass _class = new SpikePatternClass("TSTUT.NASP", ".");		
				
		
		double current = -77;//Double.parseDouble(trace.getMappedData().get(ExcelLabel.I));
		double timeMin = 0;
		
		String dur = trace.getMappedData().get(ExcelLabel.I_DUR);
		double durationOfCurrentInjected = -1;
		if(!dur.equals("EMPTY")){
			durationOfCurrentInjected = Double.parseDouble(dur);
		}
		 
		double fsl = -1;
		String fsl_ = trace.getMappedData().get(ExcelLabel.FSL);
		if(!fsl_.equals("EMPTY")){
			fsl = Double.parseDouble(fsl_);
		}
		
		double[] spikeTimes = constructSpikeTimesFromISIs(trace.getISIs(), fsl );
		//System.out.print("SpikeTimes. ");
		//GeneralUtils.displayArray(spikeTimes);
		//System.out.print("Original class. "); trace.getPatternClass().display();
		//System.out.println("\n");
		
		SpikePatternAdapting sp = new SpikePatternAdapting(spikeTimes, current, timeMin, durationOfCurrentInjected);
		SpikePatternClassifier classifier = new SpikePatternClassifier(sp);
		StatAnalyzer.display_stats = displayStats;
		String swa = trace.getMappedData().get(ExcelLabel.SWA);
		double _swa =0;
		if(!swa.equals("EMPTY")){
			_swa = Double.parseDouble(swa);
		}
		classifier.classifySpikePattern_EXP(_swa, false);
		return classifier.getSpikePatternClass();
	}
	private static double[] constructSpikeTimesFromISIs(ArrayList<Double> ISIs, double delay){
		double[] st = new double[ISIs.size()+1];
		st[0] = delay;
		for(int i=1;i<st.length;i++){
			st[i] = st[i-1]+ISIs.get(i-1);
		}
		return st;
	}
}
