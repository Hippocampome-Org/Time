package ec.app.izhikevich.plot;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import ec.app.izhikevich.inputprocess.labels.ModelParameterID;
import ec.app.izhikevich.inputprocess.labels.PatternFeatureID;
import ec.app.izhikevich.model.Izhikevich9pModel;
import ec.app.izhikevich.model.neurontypes.mc.OneNeuronInitializer;
import ec.app.izhikevich.resonate.Bifurcation;
import ec.app.izhikevich.starter.ECJStarterV2;
import ec.app.izhikevich.util.ModelFactory;

public class ChaoticSpace {

	public static final String FILE_PFX_localSearch_results = "theory/chaotic_space/periodicity_d";
	public static final String FILE_PFX_globalSearch_results = "10_2_1/Mbi2/3-006/2";
	
	public static void main(String[] args) {
		OneNeuronInitializer.init(ECJStarterV2.N_COMP, ECJStarterV2.CONN_IDCS, ECJStarterV2.PRIMARY_INPUT, ECJStarterV2.iso_comp);
		
		writeGlobalSearchResults(12);
		/*Izhikevich9pModel model = ModelFactory.readModel(FILE_PFX_globalSearch_results, 0);
		Bifurcation bf = new Bifurcation(model, model.getCurrent());
		System.out.println(model.getCurrent());
		
		SpikePatternClassifier classifier = new SpikePatternClassifier(bf.getSpattern());			
		classifier.classifySpikePattern(bf.getSpattern().getSwa(), true);
		SpikePatternClass _class = classifier.getSpikePatternClass();
		System.out.println("\t"+_class.toString());*/
	}
	
	public static void performLocalSearch(){
		Izhikevich9pModel startingModel = ModelFactory.getMB1a();
		
		ModelParameterID xID = ModelParameterID.D;	
		double x_start = startingModel.getParm(xID);
		x_start = 0;	//starting point	
		startingModel.setParm(xID, x_start);
		double stepSizeX = .1f;
		int nStepsX = 100;
		
		ModelParameterID yID = ModelParameterID.I;	
		double y_start = startingModel.getParm(yID);
		y_start=350;		//starting point
		startingModel.setParm(yID, y_start);
		double stepSizeY = 1;
		int nStepsY = 100;
				
		try {
			FileWriter fwx = new FileWriter(FILE_PFX_localSearch_results+"_x");
			FileWriter fwy = new FileWriter(FILE_PFX_localSearch_results+"_y");
			FileWriter fwz = new FileWriter(FILE_PFX_localSearch_results+"_z");
			
			for(int i=0;i<nStepsX;i++){
				for(int j=0;j<nStepsY;j++){
					double newX = x_start + (double)i*stepSizeX;
					double newY = y_start + (double)j*stepSizeY;
					startingModel.setParm(xID, newX);
					startingModel.setParm(yID, newY);					
					 
					Bifurcation bf = new Bifurcation(startingModel, newY);
					int newZ = bf.identifyPeriod(bf.cutSSPcareU(startingModel.getvPeak()-20));					
					
					fwx.write(newX+"\n");
					fwy.write(newY+"\n");
					fwz.write(newZ+"\n");
					
					fwx.flush();fwy.flush();fwz.flush();
					System.out.println(i+" "+j+" completed!");
				}				
			}
			fwx.close();fwy.close();fwz.close();			
					
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeGlobalSearchResults(double periodThreshold){
		Izhikevich9pModel[] allModels = ModelFactory.readModels(FILE_PFX_globalSearch_results);
		ArrayList<Izhikevich9pModel> filteredModels = ModelFactory.filterModels(allModels, PatternFeatureID.period, periodThreshold);
		//ModelParameterID[] parmIDs = new ModelParameterID[]{ModelParameterID.K, ModelParameterID.D, ModelParameterID.I};
		//ModelFactory.writeSelectedModelParms(filteredModels, parmIDs , FILE_PFX_globalSearch_results);
	}
	
	
}
