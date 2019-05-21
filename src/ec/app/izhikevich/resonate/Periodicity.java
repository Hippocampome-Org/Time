package ec.app.izhikevich.resonate;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import ec.app.izhikevich.model.Izhikevich9pModel;
import ec.app.izhikevich.model.neurontypes.mc.OneNeuronInitializer;
import ec.app.izhikevich.starter.ECJStarterV2;
import ec.app.izhikevich.util.ModelFactory;

public class Periodicity {

	private static void allEAidentifiedModels() {
		String exp = "10_2_3\\B8\\7-000\\1";
		int nTrials = 2212;
		try {
			FileWriter fw = new FileWriter("periodicity.dat");
			for(int i=0;i<nTrials;i++) {
				double[] maxPeriod = identifyMaxPeriod(exp, i);
				fw.write(i+","+maxPeriod[0] +","+ maxPeriod[1]+"\n");
				fw.flush();
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		OneNeuronInitializer.init(ECJStarterV2.N_COMP, ECJStarterV2.CONN_IDCS, ECJStarterV2.PRIMARY_INPUT, ECJStarterV2.iso_comp);
		String exp = "0";//"10_2_3/B8/7-000/0";//"1";//
		
		double[] b_mins= {-100, -75, -50, -25};
		double[] b_maxs= {-75, -50, -25, 0};
		int idx = Integer.parseInt(args[0]);
		
		try {
			FileWriter fw = new FileWriter("periodicity_3-006_CB_nrw_"+idx+".dat");
			double bmin = b_mins[idx];
			double bmax = b_maxs[idx];
			int bN = 25;
			double binterval = (bmax - bmin)/(double)bN;
			
			for(int bi=0;bi<bN;bi++) {
				double b =bmin + binterval*(double)bi;
				//double b =bmax - binterval*(double)bi;
				double Cmin = 0;
				double Cmax = 1000;
				int CN = 100;
				double Cinterval = (Cmax - Cmin)/(double)CN;			
				for(int Ci=0;Ci<CN;Ci++){
					double C =Cmin + Cinterval*(double)Ci;
					double[] maxPeriod = identifyMaxPeriod(exp, C, b);
					fw.write(C+","+b+","+maxPeriod[0] +","+ maxPeriod[1]+"\n");
					//System.out.println(C+","+b+","+maxPeriod[0] +","+ maxPeriod[1]+"\n");
					fw.flush();
				}
			}
			
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		
		
		//int trial = 10;
		//double[] maxPeriod = identifyMaxPeriod(exp, trial);
		//System.out.println(maxPeriod[0] +"\t"+ maxPeriod[1]);
	}
	
	//returns maximum period and corresponding I
	public static double[] identifyMaxPeriod(String exp, int trial) {
		
		Izhikevich9pModel model = ModelFactory.readModel(exp, trial);
		
		double Imin = 500;
		double Imax = 750;
		int N = 1000;
		double Iinterval = (Imax - Imin)/(double)N;
		int maxPeriod = 0;
		double i_max_period = 0;
		for(int i=0;i<N;i++){
			
			double I =Imin + Iinterval*(double)i;
			Bifurcation bf = new Bifurcation(model, I);
			if(Bifurcation.skipI){
				System.out.println("skipped "+ I);
				Bifurcation.skipI = false;
				continue;					
			}
			ArrayList<Double> u = bf.cutSSPcareU(model.getvPeak()-20);
			int period = bf.identifyPeriod(u);
			if(period>maxPeriod) {
				maxPeriod = period;
				i_max_period = I;
			}
		}
		return new double[] {maxPeriod, i_max_period};
	}

public static double[] identifyMaxPeriod(String exp, double C, double b) {
	//String csvFile = "theory/multibehavior_models/models.csv";
	//Map<String, Izhikevich9pModel> models = ModelFactory.readModelsFromCSV(csvFile);//
	
		Izhikevich9pModel model = ModelFactory.getUserDefinedModel();//readModel(exp, 17);//models.get("3-006");
		model.setcM(C);
		model.setB(b);
		
		double Imin = 100;
		double Imax = 1000;
		int N = 1000;
		double Iinterval = (Imax - Imin)/(double)N;
		int maxPeriod = 0;
		double i_max_period = 0;
		for(int i=0;i<N;i++){
			if(i%100==0)
				System.out.println(i);
			double I =Imin + Iinterval*(double)i;
			Bifurcation bf = new Bifurcation(model, I);
			if(Bifurcation.skipI){
				System.out.println("skipped "+ I);
				Bifurcation.skipI = false;
				continue;					
			}
			ArrayList<Double> u = bf.cutSSPcareU(model.getvPeak()-20);
			int period = bf.identifyPeriod(u);
			if(period>maxPeriod) {
				maxPeriod = period;
				i_max_period = I;
			}
		}
		return new double[] {maxPeriod, i_max_period};
	}

}
