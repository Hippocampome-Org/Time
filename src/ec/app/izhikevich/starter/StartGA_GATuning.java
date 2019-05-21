package ec.app.izhikevich.starter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ec.Evolve;

public class StartGA_GATuning {
	static int[] popSize = new int[] {20};//, 40};
	static int[] tourSize = new int[] {2};
	
	static String[] xOverType = new String[] {"one"};//, "two", "any"};
	static float[] xOverProb = new float[] {0.5f,  0.75f};//, 0.25f,1, 0};
	
	static float[] mutSD = new float[] {0.1f, 0.2f, 0.3f};//, 0.4f, 0.5f};
	static float[] mutProb = new float[] {0.05f, 0.1f, 0.25f, 0.5f, 0.75f};//{0.05f,0.1f,0.25f,	0.5f, 0.75f, 0.9f, 1};
	
	public static void main(String[] args) {
		String[] args2 = new String[args.length + 16];
		//base parm file
		args2[0] = args[0];
		args2[1] = args[1];
		
		int combination = 0;
		File combFile = new File("combFile_final");
		try {
			FileWriter fw = new FileWriter(combFile);
		
		for(int ps=0; ps<popSize.length; ps++)
			for(int ts = 0;ts<tourSize.length;ts++)
				for(int xt=0;xt<xOverType.length;xt++)
					for(int xp=0;xp<xOverProb.length;xp++)
						for(int msd=0;msd<mutSD.length;msd++)
							for(int mp=0;mp<mutProb.length;mp++)
							{
								//restart point = 35
							//	if(combination < 430) {combination++; continue;}
								
								GAParameter parm = new GAParameter(popSize[ps], tourSize[ts], xOverType[xt], xOverProb[xp], mutSD[msd], mutProb[mp]);
								String[] override_parms = parm.getParms();
								
								for(int i=0;i<override_parms.length;i++)
								{
									args2[i+2] = override_parms[i];
								}
								/*
								 * 5 trials each
								 */
								for(int t=0; t<5;t++) {
									args2[14] = "-p";
									args2[15] = "stat.file=../../../../output_fix/$_"+combination+"_F_"+t;
									
									args2[16] = "-p";
									args2[17] = "stat.child.0.file=../../../../output_fix/$_"+combination+"_S_"+t;
									/*
									 * *********																
									 */
									Evolve.main(args2);	
								}
								fw.write(combination+"\t" +parm.getFlatParms()+"\n");								
								fw.flush();
								
								combination++;
							}
				
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
		//System.out.println(args.length);
	}

}
