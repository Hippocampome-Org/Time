package ec.app.izhikevich.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import ec.app.izhikevich.evaluator.qualifier.SpikePatternClassifier;
import ec.app.izhikevich.inputprocess.labels.ModelParameterID;
import ec.app.izhikevich.inputprocess.labels.PatternFeatureID;
import ec.app.izhikevich.model.Ermen;
import ec.app.izhikevich.model.Izhikevich9pModel;
import ec.app.izhikevich.model.Izhikevich9pModelMC;
import ec.app.izhikevich.model.neurontypes.mc.EAGenes;
import ec.app.izhikevich.resonate.Bifurcation;
import ec.app.izhikevich.spike.labels.SpikePatternClass;

public class ModelFactory {

	private static final boolean CLASSIFY_PATTERN = true;
	public static final Izhikevich9pModel getMB1a(){
		double k0;	double a0;	double b0;	double d0;	double C0;	double vR0;	double vT0;	double vPeak0;	double c0;
		k0=3.0587634381147932;
		a0=0.026098736010710277;
		b0=7.363427418000718;
		d0=4.0;
		C0=208.0;
		vR0=-64.94459578998348;
		vT0=-44.575737574886304;
		vPeak0=-0.4169370067716045;
		c0=-51.08807348679976;
		
		Izhikevich9pModel model = new Izhikevich9pModel();
		model.setK(k0);
		model.setA(a0);
		model.setB(b0);
		model.setD(d0);	
		model.setcM(C0);
		model.setvR(vR0);
		model.setvT(vT0);		
		model.setvMin(c0);	
        model.setvPeak(vPeak0);
        
		return model;
		
	}
	public static final Izhikevich9pModelMC getUserDefined2cModel(){
		double k0;	double a0;	double b0;	double d0;	double C0;	double vR0;	double vT0;	double vPeak0;	double c0;
		double k1;	double a1;	double b1;	double d1;	double C1;	double vR1;	double vT1;	double vPeak1;	double c1;
		double G0, p0;
		k0=1.0818324180875964;
				a0=6.17132842788204E-4;
				b0=-37.30452780343738;
				d0=39.0;
				C0=74.0;
				vR0=-59.04848558918907;
				vT0=-42.6697922155552;
				vPeak0=4.382648628015637;
				c0=-52.070783040581254;


				k1=1.0818324180875964;
				a1=6.17132842788204E-4;
				b1=-37.30452780343738;
				d1=39.0;
				C1=74.0;
				vR1=-59.04848558918907;
				vT1=-42.6697922155552;
				vPeak1=4.382648628015637;
				c1=-52.070783040581254;
				G0=20.0;
				p0=0.5000903636308369;
				
				double k[] = {k0,k1};
				double a[] = {a0,a1};
				double b[] = {b0,b1};
				double d[] = {d0,d1};
				double C[] = {C0,C1};
				double vt[] = {vT0,vT1};
				double vpeak[] = {vPeak0,vPeak1};
				double c[] = {c0,c1};
				
				double g[] = {G0};
				double p[] = {p0};
				Izhikevich9pModelMC model = new Izhikevich9pModelMC(2);
				model.setK(k);
				model.setA(a);
				model.setB(b);
				model.setD(d);	
				model.setcM(C);
				model.setvR(vR0);
				model.setvT(vt);		
				model.setvMin(c);	
		        model.setvPeak(vpeak);
		        model.setG(g);
		        model.setP(p);
				return model;
				
	}
	public static final Izhikevich9pModel getUserDefinedModel(){
		double k0;	double a0;	double b0;	double d0;	double C0;	double vR0;	double vT0;	double vPeak0;	double c0;
		
		//2-003
		k0=0.6092607;
		a0=0.0036472806;
		b0=1.8359184;
		d0=2.0;
		C0=96.0;
		vR0=-57.579357;
		vT0=-37.12101;
		vPeak0=36.421597;
		c0=-49.44565;
		

		//2-044
		k0=0.50280285;
		a0=0.0065266313;
		b0=1.7731786;
		d0=45.0;
		C0=105.0;
		vR0=-56.1915;
		vT0=-33.369305;
		vPeak0=19.775654;
		c0=-39.89088;
	
		//2-043
		k0=0.9951729;
		a0=0.0038461864;
		b0=9.2642765;
		d0=-6.0;
		C0=45.0;
		vR0=-57.28488;
		vT0=-23.15752;
		vPeak0=18.676178;
		c0=-47.334415;
		
		//3-000
	/*	k0=5.9432435;
		a0=0.0011350543;
		b0=-15.885261;
		d0=74.0;
		C0=1630.0;
		vR0=-72.58829;
		vT0=-58.783615;
		vPeak0=19.990059;
		c0=-62.646774;
*/
		k0=0.5091262478834193;
		a0=0.009992916930786392;
		b0=2.3943826680001044;
		d0=6.0;
		C0=65.0;
		vR0=-60.042485181595964;
		vT0=-30.86630121066196;
		vPeak0=19.7678131981134;
		c0=-52.81049440689757;
//3006 - ca2 basket - around it - high periodic non chaotic model
		k0=5.121746081390525;
		a0=0.011996588835252293;
		b0=-8;
		d0=132.0;
		C0=400.0;
		vR0=-70.31272623619253;
		vT0=-49.91020427661326;
		vPeak0=-10.272270787521123;
		c0=-57.080158765483105;
				

		k0=0.8281213269525167;
		a0=0.0019182995200740572;
		b0=0.2579622289252459;
		d0=1.0;
		C0=46.0;
		vR0=-55.716679080251176;
		vT0=-27.74937789976201;
		vPeak0=20.277842872895683;
		c0=-45.422312115100894;
		
		//4-012
		k0=0.5916956523848826;
		a0=0.009873755940151841;
		b0=-10.914911940624444;
		d0=120.0;
		C0=195.0;
		vR0=-63.500227564101365;
		vT0=-46.58951988218102;
		vPeak0=11.38098396907138;
		c0=-50.61623937186045;

		Izhikevich9pModel model = new Izhikevich9pModel();
		model.setK(k0);
		model.setA(a0);
		model.setB(b0);
		model.setD(d0);	
		model.setcM(C0);
		model.setvR(vR0);
		model.setvT(vT0);		
		model.setvMin(c0);	
        model.setvPeak(vPeak0);
        
		return model;
		
	}
	public static final Izhikevich9pModel getUserDefinedModel(String uniqueID){
		double k0;	double a0;	double b0;	double d0;	double C0;	double vR0;	double vT0;	double vPeak0;	double c0;
		k0=0.6092607;
		a0=0.0036472806;
		b0=1.8359184;
		d0=2.0;
		C0=96.0;
		vR0=-57.579357;
		vT0=-37.12101;
		vPeak0=36.421597;
		c0=-49.44565;
		if(uniqueID.equals("2-003")) {
			//2-003
			k0=0.6092607;
			a0=0.0036472806;
			b0=1.8359184;
			d0=2.0;
			C0=96.0;
			vR0=-57.579357;
			vT0=-37.12101;
			vPeak0=36.421597;
			c0=-49.44565;
		}
		if(uniqueID.equals("2-044")) {
			//2-044
			k0=0.50280285;
			a0=0.0065266313;
			b0=1.7731786;
			d0=45.0;
			C0=105.0;
			vR0=-56.1915;
			vT0=-33.369305;
			vPeak0=19.775654;
			c0=-39.89088;
		}
		if(uniqueID.equals("2-043")) {
			//2-043
			k0=0.9951729;
			a0=0.0038461864;
			b0=9.2642765;
			d0=-6.0;
			C0=45.0;
			vR0=-57.28488;
			vT0=-23.15752;
			vPeak0=18.676178;
			c0=-47.334415;
		}
		if(uniqueID.equals("3-000")) {
			//3-000
			k0=5.9432435;
			a0=0.0011350543;
			b0=-15.885261;
			d0=74.0;
			C0=1630.0;
			vR0=-72.58829;
			vT0=-58.783615;
			vPeak0=19.990059;
			c0=-62.646774;
		}
		

		Izhikevich9pModel model = new Izhikevich9pModel();
		model.setK(k0);
		model.setA(a0);
		model.setB(b0);
		model.setD(d0);	
		model.setcM(C0);
		model.setvR(vR0);
		model.setvT(vT0);		
		model.setvMin(c0);	
        model.setvPeak(vPeak0);
        
		return model;
		
	}
	public static final Izhikevich9pModel getMB1b(){
		double k0;	double a0;	double b0;	double d0;	double C0;	double vR0;	double vT0;	double vPeak0;	double c0;
		k0=3.935030495305116; 
		C0=107.0;
		vR0=-64.67262808336909;
		vT0=-58.74397153986162; 
		a0=0.0019524485375888802;
		b0=16.57957045858656;
		c0=-59.703262575872536;
		d0=19.0;
		vPeak0=-9.928793957976993;
		
		Izhikevich9pModel model = new Izhikevich9pModel();
		model.setK(k0);
		model.setA(a0);
		model.setB(b0);
		model.setD(d0);	
		model.setcM(C0);
		model.setvR(vR0);
		model.setvT(vT0);		
		model.setvMin(c0);	
        model.setvPeak(vPeak0);
        
		return model;
		
	}
	
	public static final Izhikevich9pModel getTSTUT_NASP(){
		double k0;	double a0;	double b0;	double d0;	double C0;	double vR0;	double vT0;	double vPeak0;	double c0;
		k0=1.9127876243824558;
		a0=0.004760139450789456;
		b0=21.527153238272888;
		d0=28.0;
		C0=81.0;
		vR0=-64.68099645240748;
		vT0=-46.19479235748398;
		vPeak0=-0.6514488274989105;
		c0=-62.224898112855826;
		
		Izhikevich9pModel model = new Izhikevich9pModel();
		model.setK(k0);
		model.setA(a0);
		model.setB(b0);
		model.setD(d0);	
		model.setcM(C0);
		model.setvR(vR0);
		model.setvT(vT0);		
		model.setvMin(c0);	
        model.setvPeak(vPeak0);
        
		return model;
		
	}
	
	public static Map<String, Izhikevich9pModel> readModelsFromCSV(String csvFile){
		Map<String, Izhikevich9pModel> models = new HashMap<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(csvFile));
			String str = br.readLine();	
			str = br.readLine();	
			while(str!=null) {
				StringTokenizer st = new StringTokenizer(str, ",");
				
				String[] tokens = new String[16];
				int i=0;
				while(st.hasMoreTokens()) {
					tokens[i++]=st.nextToken();
				}
				Izhikevich9pModel model = getModel(tokens);
				models.put(tokens[0], model);
				str = br.readLine();	
			}
			br.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return models;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public static Izhikevich9pModel[] readModels(String exp){
		Izhikevich9pModel[] models = null;
		try{
			File newFile =new File("output/"+exp);
			if(newFile.isDirectory()){
				String[] files = newFile.list();
				models = new Izhikevich9pModel[files.length/2];				
				for(int i=0;i<models.length;i++){
					models[i] = readModel(exp, i);
				}				
			}else{
				System.out.println("Not a directory!");
				throw new IllegalStateException();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("Read "+models.length+" models from "+ exp);
		return models;
	}
	
	public static Izhikevich9pModel readModel(String exp, int trial){
		File newFile =new File("output/"+exp+"/job."+trial+".Full"); 
		if(!(newFile).exists()) 
		{ System.out.println("file not found! skipping.." +"file: "+ newFile.getAbsolutePath());return null;}
		double[] parms =  ECStatOutputReader.readBestSolution("output/"+exp+"/job."+trial+".Full",  50);
		
		Izhikevich9pModel model = new Izhikevich9pModel(); 			 
        EAGenes genes = new EAGenes(parms, false);        
        model.setK(genes.getK()[0]);
		model.setA(genes.getA()[0]);
		model.setB(genes.getB()[0]);
		model.setD(genes.getD()[0]);	
		model.setcM(genes.getCM()[0]);
		model.setvR(genes.getVR());
		model.setvT(genes.getVT()[0]);		
		model.setvMin(genes.getVMIN()[0]);	
        model.setvPeak(genes.getVPEAK()[0]);
           
        double I = genes.getI()[0];
        model.setInputParameters(I, 0d, 0d);
        return model;
	}

	public static Ermen readErmenModel(int trial, boolean displayParms, boolean writeParms){
		File newFile =new File("output/ermen_ea/job."+trial+".Full"); 
		if(!(newFile).exists()) 
		{ System.out.println("file not found! skipping.." +"file: "+ newFile.getAbsolutePath());return null;}
		
		double[] parms =  ECStatOutputReader.readBestSolution("output/ermen_ea/job."+trial+".Full",  50);
		
		String[] parmIDs = {"tau_v", "tau_w", "m", "n", "a", "b", "c", "d"};
		if(displayParms) {
			for(int i=0;i<8;i++)
				System.out.print(parmIDs[i]+"="+parms[i]+";\n");
		}
		
		if(writeParms) {
			try {
				FileWriter fw = new FileWriter("theory/multibehavior_models/ppsANDtvs/ermen/parms_"+trial);
				for(int i=0;i<8;i++)
					fw.write(parmIDs[i]+"="+parms[i]+";\n");
				fw.flush();
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		Ermen model = new Ermen(parms[0], parms[1], parms[2], parms[3], parms[4], parms[5], parms[6], parms[7]); 			 
         
        return model;
	}
	public static Izhikevich9pModel getModel(String[] tokens){
		int k=3;
		Izhikevich9pModel model = new Izhikevich9pModel(); 
        model.setK(Double.parseDouble(tokens[k++]));
		model.setA(Double.parseDouble(tokens[k++]));
		model.setB(Double.parseDouble(tokens[k++]));
		model.setD(Double.parseDouble(tokens[k++]));	
		model.setcM(Double.parseDouble(tokens[k++]));
		model.setvR(Double.parseDouble(tokens[k++]));
		model.setvT(Double.parseDouble(tokens[k++]));	
		model.setvPeak(Double.parseDouble(tokens[k++]));
		model.setvMin(Double.parseDouble(tokens[k++]));	
        
        return model;
	}
	public static ArrayList<Izhikevich9pModel> filterModels(Izhikevich9pModel[] models, PatternFeatureID featID, double valmin){
		int burst=0, stut=0,spike=0;
		
		ArrayList<Izhikevich9pModel> filteredModels = new ArrayList<>();
		for(int i=0;i<models.length;i++){
			Bifurcation bf = new Bifurcation(models[i], models[i].getCurrent());
			double period = bf.identifyPeriod(bf.cutSSPcareU(models[i].getvPeak()-20));	
			if(period > valmin){
				filteredModels.add(models[i]);
				if(CLASSIFY_PATTERN){
					SpikePatternClassifier classifier = new SpikePatternClassifier(bf.getSpattern());			
					classifier.classifySpikePattern(bf.getSpattern().measureSWA(models[i].getC()), true);
					SpikePatternClass _class = classifier.getSpikePatternClass();
					if(_class.containsSTUT()){
						stut++;
						
					}else{
						if(_class.containsSWB()){
							//System.out.println(bf.getSpattern().measureSWA(models[i].getC()));
							burst++;
						}else{
							if(_class.containsSP()){
								spike++;
							}else{
								System.out.println(i +"\t"+_class.toString());
							}
						}
					}
					//System.out.println(i +"\t"+_class.toString());
				}
			}
		}
		System.out.println("filtered "+filteredModels.size()+" models!");
		System.out.println("***\n"+stut+"\n"+burst+"\n"+spike+"\n***");
		return filteredModels;
	}
	
	public static void writeSelectedModelParms(ArrayList<Izhikevich9pModel> models, ModelParameterID[] parmIDs, String exp){
		String fileName = "output/"+exp+"/consModels";
		try{
			FileWriter fw = new FileWriter(fileName);
			
			for(int i=0;i<models.size();i++){
				for(ModelParameterID parmID: parmIDs){
					double parm_val = models.get(i).getParm(parmID);
					fw.write(String.valueOf(parm_val)+"\t");
				}
				fw.write("\n");
				fw.flush();
			}
			fw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

}
