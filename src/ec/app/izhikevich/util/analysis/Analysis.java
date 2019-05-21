package ec.app.izhikevich.util.analysis;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ec.app.izhikevich.util.forportal.ModelDataStructure;
import ec.app.izhikevich.util.forportal.PortalInterface;

public class Analysis {
	Map<Region, RegionalProperty> RegionToProperty;
	
	Analysis(){
		RegionToProperty = new HashMap<>();
		RegionToProperty.put(Region.DG, new RegionalProperty());
		RegionToProperty.put(Region.CA3, new RegionalProperty());
		RegionToProperty.put(Region.CA2, new RegionalProperty());
		RegionToProperty.put(Region.CA1, new RegionalProperty());
		RegionToProperty.put(Region.SUB, new RegionalProperty());
		RegionToProperty.put(Region.EC, new RegionalProperty());		
	}
	
	private void populateRegionalProperties(){
		double progress = 0;
		for(ModelDataStructure mds: NeuronPageEntries.mdsList_sc) {
			if(progress%10==0) {
				System.out.println(progress+" models complete!");
			}
			double excit = mds.measureExcitability();
			double excit_rb = mds.measureExcitability_rb();
			
			RegionalProperty regionalProperty = RegionToProperty.get(mds.identifyRegion());
			regionalProperty.mdsList.add(mds);		
			regionalProperty.excitabilityList.add(excit);			
			mds.property.excitability = excit;// redundant, but fine..
			mds.property.excitability_rb = excit_rb;
			//double minFreq = mds.measureMinFreq();
			//regionalProperty.minFreqList.add(minFreq);
			
			progress=progress+1;
			//System.out.println(mds.neuronSubtypeID+"\t"+excit);			
		}
	}	
	
	private void populateRegionalPhenotypes(double I_offset, boolean doRBS, boolean display) {
		Region[] regions = Region.values();
		for(Region reg:regions) {	
			System.out.println(reg.toString());
			//RegionToProperty.get(reg).populatePhenotypes(50d, 20, doRBS);
			RegionToProperty.get(reg).populatePhenotypes_v3(I_offset);
			if(display)
				RegionToProperty.get(reg).displayRegionalPhenotypes();
			/*
			RegionToProperty.get(reg).populateCountOfPhenotypeComponents();
			if(display)
				RegionToProperty.get(reg).displayCountOfPhenotypeComponents();		
				*/	
		}
	}
	
	private void writeForEDA_v1(String fileName) {
		Region[] regions = Region.values();
		
		try {
			FileWriter fw = new FileWriter(fileName+".csv");
			//fw.write(RegionalProperty.getDL_list_for_EDA_header());
			for(Region reg:regions) {
				fw.write(RegionToProperty.get(reg).getDL_list_for_EDA());
				fw.flush();
			}
			fw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}	
	}
	
	private void writeFor_D_ND(String fileName) {
		Region[] regions = Region.values();
		
		try {
			FileWriter fw = new FileWriter(fileName+".csv");
			//fw.write(RegionalProperty.getDL_list_for_EDA_header());
			for(Region reg:regions) {
				
				List<ModelDataStructure> mdsList = RegionToProperty.get(reg).mdsList;
				for(ModelDataStructure mds: mdsList) {
					fw.write(mds.getCSVStringFor_D_ND()+"\n");
					fw.flush();
				}				
			}
			fw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}	
	}
	
	private void populateCountOfUniqueClasses() {
		Region[] regions = Region.values();
		for(Region reg:regions) {			
			RegionToProperty.get(reg).populateCountOfUniqueClasses();
			//RegionToProperty.get(reg).displayCountOfUniqueClasses();
		}
	}
	
	private void write_K_Vt_Exc(String filename) {
		FileWriter fw;
		try {
			fw = new FileWriter(filename+".dat");			
			for(ModelDataStructure mds: NeuronPageEntries.mdsList_sc) {
				fw.write(mds.model.getK()[0]+"\t"+(mds.model.getvT()[0] - mds.model.getvR()[0])+"\t"+mds.property.excitability+"\n");
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	private void displayRegionalModelCounts() {
		Region[] regions = Region.values();
		System.out.println("************************************************");
		for(Region reg:regions) {
			System.out.print(reg.toString()+"\t");
			RegionToProperty.get(reg).displayCountOfModels();
			//RegionToProperty.get(reg).displayExcitabilities();
			System.out.println();
		}
	}
	
	
	private void writeExcitDistAsCSV(String filename) {
		Region[] regions = Region.values();
		try {
			FileWriter fw = new FileWriter(filename+".csv");			
			for(Region reg:regions) {
				fw.write(RegionToProperty.get(reg).getCSVString_excitlist()+"\n");
			}			
			fw.close();
		} catch (IOException e) {			
			e.printStackTrace();
		}		
	}
	
	private  static boolean contains(List<ModelDataStructure> mds_list, ModelDataStructure mds) {
		boolean cont = false;
		for(ModelDataStructure mds_from_list: mds_list) {
			if(mds_from_list.equals(mds)) {
				cont = true;
				break;
			}				
		}
		return cont;
	}
	
	public static List<ModelDataStructure> removeDuplicates(List<ModelDataStructure> mds) {
		List<ModelDataStructure> mds_new = new ArrayList<>();
		
		for(ModelDataStructure mds_from_list: mds) {
			if(!contains(mds_new, mds_from_list)) {
				mds_new.add(mds_from_list);
			}
		}
		return mds_new;
	}
	public static void main(String[] args) {
		String fileName = "C:\\Users\\sivav\\Dropbox\\HCO\\MCProgress_v3_15_18.xlsx";
		
		/*
		 * SC 
		 */
	
		System.out.println("Reading...");
		NeuronPageEntries.mdsList_sc = PortalInterface.readFromProgressSheet_ManySampleModels(fileName, 1, 10);		
		System.out.println("Reading complete!");	
		NeuronPageEntries.mdsList_sc = Analysis.removeDuplicates(NeuronPageEntries.mdsList_sc);	
		System.out.println("duplicates removed..");
		
		
		/*
		 * MC
		 */
		/*
		System.out.println("MC Reading...");
		List<ModelDataStructure> mdsList_mc = PortalInterface.readFromProgressSheet_mc(fileName, 0);
		System.out.println("MC Reading complete!");
		mdsList_mc = Analysis.removeDuplicates(mdsList_mc);	
		System.out.println("Mc duplicates removed..");
		
		NeuronPageEntries.mdsList_sc = new ArrayList<>();
		for(ModelDataStructure mds_mc: mdsList_mc) {
			ModelDataStructure[] mds_scs = mds_mc.deCoupleMCModelDataStructure();
			for(int i=0;i<mds_scs.length;i++) {
				NeuronPageEntries.mdsList_sc.add(mds_scs[i]);
			}
		}
				*/
		
		
		Analysis analysis = new Analysis();	 
		analysis.populateRegionalProperties();
		System.out.println("regional properties populated..");
		analysis.populateRegionalPhenotypes(150, false, false);
	
		//String fileName_for_d_nd = "C:\\Users\\sivav\\Dropbox\\HCO\\Manuscript_II\\AnalysisFig\\Fig6\\for_D_ND_v1";
		//analysis.writeFor_D_ND(fileName_for_d_nd );
		
		String fileName_for_eda = "C:\\Users\\sivav\\Dropbox\\HCO\\Manuscript_II\\AnalysisFig\\Fig6\\forEDA_v12a_sc";
		analysis.writeForEDA_v1(fileName_for_eda);
		//analysis.populateCountOfUniqueClasses();
		
		//String filename = "C:\\Users\\sivav\\Dropbox\\HCO\\Manuscript_II\\Fig3\\RegionalExcitabilityDistribution";
		//analysis.writeExcitDistAsCSV(filename);
		
		//String filename = "C:\\Users\\sivav\\Dropbox\\HCO\\Manuscript_II\\Fig3\\k_vt_excit";
		//analysis.write_K_Vt_Exc(filename);
	}

}
