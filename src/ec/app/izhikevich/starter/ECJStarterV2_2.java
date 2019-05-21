package ec.app.izhikevich.starter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import ec.EvolutionState;
import ec.Evolve;
import ec.app.izhikevich.inputprocess.labels.ModelParameterID;
import ec.app.izhikevich.model.Izhikevich9pModel;
import ec.app.izhikevich.model.neurontypes.mc.EAGenes;
import ec.app.izhikevich.model.neurontypes.mc.OneNeuronInitializer;
import ec.app.izhikevich.resonate.Bifurcation;
import ec.app.izhikevich.resonate.PyPlotter;
import ec.app.izhikevich.util.ModelFactory;
import ec.util.Output;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

public class ECJStarterV2_2 {	
	
	public static String Phen_Category = "1-1-1";
	public static String Phen_Num = "B0";
	public static String Neur = "9-999";	
	public static boolean iso_comp = false;
	
	public static int N_COMP = -1;
	public static int[] CONN_IDCS =null; 	
	public static String PRIMARY_INPUT = "";
	private static final boolean timer = true;			
		 
	private static String ECJ_PARMS;	
	static {
		try {
			BufferedReader br = new BufferedReader(new FileReader("primary_input"));
			String str = br.readLine();
			if(str==null){
				System.out.println("Empty primary input!");
				System.exit(-1);
			}
			
			//System.out.println(lastButTwo);
			StringTokenizer st = new StringTokenizer(str, ",");
			Phen_Category = st.nextToken();
			Phen_Num = st.nextToken();
			Neur = st.nextToken();
			///System.out.println(st.nextToken());
			N_COMP = Integer.valueOf(st.nextToken());
			CONN_IDCS = new int[N_COMP];
			for(int i=0;i<N_COMP;i++)
				CONN_IDCS[i]=Integer.valueOf(st.nextToken());
			if(st.hasMoreTokens()){
				iso_comp = Boolean.valueOf(st.nextToken());
			}
			str = br.readLine();
			
				
			ECJ_PARMS = "input/izhikevich_SO2.params";
			
			br.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PRIMARY_INPUT = "input/"+Phen_Category+"/"+Phen_Num+"/"+Neur+".json";
	}
	
	
	public static void main(String[] args) {	
		OneNeuronInitializer.init(N_COMP, CONN_IDCS, PRIMARY_INPUT, iso_comp);
		Map<ModelParameterID, EAParmsOfModelParm> geneParms = OneNeuronInitializer.geneParms;
		
		String[] args2 = new String[] {"-file", "input/ermen.params", "-p", "jobs=1000"};
        Evolve.main(args2);
		
	}




}