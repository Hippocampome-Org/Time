package ec.app.izhikevich.util.forportal;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ec.app.izhikevich.ModelEvaluatorWrapper;
import ec.app.izhikevich.util.ModelDBInterface;

public class PortalInterface {
	//private static final boolean IS_MC=false;
	private static int NSUBPOPS;
	private static int NTHPOP;
	private static int N_HEADER_ROWS=3;
	static {
	
	NSUBPOPS=1;
	NTHPOP=0;
		
	}
	
	public static void writeCSV_for_STAT_CORR(List<ModelDataStructure> mdsList_sc,  String fileName) {
		 try {     		
			boolean append = false;
			FileWriter fw = new FileWriter(fileName, append);
			fw.write(mdsList_sc.get(0).getHeaderCSVString_for_STAT_CORR()+"\n");
			//preferred condition mock-up : must remove later
			boolean isNewUniqueId;
			String pre_uniqueID = "";
			for(ModelDataStructure mds: mdsList_sc) {	
				
				if(mds.uniqueID.equals(pre_uniqueID))
					continue;
				
				pre_uniqueID = mds.uniqueID;
				
				System.out.print(mds.uniqueID+"\t"+mds.name+"\t"+mds.neuronSubtypeID+"\t");				
				
				fw.write(mds.getCSVString_For_STAT_CORR());		
				
				fw.write("\n");	
			}			
			fw.flush();
			fw.close();
				
   } catch (IOException e) {
  	 e.printStackTrace();
		}
	}
	public static void writeCSV(List<ModelDataStructure> mdsList_sc,List<ModelDataStructure> mdsList_mc,  String fileName) {
		 try {     		
			boolean append = false;
			FileWriter fw = new FileWriter(fileName, append);
			fw.write(mdsList_sc.get(0).getHeaderCSVString()+"\n");
			//preferred condition mock-up : must remove later
			boolean isNewUniqueId;
			String pre_uniqueID = "";
			for(ModelDataStructure mds: mdsList_sc) {	
				/*
				 * for mock up preferred
				 */
				if(mds.uniqueID.equals(pre_uniqueID))
					isNewUniqueId = false;
				else
					isNewUniqueId = true;		
				
				pre_uniqueID = mds.uniqueID;
				
				if(isNewUniqueId)
					mds.preferredCondition="Y";
				else
					mds.preferredCondition="N";
				
				System.out.print(mds.uniqueID+"\t"+mds.name+"\t"+mds.neuronSubtypeID+"\t");
				for(int i=0;i<mds.Is.length;i++) {
					System.out.print(mds.Is[i]+"\t");
				}
				System.out.println();
				/*
				 * end of mock up
				 */
				fw.write(mds.getCSVString());		
				for(ModelDataStructure mds_mc: mdsList_mc) {
					if(mds_mc.neuronSubtypeID.equals(mds.neuronSubtypeID)) {
						fw.write(mds_mc.getCSVString());	
						break;
					}
				}
				fw.write("\n");	
			}			
			fw.flush();
			fw.close();
				
    } catch (IOException e) {
   	 e.printStackTrace();
		}
	}
	
	public static List<ModelDataStructure> readFromProgressSheet(String progressSheetfileName, int sheetNo){
		
		List<ModelDataStructure> mdsList = new ArrayList<>();
		String primary_input=null;
		String exp=null;
		int job=-1;
		boolean savePlot = false;
		
		 try {
	            //Create the input stream from the xlsx/xls file	        	
	            FileInputStream fis = new FileInputStream(progressSheetfileName);	             
	            //Create Workbook instance for xlsx/xls file input stream
	            Workbook workbook = null;
	            if(progressSheetfileName.toLowerCase().endsWith("xlsx")){
	                workbook = new XSSFWorkbook(fis);
	            }else if(progressSheetfileName.toLowerCase().endsWith("xls")){
	                workbook = new HSSFWorkbook(fis);
	            }  
	            
	            Sheet sheet = workbook.getSheetAt(sheetNo);                 
	            Iterator<Row> rowIterator = sheet.iterator();    
	            Row row = null;
	            while(rowIterator.hasNext()){
        			//rowIdx++;
	                row = rowIterator.next();
	                if(row.getRowNum()<N_HEADER_ROWS) { //headerrow
	                	continue;
	                }
	                String name = readString(row, ExcelLabel.NAME.getColIdx());
	                int completed = readInt(row, ExcelLabel.N_COMPLETED.getColIdx());
	                
	                if(completed<0) {
	                	continue;
	                }
	                
	                String uniqueID = readString(row, ExcelLabel.UNIQUE_ID.getColIdx());
	                if(uniqueID==null || uniqueID.isEmpty()) {
	                	System.out.println("Fill unique ID for completed >0\t"+ name);
	                	System.exit(-1);
	                }
	                
	                for(int i=0;i<completed;i++) { //one row for each completed subtype under a neuron type
	                	int offset=i*3;
	                	 primary_input = readString(row, offset+ExcelLabel.PRIMARY_INPUT_1_START_IDX.getColIdx());
	 	                exp = readString(row, offset+ExcelLabel.PRIMARY_INPUT_1_START_IDX.getColIdx()+1);
	 	                job = readInt(row, offset+ExcelLabel.PRIMARY_INPUT_1_START_IDX.getColIdx()+2);
	 	                
	 	                //System.out.println(completed+"\t"+primary_input+"\t"+exp+"\t"+job);
	 	                
	 	                ModelDBInterface mdbInt = new ModelDBInterface(primary_input, exp, job, savePlot);
	 	                mdbInt.setUniqueID(uniqueID);
	 	                
	 	               ModelDataStructure mds;	 	               
	 	                if(exp.equals("p")) {
	 	                	double[] parms = readParmsDirect(row, job); // job is the offset for direct parm list (so far it's either 0 or 1)
							mds= mdbInt.createModelDataStructure(name, parms );
	 	                }else {
	 	                	//System.out.println(name);
	 	                	mds = mdbInt.createModelDataStructure(name, NSUBPOPS, NTHPOP);
	 	                }
	 	                
	 	                mds.property.setInputSpikePatternConstraints(ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS);
	 	                mdsList.add(mds);
	                }    
        		}
	            
	            fis.close();
		 }catch (IOException e) {
	            e.printStackTrace();
	     }        
		
		
		return mdsList;
	}
	
public static List<ModelDataStructure> readFromProgressSheet_ManySampleModels(String progressSheetfileName, int sheetNo, int n_samples){
		
		List<ModelDataStructure> mdsList = new ArrayList<>();
		String primary_input=null;
		String exp=null;
		int job=-1;
		boolean savePlot = false;
		
		 try {
	            //Create the input stream from the xlsx/xls file	        	
	            FileInputStream fis = new FileInputStream(progressSheetfileName);	             
	            //Create Workbook instance for xlsx/xls file input stream
	            Workbook workbook = null;
	            if(progressSheetfileName.toLowerCase().endsWith("xlsx")){
	                workbook = new XSSFWorkbook(fis);
	            }else if(progressSheetfileName.toLowerCase().endsWith("xls")){
	                workbook = new HSSFWorkbook(fis);
	            }  
	            
	            Sheet sheet = workbook.getSheetAt(sheetNo);                 
	            Iterator<Row> rowIterator = sheet.iterator();    
	            Row row = null;
	            while(rowIterator.hasNext()){
        			//rowIdx++;
	                row = rowIterator.next();
	                if(row.getRowNum()<N_HEADER_ROWS) { //headerrow
	                	continue;
	                }
	                String name = readString(row, ExcelLabel.NAME.getColIdx());
	                int completed = readInt(row, ExcelLabel.N_COMPLETED.getColIdx());
	                
	                if(completed<0) {
	                	continue;
	                }
	                
	                String uniqueID = readString(row, ExcelLabel.UNIQUE_ID.getColIdx());
	                if(uniqueID==null || uniqueID.isEmpty()) {
	                	System.out.println("Fill unique ID for completed >0\t"+ name);
	                	System.exit(-1);
	                }
	                
	                for(int i=0;i<completed;i++) { //one row for each completed subtype under a neuron type
	                	int offset=i*3;
	                	 primary_input = readString(row, offset+ExcelLabel.PRIMARY_INPUT_1_START_IDX.getColIdx());
	 	                exp = readString(row, offset+ExcelLabel.PRIMARY_INPUT_1_START_IDX.getColIdx()+1);
	 	                job = readInt(row, offset+ExcelLabel.PRIMARY_INPUT_1_START_IDX.getColIdx()+2);
	 	                
	 	                //System.out.println(completed+"\t"+primary_input+"\t"+exp+"\t"+job);
	 	                
	 	                ModelDBInterface mdbInt = new ModelDBInterface(primary_input, exp, job, savePlot);
	 	                mdbInt.setUniqueID(uniqueID);	 	                
	 	                	 	               
	 	                if(exp.equals("p")) {
	 	                	double[] parms = readParmsDirect(row, job); // job is the offset for direct parm list (so far it's either 0 or 1)
	 	                	ModelDataStructure mds= mdbInt.createModelDataStructure(name, parms );
							
							mds.property.setInputSpikePatternConstraints(ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS);
		 	                mdsList.add(mds);
		 	                
	 	                }else {
	 	                	List<ModelDataStructure> mdss = mdbInt.createModelDataStructure_manySampleModels(name, n_samples);
	 	                	
	 	                	for(ModelDataStructure mds: mdss) {
	 	                		mds.property.setInputSpikePatternConstraints(ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS);
			 	                mdsList.add(mds);
	 	                	}	 	                	
		 	                
	 	                }
	 	                
	 	                
	                }    
        		}
	            
	            fis.close();
		 }catch (IOException e) {
	            e.printStackTrace();
	     }        
		
		
		return mdsList;
	}

	public static List<ModelDataStructure> readFromProgressSheet_mc(String progressSheetfileName, int sheetNo){
		
		List<ModelDataStructure> mdsList = new ArrayList<>();
		String primary_input=null;
		String exp=null;
		int job=-1;
		boolean savePlot = false;
		
		 try {
	            //Create the input stream from the xlsx/xls file	        	
	            FileInputStream fis = new FileInputStream(progressSheetfileName);	             
	            //Create Workbook instance for xlsx/xls file input stream
	            Workbook workbook = null;
	            if(progressSheetfileName.toLowerCase().endsWith("xlsx")){
	                workbook = new XSSFWorkbook(fis);
	            }else if(progressSheetfileName.toLowerCase().endsWith("xls")){
	                workbook = new HSSFWorkbook(fis);
	            }  
	            
	            Sheet sheet = workbook.getSheetAt(sheetNo);                 
	            Iterator<Row> rowIterator = sheet.iterator();    
	            Row row = null;
	            while(rowIterator.hasNext()){
        			//rowIdx++;
	                row = rowIterator.next();
	                if(row.getRowNum()<N_HEADER_ROWS) { //headerrow
	                	continue;
	                }
	                String name = readString(row, ExcelLabel.NAME.getColIdx());
	                int completed = readInt(row, ExcelLabel.N_COMPLETED.getColIdx());
	                
	                if(completed<0) {
	                	continue;
	                }
	                
	                String uniqueID = readString(row, ExcelLabel.UNIQUE_ID.getColIdx());
	                if(uniqueID==null || uniqueID.isEmpty()) {
	                	System.out.println("Fill unique ID for completed >0\t"+ name);
	                	System.exit(-1);
	                }
	                
	                for(int i=0;i<completed;i++) { //one row for each completed subtype under a neuron type
	                	int offset=i*3;
	                	 primary_input = readString(row, offset+ExcelLabel.PRIMARY_INPUT_1_START_IDX.getColIdx());
	 	                exp = readString(row, offset+ExcelLabel.PRIMARY_INPUT_1_START_IDX.getColIdx()+1);
	 	                job = readInt(row, offset+ExcelLabel.PRIMARY_INPUT_1_START_IDX.getColIdx()+2);
	 	              
	 	                if(exp==null) {
	 	                	 System.out.println(completed+"\t"+primary_input+"\t"+exp+"\t"+job);
	 	                }
	 	               
	 	                ModelDBInterface mdbInt = new ModelDBInterface(primary_input, exp, job, savePlot);
	 	                mdbInt.setUniqueID(uniqueID);
	 	                ModelDataStructure mds;
	 	               
	 	              if(exp.equals("sp")) //  if(exp.equals("p")) 
	 	               {	 	            	  
		 	            	 NSUBPOPS=10;
		 	            	 NTHPOP = job;
	 	            	 
		 	            	if(uniqueID.equals("1-009")) {
		 	            		NSUBPOPS=5;
		 	            	}
		 	            	
		 	            	if(uniqueID.equals("2-000") && (i==2 || i==3)) {
		 	            		NSUBPOPS=5;
		 	            	}
	 	            	
		 	            	 mds = mdbInt.createModelDataStructure(name, NSUBPOPS, NTHPOP);
		 	            	 
	 	                }else {	 	                	
	 	                	 if(exp.equals("p")) {
	 	 	                	double[] parms = readParmsDirect(row, job); // job is the offset for direct parm list (so far it's either 0 or 1)
	 							mds= mdbInt.createModelDataStructure_mc(name, parms );
	 	 	                }else {	 	         //"2c0", "3c0" denoting internal simulator       	 
		 	                	 NSUBPOPS=1;
			 	            	 NTHPOP = 0;
			 	            	 mds = mdbInt.createModelDataStructure(name, NSUBPOPS, NTHPOP);
	 	 	                }
	 	                }
	 	               
	 	                mds.property.setInputSpikePatternConstraints(ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS);
	 	                mdsList.add(mds);
	                }    
        		}
	            
	            fis.close();
		 }catch (IOException e) {
	            e.printStackTrace();
	     }        
		
		
		return mdsList;
	}
	private static String readString(Row row, int colIdx){
		//System.out.println(row.getRowNum() +", "+colIdx);
    	String item = null;
    	try{
	    	Cell cell = row.getCell(colIdx);		    	
	    	if(cell == null || cell.getCellType()==cell.CELL_TYPE_BLANK){
            	return item;
            }
        	if(cell.getCellType()==cell.CELL_TYPE_NUMERIC){
        		item = String.valueOf((int)cell.getNumericCellValue()) ;
        	}else{
        		item = cell.getStringCellValue();
        	}
        	
    	}catch(IllegalStateException e){
    		System.out.println(row.getRowNum() +", "+colIdx);
    		e.printStackTrace();
    	}
        return item;
    }
	private static int readInt(Row row, int colIdx){
		//System.out.println(row.getRowNum() +", "+colIdx);
    	int item = -1;
    	try{
	    	Cell cell = row.getCell(colIdx);
	    	if(cell == null || cell.getCellType()==cell.CELL_TYPE_BLANK){
            	return item;
            }
        	if(cell.getCellType()==cell.CELL_TYPE_NUMERIC){
        		item = (int) cell.getNumericCellValue();
        	}else{
        		item = Integer.valueOf((int) cell.getNumericCellValue());
        	}
        	
    	}catch(IllegalStateException e){
    		System.out.println(row.getRowNum() +", "+colIdx);
    		e.printStackTrace();
    	}
        return item;
    }
	
	private static double[] readParmsDirect(Row row, int parmlistIdx){
		int colIdx=0;
		if(parmlistIdx==0)
			colIdx = ExcelLabel.PARMSET_1_START_IDX.getColIdx();
		if(parmlistIdx==1)
			colIdx = ExcelLabel.PARMSET_2_START_IDX.getColIdx();
		
		double item = -1;
    	List<Double> parms = new ArrayList<>();
    	try{
    		while(true) {
    			Cell cell = row.getCell(colIdx);
    	    	if(cell == null || cell.getCellType()==cell.CELL_TYPE_BLANK){
                	break;
                }
    	    	
            	if(cell.getCellType()==cell.CELL_TYPE_NUMERIC){
            		item = (double) cell.getNumericCellValue();
            	}else{
            		item = Double.valueOf((double) cell.getNumericCellValue());
            	}
            	colIdx++;
            	parms.add(item);
    		}        	
    	}catch(IllegalStateException e){
    		System.out.println(row.getRowNum() +", "+colIdx);
    		e.printStackTrace();
    	}
    	double[] parmsArray = new double[parms.size()];
    	for(int i=0;i<parmsArray.length;i++) {
    		parmsArray[i]=parms.get(i);
    	}
        return parmsArray;
    }
	
	public static void main(String[] args) {
		String fileName = "C:\\Users\\sivav\\Dropbox\\HCO\\MCProgress_v3_15_18.xlsx";
		String opFileName="C:\\Users\\sivav\\Dropbox\\HCO\\OnPortal\\IzhModels_Matrix_v7.csv";
		//String opFileName="C:\\Users\\sivav\\Dropbox\\HCO\\OnPortal\\for_stat_corr\\Izhparms_categorical_v1.csv";
		
	
		System.out.println("*********\nReading...");
		List<ModelDataStructure> mdsList_sc = readFromProgressSheet(fileName, 1);
		List<ModelDataStructure> mdsList_mc = readFromProgressSheet_mc(fileName, 0);

		
		System.out.println("*********\nWriting...");		
		writeCSV(mdsList_sc,mdsList_mc, opFileName); //portal main matrix
		
		//writeCSV_for_STAT_CORR(mdsList_sc, opFileName); //for bernard exact test
	}

}
enum ExcelLabel{
	UNIQUE_ID(1), NAME(2), N_COMP(3), N_MODELS(4), N_COMPLETED(5), 
	
	PRIMARY_INPUT_1_START_IDX(10), PARMSET_1_START_IDX(30), PARMSET_2_START_IDX(45);
	
	private int colIdx;
	ExcelLabel(int col_idx){
		colIdx = col_idx;
	}
	public int getColIdx(){
		return colIdx-1;
	}
}
