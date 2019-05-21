package ec.app.izhikevich.util.analysis;

import java.util.List;

import ec.app.izhikevich.outputprocess.CarlMcSimData;
import ec.app.izhikevich.outputprocess.CarlOutputParser;
import ec.app.izhikevich.util.forportal.ModelDataStructure;
import ec.app.izhikevich.util.forportal.PortalInterface;

public class Analysis_MC {

	private static void rheobase_inputresistance_corr(List<ModelDataStructure> mdsList_mc, String layout) {
		for(ModelDataStructure mds: mdsList_mc) {
			if(!mds.getMcLayoutCode().equals(layout)) continue;
			
			if(mds.getCarlOutputParser()!=null) {
				
				System.out.print(mds.neuronSubtypeID+"\t");
				CarlMcSimData mcData = mds.getCarlOutputParser().extractCarlMcSimData();
				
				double[] vDefs = mcData.getVdefs();
				double[] ramprheos = mcData.getRampRheos();
				for(int i=0;i<vDefs.length; i++) {
					vDefs[i] = mds.model.getvR()[0]-vDefs[i];
				}
				
				if(layout.equals("2c")) {
					System.out.print(ramprheos[1]+"\t"+ramprheos[0]+"\t");
					System.out.print(vDefs[1]+"\t"+vDefs[0]+"\t");
					System.out.println();
				}
				if(layout.equals("3c1")) {
					System.out.print(ramprheos[0]+"\t"+ramprheos[1]+"\t"+ramprheos[2]+"\t");
					System.out.print(vDefs[0]+"\t"+vDefs[1]+"\t"+vDefs[2]+"\t");
					System.out.println();
				}
				if(layout.equals("3c2")) {
					System.out.print(mds.model.getK()[1]+"\t"+mds.model.getK()[0]+"\t"+mds.model.getK()[2]+"\t");
					System.out.print(mds.model.getA()[1]+"\t"+mds.model.getA()[0]+"\t"+mds.model.getA()[2]+"\t");
					System.out.print(mds.model.getB()[1]+"\t"+mds.model.getB()[0]+"\t"+mds.model.getB()[2]+"\t");
					System.out.print(mds.model.getD()[1]+"\t"+mds.model.getD()[0]+"\t"+mds.model.getD()[2]+"\t");
					System.out.print(mds.model.getcM()[1]+"\t"+mds.model.getcM()[0]+"\t"+mds.model.getcM()[2]+"\t");
					
					System.out.print(ramprheos[1]+"\t"+ramprheos[0]+"\t"+ramprheos[2]+"\t");
					System.out.print(vDefs[1]+"\t"+vDefs[0]+"\t"+vDefs[2]+"\t");
					System.out.println();
				}
				
				if(layout.equals("4c2")) {
					System.out.print(ramprheos[1]+"\t"+ramprheos[0]+"\t"+ramprheos[2]+"\t"+ramprheos[3]+"\t");
					System.out.print(vDefs[1]+"\t"+vDefs[0]+"\t"+vDefs[2]+"\t"+vDefs[3]+"\t");
					System.out.println();
				}
				
			}
		}	
	}
	
	private static double calculateAttenuation(double sourceVAmp, double destVAmp) {
		return destVAmp/sourceVAmp;
	}
	
	private static double[] getAttenuations(double[][] espsAll, double Vr) {
		double[] attRates = new double[espsAll.length];
		//System.out.println(Vr);
		for(int i=0;i<espsAll.length;i++) {
			//System.out.print(espsAll[2][i+1]+"\t"+espsAll[2][0]+"\n");
			attRates[i] = calculateAttenuation(espsAll[i][i+1]-Vr, espsAll[i][0]-Vr);
		}
		
		return attRates;
	}
	
	private static void voltage_attenuation(List<ModelDataStructure> mdsList_mc, String layout) {
		for(ModelDataStructure mds: mdsList_mc) {
			if(!mds.getMcLayoutCode().equals(layout)) continue;
			
			if(mds.getCarlOutputParser()!=null) {
				CarlOutputParser parser = mds.getCarlOutputParser();
				CarlMcSimData mcData = parser.extractCarlMcSimData();
				
				
				double[] attRates = getAttenuations(mcData.getEpspsAll(), mds.model.getvR()[0]);
				String flat_subtypeID = mds.neuronSubtypeID.substring(0, 1) +""+ 
										mds.neuronSubtypeID.substring(2, 5)+""+
										mds.neuronSubtypeID.substring(6, 7);
				System.out.print(flat_subtypeID+",");
				for(int i=0;i<attRates.length;i++) {
						
					if(i==attRates.length-1)
						System.out.print(attRates[i]);
					else
						System.out.print(attRates[i]+",");
				}
				System.out.println();
			}
		}
	}
	private static void coupling_asymmetry(List<ModelDataStructure> mdsList_mc, String layout) {
		for(ModelDataStructure mds: mdsList_mc) {
			if(!mds.getMcLayoutCode().equals(layout)) continue;
			
			if(mds.getCarlOutputParser()!=null) {
				
				String flat_subtypeID = mds.neuronSubtypeID.substring(0, 1) +""+ 
										mds.neuronSubtypeID.substring(2, 5)+""+
										mds.neuronSubtypeID.substring(6, 7);
				System.out.print(flat_subtypeID+",");
				
				//4c2, 4c1
				//System.out.print(mds.model.getP()[0]+","+mds.model.getP()[1]+","+mds.model.getP()[2]);
				//3c1
				//System.out.print(mds.model.getP()[0]+","+mds.model.getP()[1]);
				System.out.print(mds.model.getP()[0]);
				System.out.println();
			}
		}
	}
	
	private static void coupling_strength(List<ModelDataStructure> mdsList_mc, String layout) {
		for(ModelDataStructure mds: mdsList_mc) {
			if(!mds.getMcLayoutCode().equals(layout)) continue;
			
			if(mds.getCarlOutputParser()!=null) {
				
				String flat_subtypeID = mds.neuronSubtypeID.substring(0, 1) +""+ 
										mds.neuronSubtypeID.substring(2, 5)+""+
										mds.neuronSubtypeID.substring(6, 7);
				System.out.print(flat_subtypeID+",");
				
				//4c2, 4c1
				System.out.print(mds.model.getG()[0]*mds.model.getP()[0]+","+mds.model.getG()[1]*mds.model.getP()[1]+","+mds.model.getG()[2]*mds.model.getP()[2]);
				//3c1
				//System.out.print(mds.model.getG()[0]*mds.model.getP()[0]+","+mds.model.getG()[1]*mds.model.getP()[1]);
				System.out.println();
			}
		}
	}
	public static void main(String[] args) {
		
		/*
		 * 40 synapses on CA1 pyramidal SLM
		 */
	/*	double vr = -69.14038633346178;
		double so = -66.24066162109375;
		double sp = -66.1329345703125;				
		double sr = -63.907398223876953;
		double slm = -33.198516845703125;
		
		System.out.println(calculateAttenuation(sr-vr, sp-vr));
		System.out.println(calculateAttenuation(slm-vr, sp-vr));
		System.exit(0);
		*/
		
		
		String fileName = "C:\\Users\\sivav\\Dropbox\\HCO\\MCProgress_v3_15_18.xlsx";
		System.out.println("Reading...");
		List<ModelDataStructure> mdsList_sc = PortalInterface.readFromProgressSheet(fileName, 1);
		System.out.println("SC Reading complete!");
		List<ModelDataStructure> mdsList_mc = PortalInterface.readFromProgressSheet_mc(fileName, 0);
		System.out.println("MC Reading complete!");
		
		
		//String neuronSubTypeID = "4-000-1";
		//ModelDataStructure mds = NeuronPageEntries.getModelDataStructure(mdsList_mc, neuronSubTypeID);
		String layout = "2c";		
		//rheobase_inputresistance_corr(mdsList_mc, layout);	
		//voltage_attenuation(mdsList_mc, layout);
		
		coupling_asymmetry(mdsList_mc, layout);
	}

}
