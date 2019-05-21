package ec.app.izhikevich.exputils;



import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ec.app.izhikevich.inputprocess.labels.PatternFeatureID;
import ec.app.izhikevich.spike.labels.SpikePatternClass;
import ec.app.izhikevich.spike.labels.SpikePatternComponent;

enum ExcelLabel{
	/*
	 * label(row_idx)
	 * row_idx is 1 based idx; getRowIdx() returns 0 based idx;
	 */
	REGION(1), NEURON_TYPE(2), UNIQUE_ID(3),  // for NeuronType class
	
	PMID(4), FIG_N(5), I(6), I_DUR(7), PATTERN_CLASS(16), ALT_CLASS(17), FSL(18), PSS(19), SWA(20), N_ISI(22), 
	//sfa stuff
	AI(38),
	ISI_AV(44),
	M_2_1(46), C_2_1(47),							//2 parm						// for spikePatternTrace class
	M_3_1(48), C_3_1(49), C_3_2(50),			// 3 parm
	M_4_1(52), C_4_1(53), M_4_2(54), C_4_2(55),	// 4 parm
	
	/*
	 * other parms not needed for the most part!
	 */
	
	F_12(58), F_12c(59), F_23(60), F_23c(61), F_34(62), F_34c(63),
	P_12(64), P_23(65), P_34(66), P_12uv(67),  P_23uv(68), P_34uv(69), subtypes(117), Phen_cat(118), J_class(121),
	N_ISI_cut_3p(131), N_ISI_cut_4p(132)
	,ISI1(151)
	;
	
	private int rowIdx;
	ExcelLabel(int row_idx){
		rowIdx = row_idx;
	}
	public int getRowIdx(){
		return rowIdx-1;
	}
	
	//sfa needs a slightly different take
	public static ExcelLabel mapToExcelLabels(PatternFeatureID featID, SpikePatternClass _class){
		if(featID.equals(PatternFeatureID.fsl)){
			return FSL;
		}
		if(featID.equals(PatternFeatureID.pss)){
			return PSS;
		}
		if(featID.equals(PatternFeatureID.pattern_class)){
			return PATTERN_CLASS;
		}
		
		if(featID.equals(PatternFeatureID.current)){
			return I;
		}
		if(featID.equals(PatternFeatureID.current_duration)){
			return I_DUR;
		}
		
		final SpikePatternClass NASP = new SpikePatternClass("NASP.", ".");
		final SpikePatternClass ASP = new SpikePatternClass("ASP.", ".");
		final SpikePatternClass D_NASP = new SpikePatternClass("D.NASP", ".");
		final SpikePatternClass D_ASP = new SpikePatternClass("D.ASP", ".");
		final SpikePatternClass ASP_SLN = new SpikePatternClass("ASP.SLN.", ".");
		
		final SpikePatternClass ASP_NASP = new SpikePatternClass("ASP.NASP", ".");		
		final SpikePatternClass ASP_ASP = new SpikePatternClass("ASP.ASP", ".");
		
		final SpikePatternClass PSTUT = new SpikePatternClass("PSTUT", ".");
		
		if(featID.equals(PatternFeatureID.sfa_linear_m1)){
			if(_class.equals(NASP) || _class.equals(ASP) || _class.equals(D_ASP) ||_class.equals(D_NASP)|| _class.equals(ASP_SLN) 
					|| _class.equals(PSTUT))
				return M_2_1;
			if(_class.equals(ASP_NASP))
				return M_3_1;		
			if(_class.equals(ASP_ASP))
				return M_4_1;
		}
		if(featID.equals(PatternFeatureID.sfa_linear_b1)){
			if(_class.equals(NASP) || _class.equals(ASP) || _class.equals(D_ASP) ||_class.equals(D_NASP)|| _class.equals(ASP_SLN) 
					|| _class.equals(PSTUT))
				return C_2_1;
			if(_class.equals(ASP_NASP))
				return C_3_1;	
			if(_class.equals(ASP_ASP))
				return C_4_1;
		}		
		if(featID.equals(PatternFeatureID.n_sfa_isis1)){
			if(_class.equals(NASP) || _class.equals(ASP) || _class.equals(D_ASP) ||_class.equals(D_NASP)|| _class.equals(ASP_SLN) 
					|| _class.equals(PSTUT))
				return N_ISI;
			if(_class.equals(ASP_NASP))
				return N_ISI;	 //this is where breakpoint should be available, ideally!!!
			if(_class.equals(ASP_ASP))
				return N_ISI;   //this is where breakpoint should be available, ideally!!!
		}	
		
		if(featID.equals(PatternFeatureID.sfa_linear_b2)){			
			if(_class.equals(ASP_NASP))
				return C_3_2;	
			if(_class.equals(ASP_ASP))
				return C_4_2;
			else
				return C_4_2;
		}
		if(featID.equals(PatternFeatureID.sfa_linear_m2)){			
			if(_class.equals(ASP_ASP))
				return M_4_2;	
			else
				return M_4_2;
		}		
		if(featID.equals(PatternFeatureID.n_sfa_isis2)){			
			if(_class.equals(ASP_NASP))
				return N_ISI;	 //this is where breakpoint should be available, ideally!!!
			if(_class.equals(ASP_ASP))
				return N_ISI;   //this is where breakpoint should be available, ideally!!!
			else
				return N_ISI;
		}
		
		System.out.println(featID.name() +" invalid request for class "+ _class.toString());
		System.exit(-1);
		return null;
	}
}
public class EphysData {		 
		private static final String file_pfx="input/AK/";
		private static final String file1 = "Firing pattern parameters-subtypes-12 - SV - 10_27_2016.xlsx";		
		//private static final String file1 = "Firing pattern parameters-subtypes-16.xlsx";	
		private static final int MAX_N_COLS = 1000;
		
		public static Map<NeuronType, List<SpikePatternTrace>> readExcelData(){
			return  readExcelData(file_pfx+file1);
		}
	    public static Map<NeuronType, List<SpikePatternTrace>> readExcelData(String fileName) {
	    	Map<NeuronType, List<SpikePatternTrace>> map = new TreeMap<>();
	         
	        try {
	            //Create the input stream from the xlsx/xls file	        	
	            FileInputStream fis = new FileInputStream(fileName);
	             
	            //Create Workbook instance for xlsx/xls file input stream
	            Workbook workbook = null;
	            if(fileName.toLowerCase().endsWith("xlsx")){
	                workbook = new XSSFWorkbook(fis);
	            }else if(fileName.toLowerCase().endsWith("xls")){
	                workbook = new HSSFWorkbook(fis);
	            }  
	             
	         
	            //build map   
	            boolean allRead = false;
	            ExcelLabel[] allLabels = ExcelLabel.values();	            
	            for(int col=1;col<MAX_N_COLS;col++){	            	
	            	Sheet sheet = workbook.getSheetAt(0);                 
		            Iterator<Row> rowIterator = sheet.iterator();    
		            //int rowIdx = -1;
		            
		            String neuronTypeName=null;
		            String uniqueID = null;
		            String patternClass = null;
		            SpikePatternTrace trace = new SpikePatternTrace();
		            
		            for(ExcelLabel label: allLabels){	//assuming idcs are in asc. order
		            	Row row = null;
                		while(rowIterator.hasNext()){
                			//rowIdx++;
    		                row = rowIterator.next();	
    		                if(label.getRowIdx()==row.getRowNum()){
    		                	break;
    		                }
                		}
                		//System.out.println(label.getRowIdx() +" "+row.getRowNum());
                		String item = readString(row, col);
                		
                		if(row.getRowNum() ==0 && item ==null){
                			allRead = true;
                			break;
                		}                		
                		if(row.getRowNum() == 1){
		                	neuronTypeName = item;
		                }		                
		                if(row.getRowNum() == 2){
		                	uniqueID = item;
		                }		                
		                if(row.getRowNum()<150){
		                	//populate 'a' trace		                	
		                	 if(row.getRowNum() == ExcelLabel.J_class.getRowIdx()){// needs special processing
				                	patternClass = item;
				                	if(patternClass.equals("-")||patternClass.equals("*No_data")){
				                		patternClass = "EMPTY";
				                	}	
				                	SpikePatternClass _class = new SpikePatternClass(patternClass, ".");    
				                	trace.setSpikePatternClass(_class);
				             }
				            	 trace.addData(label, item);
				             
		                }
		               // System.out.println(rowIdx);
		                if(row.getRowNum()==150){
		                	String nISI = trace.getMappedData().get(ExcelLabel.N_ISI);
		                	if(nISI==null || nISI.equals("EMPTY")){
		                		continue;
		                	}
		                	int nISI_int = (int)Double.parseDouble(nISI);
		                	//System.out.println(nISI_int);
		                	for(int i=0;i<nISI_int;i++)
		                	{		                		
		                		item = readString(row, col);		                	
		                		if(item.equals("EMPTY") )
		                		{
		                			System.out.println( "col: "+col+", row: "+row.getRowNum() +", nISI: "+nISI_int + ",uniqueID: "+uniqueID);
		                			
		                		}
		                		trace.addISI(Double.parseDouble(item));
		                		row = rowIterator.next();	
		                	}		                	
		                }
	                }
		            if(allRead){
		            	break;
		            }
		            NeuronType neuronType = new NeuronType(neuronTypeName, uniqueID);  		            
                	
					if(map.containsKey(neuronType)){
                		List<SpikePatternTrace> traces = map.get(neuronType);
                		traces.add(trace);
                	}else{
                		List<SpikePatternTrace> traces = new ArrayList<>();
                		traces.add(trace);
                		map.put(neuronType, traces);
                	}
	            }  
	            //close file input stream
	            fis.close();
	             
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	         
	       return map;
	    }

	   
	    private static String readString(Row row, int colIdx){
	    	String item = null;
	    	try{
		    	Cell cell = row.getCell(colIdx);		
		    	if(row.getRowNum()==0 && (cell == null || cell.getCellType()==cell.CELL_TYPE_BLANK)){
	            	return item;
	            }
	            if(row.getRowNum()!=0 && (cell == null || cell.getCellType()==cell.CELL_TYPE_BLANK)){
	            	return "EMPTY";
	            }
	            if(cell.getCellType() == cell.CELL_TYPE_ERROR){
	            	return "ERR";
	        	}            
	        	if(cell.getCellType()==cell.CELL_TYPE_NUMERIC){
	        		item = String.valueOf(cell.getNumericCellValue()) ;
	        	}else{
	        		item = cell.getStringCellValue();
	        	}
        	}catch(IllegalStateException e){
        		System.out.println(row.getRowNum() +", "+colIdx);
        		e.printStackTrace();
        	}
	    	//System.out.println(row.getRowNum() +", "+colIdx+ "\t:"+item);
            return item;
	    }
	    
	    public static Map<NeuronType, List<SpikePatternTrace>> fetchSingleBehaviorTypes(Map<NeuronType, List<SpikePatternTrace>> mp){
	    	Map<NeuronType, List<SpikePatternTrace>> singleBehaviorTypes = new HashMap<>();
	    	Iterator it = mp.entrySet().iterator();
	        while (it.hasNext()) {
	            Map.Entry pair = (Map.Entry)it.next();
	            NeuronType nt = (NeuronType) pair.getKey();	           
	            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();
	            if(isSingleBehavior(traces)){
	            	singleBehaviorTypes.put(nt, traces);
	            }
	        }
	        return singleBehaviorTypes;	            
	    }
	    
	    public static Map<NeuronType, List<SpikePatternTrace>> fetchSubTypes(Map<NeuronType, List<SpikePatternTrace>> mp){
	    	Map<NeuronType, List<SpikePatternTrace>> singleBehaviorTypes = new HashMap<>();
	    	Iterator it = mp.entrySet().iterator();
	        while (it.hasNext()) {
	            Map.Entry pair = (Map.Entry)it.next();
	            NeuronType nt = (NeuronType) pair.getKey();	           
	            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();
	            if(isSingleBehavior(traces)){
	            	singleBehaviorTypes.put(nt, traces);
	            }
	        }
	        return singleBehaviorTypes;	            
	    }
	    
	    public static Map<NeuronType, List<SpikePatternTrace>> fetchMultiBehaviorTypes(Map<NeuronType, List<SpikePatternTrace>> mp){
	    	Map<NeuronType, List<SpikePatternTrace>> multiBehaviorTypes = new HashMap<>();
	    	Iterator it = mp.entrySet().iterator();
	        while (it.hasNext()) {
	            Map.Entry pair = (Map.Entry)it.next();
	            NeuronType nt = (NeuronType) pair.getKey();	           
	            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();
	            if(!isSingleBehavior(traces)){
	            	multiBehaviorTypes.put(nt, traces);
	            }
	        }
	        return multiBehaviorTypes;	            
	    }
	    
	    public static int countEmptyEvidenceNeuronTypes(Map<NeuronType, List<SpikePatternTrace>> mp){
	    	int cnt =0;
	    	Iterator it = mp.entrySet().iterator();
	        while (it.hasNext()) {
	            Map.Entry pair = (Map.Entry)it.next();
	            NeuronType nt = (NeuronType) pair.getKey();	           
	            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();
	            boolean empty = true;
	            for(SpikePatternTrace _trace:traces){
	            	if(!_trace.getPatternClass().contains(SpikePatternComponent.EMPTY)){
	            		empty = false;
	            	}
	            }
	            if(empty){
	            	cnt += 1;
	            }
	        }
	        return cnt;	            
	    }
	    
	    public static List<SpikePatternTrace> fetchSingleBehaviorTraces(Map<NeuronType, List<SpikePatternTrace>> mp){
	    	List<SpikePatternTrace> singleBehaviorTraces = new ArrayList<>();
	    	Iterator it = mp.entrySet().iterator();
	        while (it.hasNext()) {
	            Map.Entry pair = (Map.Entry)it.next();
	            NeuronType nt = (NeuronType) pair.getKey();	           
	            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();
	            for(SpikePatternTrace _trace: traces){
	            	if(isSingleTypeTrace(_trace)){
	            		singleBehaviorTraces.add(_trace);
	            	}
	            }
	        }
	        return singleBehaviorTraces;	            
	    }
	    
	    public static List<SpikePatternTrace> fetchMultipleBehaviorTraces(Map<NeuronType, List<SpikePatternTrace>> mp){
	    	List<SpikePatternTrace> behaviorTraces = new ArrayList<>();
	    	Iterator it = mp.entrySet().iterator();
	        while (it.hasNext()) {
	            Map.Entry pair = (Map.Entry)it.next();
	            NeuronType nt = (NeuronType) pair.getKey();	           
	            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();
	            for(SpikePatternTrace _trace: traces){
	            	if(isMultiBehaviorTrace(_trace)){
	            		behaviorTraces.add(_trace);
	            	}
	            }
	        }
	        return behaviorTraces;	            
	    }
	    
	    
	    public static List<SpikePatternTrace> fetchSubTypeTraces(Map<NeuronType, List<SpikePatternTrace>> mp){
	    	List<SpikePatternTrace> behaviorTraces = new ArrayList<>();
	    	Iterator it = mp.entrySet().iterator();
	        while (it.hasNext()) {
	            Map.Entry pair = (Map.Entry)it.next();
	            NeuronType nt = (NeuronType) pair.getKey();	           
	            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();
	            for(SpikePatternTrace _trace: traces){
	            	if(isSubTypeTrace(_trace)){
	            		behaviorTraces.add(_trace);
	            	}
	            }
	        }
	        return behaviorTraces;	            
	    }
	    
	    public static List<SpikePatternTrace> fetchSubTypeOrMultiBehaviorTraces(Map<NeuronType, List<SpikePatternTrace>> mp){
	    	List<SpikePatternTrace> behaviorTraces = new ArrayList<>();
	    	Iterator it = mp.entrySet().iterator();
	        while (it.hasNext()) {
	            Map.Entry pair = (Map.Entry)it.next();
	            NeuronType nt = (NeuronType) pair.getKey();	           
	            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();
	            for(SpikePatternTrace _trace: traces){
	            	if(isSubtypesORMultiBehaviorTrace(_trace)){
	            		behaviorTraces.add(_trace);
	            	}
	            }
	        }
	        return behaviorTraces;	            
	    }
	    
	    public static List<SpikePatternTrace> fetchSubTypeANDMultiBehaviorTraces(Map<NeuronType, List<SpikePatternTrace>> mp){
	    	List<SpikePatternTrace> behaviorTraces = new ArrayList<>();
	    	Iterator it = mp.entrySet().iterator();
	        while (it.hasNext()) {
	            Map.Entry pair = (Map.Entry)it.next();
	            NeuronType nt = (NeuronType) pair.getKey();	           
	            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();
	            for(SpikePatternTrace _trace: traces){
	            	if(isSubtypesANDMultiBehaviorTrace(_trace)){
	            		behaviorTraces.add(_trace);
	            	}
	            }
	        }
	        return behaviorTraces;	            
	    }
	    
	    public static Map<NeuronType, List<SpikePatternTrace>> fetchAllByRegion(Map<NeuronType, List<SpikePatternTrace>> mp, Region rg){
	    	Map<NeuronType, List<SpikePatternTrace>> byRegion = new HashMap<>();
	    	Iterator it = mp.entrySet().iterator();
	        while (it.hasNext()) {
	            Map.Entry pair = (Map.Entry)it.next();
	            NeuronType nt = (NeuronType) pair.getKey();	           
	            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();
	            if(nt.getRegion().equals(rg)){
	            	byRegion.put(nt, traces);
	            }
	        }
	        return byRegion;	            
	    }
	    
	    
	    
	    /*
	     * search by sp type. (at least 'one' of the traces should be of type.
	     */
	    public static Map<NeuronType, List<SpikePatternTrace>> fetchAllByClass(Map<NeuronType, List<SpikePatternTrace>> mp, SpikePatternClass _class){
	    	Map<NeuronType, List<SpikePatternTrace>> byClass = new HashMap<>();
	    	Iterator it = mp.entrySet().iterator();
	        while (it.hasNext()) {
	            Map.Entry pair = (Map.Entry)it.next();
	            NeuronType nt = (NeuronType) pair.getKey();	           
	            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();
	            boolean found = false;
	            for(SpikePatternTrace _trace: traces){
	            	if(_trace.getPatternClass().equals(_class)){
	            		found = true;
	            	}
	            }
	            if(found)
	            	byClass.put(nt, traces);
	        }
	        return byClass;	            
	    }
	    
	    /*
	     * search by sp type: return only traces!
	     */
	    public static List<SpikePatternTrace> fetchTracesByClass(Map<NeuronType, List<SpikePatternTrace>> mp, SpikePatternClass _class){
	    	List<SpikePatternTrace> byClass = new ArrayList<>();
	    	Iterator it = mp.entrySet().iterator();
	        while (it.hasNext()) {
	            Map.Entry pair = (Map.Entry)it.next();
	            NeuronType nt = (NeuronType) pair.getKey();	           
	            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();	           
	            for(SpikePatternTrace _trace: traces){
	            	if(_trace.getPatternClass().equals(_class)){
	            		byClass.add(_trace);
	            	}
	            }	            
	        }
	        return byClass;	            
	    }
	    public static List<SpikePatternTrace> fetchTracesByComponent(Map<NeuronType, List<SpikePatternTrace>> mp, 
	    		SpikePatternComponent component,
	    		boolean considerAltClass){
	    	List<SpikePatternTrace> byClass = new ArrayList<>();
	    	Iterator it = mp.entrySet().iterator();
	        while (it.hasNext()) {
	            Map.Entry pair = (Map.Entry)it.next();
	            NeuronType nt = (NeuronType) pair.getKey();	           
	            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();	           
	            for(SpikePatternTrace _trace: traces){
	            	if(_trace.getPatternClass().contains(component)){
	            		byClass.add(_trace);
	            	}else{
	            		if(considerAltClass && 
	            				_trace.getMappedData().get(ExcelLabel.ALT_CLASS).contains(component.name())){
	            			byClass.add(_trace);
	            		}
	            	}
	            }	            
	        }
	        return byClass;	            
	    }
	    
	    public static Map<NeuronType, List<SpikePatternTrace>> fetchNeuronTypesByComponent(Map<NeuronType, List<SpikePatternTrace>> mp, 
	    		SpikePatternComponent component,
	    		boolean considerAltClass){
	    	
	    	Map<NeuronType, List<SpikePatternTrace>> neuronTypes = new HashMap<>();
	    	
	    	Iterator it = mp.entrySet().iterator();
	        while (it.hasNext()) {
	            Map.Entry pair = (Map.Entry)it.next();
	            NeuronType nt = (NeuronType) pair.getKey();	           
	            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();	           
	            for(SpikePatternTrace _trace: traces){
	            	if(_trace.getPatternClass().contains(component)){
	            		neuronTypes.put(nt, traces);
	            		break;
	            	}else{
	            		if(considerAltClass && 
	            				_trace.getMappedData().get(ExcelLabel.ALT_CLASS).contains(component.name())){
	            			neuronTypes.put(nt, traces);
		            		break;
	            		}
	            	}
	            }	            
	        }
	        return neuronTypes;	            
	    }
	    
	    private static Map<String, List<SpikePatternTrace>> groupTraces(List<SpikePatternTrace> traces){
	    	 Map<String, List<SpikePatternTrace>> grouped = new HashMap<>();
	    	 for(SpikePatternTrace _trace: traces){
	    		 String subTypeID = _trace.getMappedData().get(ExcelLabel.subtypes);
	    		 if(grouped.containsKey(subTypeID)){
	    			 grouped.get(subTypeID).add(_trace);
	    		 }else{
	    			 List<SpikePatternTrace> _traces = new ArrayList<>();
	    			 _traces.add(_trace);
	    			 grouped.put(subTypeID, _traces);
	    		 }
	    	 }
	    	return grouped;
	    }
	    
	    /*
	     * this function receives a (grouped) map with key "subtype id", and not the neurontype ID
	     */
	 /*   public PhenoTypeCategory identifyPhenoTypeCategory(Map<String, List<SpikePatternTrace>> singleNeuronTypeTraces){
	    	Iterator it = singleNeuronTypeTraces.entrySet().iterator();	    	
	    	PhenoTypeCategory category = null;
	    	
	    	boolean single_behavior = false;
	    	boolean sub_types = false;
	    	boolean multi_behavior = false;
	    	boolean sub_types_or_multi_behavior = false;
	    	
	    	if(singleNeuronTypeTraces.size()==1) {
	    		 boolean sameClass = true;
	    		  while (it.hasNext()) {
	  	            Map.Entry pair = (Map.Entry)it.next();
	  	            String subTypeID = (String)pair.getKey();
	  	            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();
	  	            SpikePatternTrace _trace0 = traces.get(0);
	  	           
	  	            for(SpikePatternTrace _trace: traces){
	  	            	if(!_trace0.getPatternClass().equals(_trace.getPatternClass())){
	  	            		sameClass = false;
	  	            	}
	  	            }
	    		  }	  	            
	  	           
            	 if(sameClass){
            		 category = PhenoTypeCategory.SINGLE_BEHAVIOR_TYPE;
 		         }else
 		        	 category = PhenoTypeCategory.MULTI_BEHAVIOR;
            	 
            	 return category;
  	         }else{
  	        	sub_types = true;
  	         }
	    	
	    	
    	}
	 */
	    private static boolean isSingleBehavior(List<SpikePatternTrace> traces){
	    	SpikePatternTrace firstTrace = traces.get(0);	    	
	    	for(SpikePatternTrace trace: traces){
	    		if(!firstTrace.getPatternClass().equals(trace.getPatternClass()) 
	    				|| firstTrace.getPatternClass().contains(SpikePatternComponent.EMPTY)) return false;
	    	}	    	
	    	return true;
	    }
	    
	    private static boolean isSubType(List<SpikePatternTrace> traces){
	    	SpikePatternTrace firstTrace = traces.get(0);	    	
	    	for(SpikePatternTrace trace: traces){
	    		if(!firstTrace.getPatternClass().equals(trace.getPatternClass()) 
	    				|| firstTrace.getPatternClass().contains(SpikePatternComponent.EMPTY)) return false;
	    	}	    	
	    	return true;
	    }
	    
	    private static boolean isSingleTypeTrace(SpikePatternTrace trace){
	    	if(trace.getMappedData().get(ExcelLabel.Phen_cat).equals("Single type"))
	    		return true;
	    	return false;
	    }
	    
	    private static boolean isSubTypeTrace(SpikePatternTrace trace){
	    	if(trace.getMappedData().get(ExcelLabel.Phen_cat).equals("Subtypes"))
	    		return true;
	    	return false;
	    }
	    
	    private static boolean isMultiBehaviorTrace(SpikePatternTrace trace){
	    	if(trace.getMappedData().get(ExcelLabel.Phen_cat).equals("Single type WITH multi-behavior"))
	    		return true;
	    	return false;
	    }
	    
	    private static boolean isSubtypesORMultiBehaviorTrace(SpikePatternTrace trace){
	    	if(trace.getMappedData().get(ExcelLabel.Phen_cat).equals("Subtypes OR multi-behavior"))
	    		return true;
	    	return false;
	    }
	    
	    private static boolean isSubtypesANDMultiBehaviorTrace(SpikePatternTrace trace){
	    	if(trace.getMappedData().get(ExcelLabel.Phen_cat).equals("Subtypes AND multi-behavior"))
	    		return true;
	    	return false;
	    }
	    public static void main(String[] args) {
			Map<NeuronType, List<SpikePatternTrace>> map = readExcelData();
			//map = fetchSingleBehaviorTypes(map);
			SpikePatternClass _class = new SpikePatternClass("PSTUT", ".");
			map = fetchAllByClass(map, _class);
						
			ExcelLabel[] labelsToPrint = new ExcelLabel[] {};
			printMap(map);//
	    }
	    
	    /*
	     * Only print neuron type and sp classes associated with it!
	     */
		 public static void printMap( Map<NeuronType, List<SpikePatternTrace>> mp) {
		        Iterator it = mp.entrySet().iterator();
		        while (it.hasNext()) {
		            Map.Entry pair = (Map.Entry)it.next();
		            NeuronType nt = (NeuronType) pair.getKey();
		            System.out.print(nt.getName()+"\t\t\t");
		          //  nt.display();
		            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();
		            for(SpikePatternTrace _trace: traces){
		            	_trace.getPatternClass().display();
		            	System.out.print("\t");
		            }	         
		           System.out.println();
		        }
		  }
		 
		 public static void printMapWithISIs( Map<NeuronType, List<SpikePatternTrace>> mp) {
		        Iterator it = mp.entrySet().iterator();
		        while (it.hasNext()) {
		            Map.Entry pair = (Map.Entry)it.next();
		            NeuronType nt = (NeuronType) pair.getKey();
		            System.out.print(nt.getName()+"\t\t\t");
		          //  nt.display();
		            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();
		            for(SpikePatternTrace _trace: traces){
		            	System.out.println("\t");
		            	_trace.getPatternClass().display(); 
		            	System.out.print(_trace.getMappedData().get(ExcelLabel.N_ISI));
		            	System.out.print("\t"+_trace.getISIs());
		             }	         
		           System.out.println();
		        }
		  }
		 /*
		     * print neuron types and constraints for model
		     */
			 public static void printMap( Map<NeuronType, List<SpikePatternTrace>> mp, ExcelLabel[] labelsToPrint) {
			        Iterator it = mp.entrySet().iterator();
			        while (it.hasNext()) {
			            Map.Entry pair = (Map.Entry)it.next();
			            NeuronType nt = (NeuronType) pair.getKey();
			            
			          //  nt.display();
			            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();
			            for(SpikePatternTrace _trace: traces){
			            	// System.out.print(nt.getUniqueID()+"\t");
			            	//System.out.print(nt.getRegion()+"\t");
			            	//System.out.print(nt.getName()+"\t");				           				            
			            	//_trace.getPatternClass().display();  	System.out.print("\t");
			            	
			            	for(ExcelLabel label: labelsToPrint){
			            		String item = _trace.getMappedData().get(label);
			            		System.out.print(item+",");
			            	}
			            	 System.out.println();
			            }	         
			          
			        }
			  }
			 
			 public static void printTrace(SpikePatternTrace _trace, ExcelLabel[] labelsToPrint){
				 for(ExcelLabel label: labelsToPrint){
	            		String item = _trace.getMappedData().get(label);
	            		System.out.print(item+"\t");
	            	}
			 }
			 
			 public static void printTraces( List<SpikePatternTrace> traces, ExcelLabel[] labelsToPrint) {
		            for(SpikePatternTrace _trace: traces){			            	
		            	 printTrace(_trace, labelsToPrint);
		            	 System.out.println();
		            }	
			  }
			 
			 public static void printTracesIncludingOTHERtraces( List<SpikePatternTrace> traces, ExcelLabel[] labelsToPrint) {
				 for(SpikePatternTrace _trace: traces){			            	
	            	 printTrace(_trace, labelsToPrint);
	            	 System.out.println();
				 	}	
			  }
			 
			 
			 
			 public static List<SpikePatternTrace> fetchSpikePatternTracesByUniqueID(Map<NeuronType, List<SpikePatternTrace>> mp, String uniqueID){
				return mp.get(new NeuronType("", uniqueID));
				 /* Iterator it = mp.entrySet().iterator();
				 while (it.hasNext()) {
			            Map.Entry pair = (Map.Entry)it.next();
			            NeuronType nt = (NeuronType) pair.getKey();
			            if(nt.getUniqueID().equals(uniqueID)){
			            	return (List<SpikePatternTrace>)pair.getValue();
			            }
				 }
				 System.out.println("unique ID not found in map");
				 return null;*/
			 }
			 public static Map<NeuronType, List<SpikePatternTrace>> fetchAsMapByUniqueID(Map<NeuronType, List<SpikePatternTrace>> mp, String uniqueID){
				 Map<NeuronType, List<SpikePatternTrace>> mpSingleItem = new TreeMap<>();
					 Iterator it = mp.entrySet().iterator();
					 while (it.hasNext()) {
				            Map.Entry pair = (Map.Entry)it.next();
				            NeuronType nt = (NeuronType) pair.getKey();
				            if(nt.getUniqueID().equals(uniqueID)){
				            	mpSingleItem.put(nt, (List<SpikePatternTrace>)pair.getValue());
				            	return mpSingleItem;
				            }
					 }
					 System.out.println("unique ID not found in map");
					 return null;
				 }
			 /*
			    * print neuron types and new classes
			     */
				 public static void printMapWNewClasses( Map<NeuronType, List<SpikePatternTrace>> mp) {
				        Iterator it = mp.entrySet().iterator();
				        while (it.hasNext()) {
				            Map.Entry pair = (Map.Entry)it.next();
				            NeuronType nt = (NeuronType) pair.getKey();
				            
				          //  nt.display();
				            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();
				            for(SpikePatternTrace _trace: traces){
				            	 System.out.print(nt.getUniqueID()+"\t");
				            	System.out.print(nt.getRegion()+"\t");
				            	System.out.print(nt.getName()+"\t");				           				            
				            	_trace.getPatternClass().display();  	System.out.print("\t");
				            	
				            	float[] f = new float[3];
				            	float[] f_crit= new float[3];
				            	float[] p = new float[3];
				            	float[] p_uv = new float[3];
				            	
				            		
				            		f[0] = Float.valueOf(_trace.getMappedData().get(ExcelLabel.F_12));
				            		f[1] = Float.valueOf(_trace.getMappedData().get(ExcelLabel.F_23));
				            		f[2] = Float.valueOf(_trace.getMappedData().get(ExcelLabel.F_34));
				            		
				            		f_crit[0] = Float.valueOf(_trace.getMappedData().get(ExcelLabel.F_12c));
				            		f_crit[1] = Float.valueOf(_trace.getMappedData().get(ExcelLabel.F_23c));
				            		f_crit[2] = Float.valueOf(_trace.getMappedData().get(ExcelLabel.F_34c));				            		
				            						            		
				            		p[0] = Float.valueOf(_trace.getMappedData().get(ExcelLabel.P_12));
				            		p[1] = Float.valueOf(_trace.getMappedData().get(ExcelLabel.P_23));
				            		p[2] = Float.valueOf(_trace.getMappedData().get(ExcelLabel.P_34));
				            		
				            		p_uv[0] = Float.valueOf(_trace.getMappedData().get(ExcelLabel.P_12uv));
				            		p_uv[1] = Float.valueOf(_trace.getMappedData().get(ExcelLabel.P_23uv));
				            		p_uv[2] = Float.valueOf(_trace.getMappedData().get(ExcelLabel.P_34uv));
				            		
				            		System.out.print(reClassify(f,f_crit, p, p_uv)+"\t");
				            	
				            	 System.out.println();
				            }	         
				          
				        }
				  }
			    
			 private static String reClassify(float[] f, float[] fcrit, float[] p, float[] p_uv){
				 String str = "NASP";
				 
				 //idx = 0 --> 1 to 2 parm
				 if(isSignificantImpr(f[0], fcrit[0], p[0], p_uv[0], 0.05f)){
					 str = "ASP";
				 }else
					 return str;
				 
				 if(isSignificantImpr(f[1], fcrit[1], p[1], p_uv[1], 0.025f)){
					 str = "ASP.NASP";
				 }else
					 return str;
				 
				 if(isSignificantImpr(f[2], fcrit[2], p[2], p_uv[2], 0.0125f)){
					 str = "ASP.ASP";
				 }else
					 return str;
				 
				 return str;
			 }
			 private static boolean isSignificantImpr(float f, float f_c, float p, float p_uv, float threshold){
				 if(f<f_c){
					 if(p<threshold)
						 return true;
					 else
						 return false;
				 }else{
					 if(p_uv<threshold)
						 return true;
					 else 
						 return false;
				 }
			 }

}
