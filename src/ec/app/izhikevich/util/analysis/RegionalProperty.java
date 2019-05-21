package ec.app.izhikevich.util.analysis;

import java.util.ArrayList;
import java.util.List;

import ec.app.izhikevich.spike.labels.Phenotype;
import ec.app.izhikevich.spike.labels.SpikePatternClass;
import ec.app.izhikevich.spike.labels.SpikePatternComponent;
import ec.app.izhikevich.util.forportal.ModelDataStructure;

public class RegionalProperty {
	List<ModelDataStructure> mdsList;
	List<Double> excitabilityList;
	List<Double> minFreqList; //continuous variable to distinguish FAST spiking class
	List<Phenotype> phenotypeList;
	//other lists...
	
	SpikePatternClass[][] patternClasses;
	int countOfUniqueClasses[];// length equals the 1st dimension of patternClasses = length of mdsList
	
	int[] countOfD; // length equals the 2nd dimension of patternClasses
	int[] countOfSLN;
	int[] countOfSTUT;
	int[] countOfRASP;
	int[] countOfASP;
	int[] countOfNASP;
	
	RegionalProperty(){
		mdsList = new ArrayList<>();
		excitabilityList = new ArrayList<>();
		minFreqList = new ArrayList<>();
		phenotypeList = new ArrayList<>();
	}
	
	/*public void populatePhenotypes(double I_offset, int n_offsets, boolean doRBS) {
		patternClasses = new SpikePatternClass[mdsList.size()][];
		for(int i=0;i<mdsList.size();i++) {
			double I_start = excitabilityList.get(i)+I_offset;	
			//this call will populate n_offsets+1 classes (+1 for rebound spiking scenario)
			patternClasses[i] = mdsList.get(i).getPhenotype(I_start, I_offset, n_offsets, doRBS);
			if(doRBS) {
				System.out.println("****WARNING!! RBS will take in class 'D.', SET doRBS to FALSE!");
			}
			phenotypeList.add(Phenotype.getPhenotype(patternClasses[i]));
		}		
	}*/
	
	public void populatePhenotypes_v3(double I_offset) {
		for(int i=0;i<mdsList.size();i++) {
			mdsList.get(i).populatePhenotype_v3(I_offset);
		}		
	}
	
	public void populateCountOfPhenotypeComponents() {
		if(patternClasses == null) {
			System.out.println("Invoke populatePhenotypes() first!");
			System.out.println();
		}
		
		countOfD = new int[patternClasses[0].length];
		countOfSLN = new int[patternClasses[0].length];
		countOfSTUT= new int[patternClasses[0].length];
		countOfRASP= new int[patternClasses[0].length];
		countOfASP= new int[patternClasses[0].length];
		countOfNASP= new int[patternClasses[0].length];
		SpikePatternClass nasp = new SpikePatternClass();
		nasp.addComponent(SpikePatternComponent.NASP);
		
		SpikePatternClass asp_nasp = new SpikePatternClass();
		asp_nasp.addComponent(SpikePatternComponent.ASP);
		asp_nasp.addComponent(SpikePatternComponent.NASP);		
		SpikePatternClass asp = new SpikePatternClass();
		asp.addComponent(SpikePatternComponent.ASP);
		SpikePatternClass asp_asp = new SpikePatternClass();
		asp_asp.addComponent(SpikePatternComponent.ASP);
		asp_asp.addComponent(SpikePatternComponent.ASP);
		
		for(int i=0;i<patternClasses.length; i++) {
			for(int j=0;j<patternClasses[i].length;j++) {
				
				if(patternClasses[i][j].contains(SpikePatternComponent.D)) {
					countOfD[j]++;
				}
				if(patternClasses[i][j].contains(SpikePatternComponent.SLN)) {
					countOfSLN[j]++;
				}
				if(patternClasses[i][j].contains(SpikePatternComponent.RASP)) {
					countOfRASP[j]++;
				}
				if(patternClasses[i][j].containsSTUT() || patternClasses[i][j].containsSWB()) {
					countOfSTUT[j]++;
				}
				if(patternClasses[i][j].equals(nasp)) {
					countOfNASP[j]++;
				}
				if(patternClasses[i][j].equals(asp) || patternClasses[i][j].equals(asp_nasp) || patternClasses[i][j].equals(asp_asp)) {
					countOfASP[j]++;
				}
			}
		}
	}
	public void populateCountOfUniqueClasses() {
		countOfUniqueClasses = new int[excitabilityList.size()];
		for(int i=0;i<excitabilityList.size();i++) {
			countOfUniqueClasses[i]=getNoOfUniqueClasses(i);
		}
	}
	
	public int getNoOfUniqueClasses(int idx) {
		
		ArrayList<SpikePatternClass> pattern_class_hold = new ArrayList<>();
		SpikePatternClass[] phenotype = patternClasses[idx];
		
		for(int i=0;i<phenotype.length;i++) {
			if(!contains(pattern_class_hold, phenotype[i])) {
				pattern_class_hold.add(phenotype[i]);
			}
		}
		
		return pattern_class_hold.size();
	}
	
	private boolean contains(ArrayList<SpikePatternClass> list, SpikePatternClass pattern_class) {
		boolean _contains = false;
		for(SpikePatternClass _class: list) {
			if(_class.equals(pattern_class)) {
				_contains = true;
				break;
			}
		}
		return _contains;
	}
	
	public int countOfModels() {
		return mdsList.size();
	}
	
	public void displayCountOfModels() {
		System.out.print(countOfModels()+"\t");
	}
	
	public void displayExcitabilities() {
		for(double e: excitabilityList)
			System.out.print(e+"\t");
	}
	
	public void displayRegionalPhenotypes() {
		System.out.println();
		for(int i=0;i<patternClasses.length;i++) {
			System.out.print(mdsList.get(i).neuronSubtypeID+"\t");
			for(int j=0;j<patternClasses[i].length;j++) {
				System.out.print(patternClasses[i][j]+"\t");
			}
			System.out.println();
		}
	}
	
	public void displayCountOfPhenotypeComponents() {
		System.out.println();
		System.out.print("D.\t");
		for(int i=0;i<patternClasses[0].length;i++) {
			System.out.print(countOfD[i]+"\t");
		}
		System.out.println();
		System.out.print(".SLN\t");
		for(int i=0;i<patternClasses[0].length;i++) {
			System.out.print(countOfSLN[i]+"\t");
		}
		System.out.println();
		System.out.print("RASP\t");
		for(int i=0;i<patternClasses[0].length;i++) {
			System.out.print(countOfRASP[i]+"\t");
		}
		System.out.println();
		System.out.print("STUT.\t");
		for(int i=0;i<patternClasses[0].length;i++) {
			System.out.print(countOfSTUT[i]+"\t");
		}
		System.out.println();
		System.out.print("NASP.\t");
		for(int i=0;i<patternClasses[0].length;i++) {
			System.out.print(countOfNASP[i]+"\t");
		}
		System.out.println();
		System.out.print("ASP./ASP.NASP\t");
		for(int i=0;i<patternClasses[0].length;i++) {
			System.out.print(countOfASP[i]+"\t");
		}
		System.out.println();		
	}
	public void displayCountOfUniqueClasses() {
		System.out.println();
		for(int i=0;i<excitabilityList.size();i++) {
			System.out.print(excitabilityList.get(i)+"\t"+countOfUniqueClasses[i]+"\n");
		}
	}
	
	public String getCSVString_excitlist() {
		String csvString = "";
		for(int i=0;i<excitabilityList.size();i++) {
			csvString +=excitabilityList.get(i);
			if(i!=excitabilityList.size()-1)
				csvString +=",";
		}
		return csvString;
	}
	
	public boolean isContSpikingOnly(int idx) {
		boolean contSpikingOnly = true;
		for(int i=0;i<patternClasses[idx].length-1;i++) {
			if(	patternClasses[idx][i].contains(SpikePatternComponent.TSTUT) 	|| 
				patternClasses[idx][i].contains(SpikePatternComponent.TSWB) 	||
				patternClasses[idx][i].contains(SpikePatternComponent.PSTUT) 	|| 
				patternClasses[idx][i].contains(SpikePatternComponent.PSWB)
				) {
				contSpikingOnly = false;
				break;
			}				
		}	
		return contSpikingOnly;
	}
	
	public String hasDelay(int idx) {
		String d="0";
		for(int i=0;i<patternClasses[idx].length-1;i++) {// length-1 cuz of RBS appended to the last idx
			if(patternClasses[idx][i].contains(SpikePatternComponent.D)) {
				d="1";
				break;
			}				
		}	
		return d;
	}
	
	public String hasSLN(int idx) {
		String d="0";
		for(int i=0;i<patternClasses[idx].length-1;i++) {// length-1 cuz of RBS appended to the last idx
			if(patternClasses[idx][i].contains(SpikePatternComponent.SLN)) {
				d="1";
				break;
			}				
		}	
		return d;
	}
	
	public String hasTSTUT(int idx) {
		String d="0";
		for(int i=0;i<patternClasses[idx].length-1;i++) {// length-1 cuz of RBS appended to the last idx
			if(		patternClasses[idx][i].contains(SpikePatternComponent.TSTUT) 	||
					patternClasses[idx][i].contains(SpikePatternComponent.TSWB)	) {
				d="1";
				break;
			}				
		}	
		return d;
	}

	public String hasRBS(int idx) {
		String d="0";
		if(patternClasses[idx][patternClasses[idx].length-1].contains(SpikePatternComponent.RBS))
			d="1";
		return d;
	}
	
	public static String getDL_list_for_EDA_header() {
		String dslString = "";
		dslString += "subtype_id\tk\ta\tb\td\tC\tvT\t"
				+ "Excitability\t"
				+ "Min_Freq\t"
				+ "Phen_Class\t"
				+ "Has_RBS\tHAS_D\tHAS_SLN\tHAS_TSTUT\n";
		return dslString;
	}
	/*
	 * phenotype grouping based on FP paper
	 */	
	public String getDL_list_for_EDA() {
		String dslString = "";
		String delim = ",";
		
		for(int i=0;i<mdsList.size();i++) {
			//dslString += mdsList.get(i).neuronSubtypeID+"\t";
			dslString += mdsList.get(i).model.getK()[0]+delim;
			dslString += mdsList.get(i).model.getA()[0]+delim;
			dslString += mdsList.get(i).model.getB()[0]+delim;
			dslString += mdsList.get(i).model.getD()[0]+delim;
			dslString += mdsList.get(i).model.getcM()[0]+delim;
			dslString += (mdsList.get(i).model.getvT()[0] - mdsList.get(i).model.getvR()[0])+delim;
			dslString += (mdsList.get(i).model.getvMin()[0] - mdsList.get(i).model.getvR()[0])+delim;
			dslString += (mdsList.get(i).model.getvPeak()[0] - mdsList.get(i).model.getvR()[0])+delim;
			
			dslString += mdsList.get(i).S+delim; //soma (=0) for SC
			
			dslString += excitabilityList.get(i)+delim;
			
			dslString += mdsList.get(i).RBS+delim;
			dslString +=  mdsList.get(i).D+delim;
			dslString +=  mdsList.get(i).ASP+delim;
			dslString +=  mdsList.get(i).PSTUT+delim;
			//for v8 EDA
			dslString +=  mdsList.get(i).NASP+delim;
			dslString +=  mdsList.get(i).property.stutFactor+delim;
			//for v9 EDA
			dslString +=  mdsList.get(i).property.delayFactor+delim;
			//forv11 EDA
			dslString += mdsList.get(i).property.excitability_rb+delim;
			//forv12 EDA
			dslString += mdsList.get(i).property.rbDelay;
			
			//dslString += minFreqList.get(i)+delim;
			//dslString += phenotypeList.get(i).getIdentifier();
			//dslString += "25"+"\t";
			/*
			int phenClass = 0;
			if(isContSpikingOnly(i))
				phenClass = 1;
			else
				phenClass = 2;
			dslString += phenClass+delim;
			
			dslString += hasRBS(i)+delim+hasDelay(i)+delim+hasSLN(i)+delim+hasTSTUT(i);
			*/
			
			dslString+="\n";
		}
		return dslString;
	}
}
