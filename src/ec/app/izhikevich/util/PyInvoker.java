package ec.app.izhikevich.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PyInvoker {
	
	private static final String C1_MODULE_onlyV = "_2cFromJava.py";
	private static final String C1_MODULE_onlyV_forportal = "_2cFromJava_forpor.py";
	private static final String SingleCompartment_py = "_1cFromJava.py";
	
	String opFile;
	double[] durations;
	
	boolean displayErrStream;
	
	double[][] inputCurrents;
	int nScen;
	int nComp;
	int layoutcode;
	
	double modelVr;
	double modelVpk;
	int[] somaCurrents;
	List<String> _classes;
	int[] colorCodeIdcs;
	
	int[] rheoCurrents;
	int[] irCurrents;
	int[] spCurrents;
	
	public PyInvoker(String outputFile, int nScen, int nComp, double model_vr, double model_vpk, double[] soma_currents) {
		this.opFile= outputFile;
		this.nScen = nScen;
		this.nComp=nComp;
		this.modelVr=model_vr;
		this.modelVpk=model_vpk;
		somaCurrents = new int[nScen];
		_classes = new ArrayList<>();
		colorCodeIdcs = new int[nScen];
		
		//System.out.println("--"+colorCodeIdcs.length);
		
		for(int i=0;i<nScen;i++) {
			somaCurrents[i]=(int) soma_currents[i];
		}
		
		if(nComp>1) {
			rheoCurrents=new int[nComp];
			irCurrents=new int[nComp];
			spCurrents=new int[nComp-1];
		}			
	}

	public void addClass(String _class) {
		_classes.add(_class);
	}
	public void invokeForSC(String fileNamePfx, String doShow){
		List<String> command = new ArrayList<String>();
		command.add("python");
	
		command.add(SingleCompartment_py);	
		
		command.add(""+nScen);		
		
		for(int i=0;i<nScen;i++) {
			command.add(fileNamePfx+"_t"+i);			
			command.add(fileNamePfx+"_v"+i);
		}	
		
				
		command.add(opFile);
		command.add(doShow);
		command.add(""+modelVr);
		command.add(""+modelVpk);
		
		for(int i=0;i<nScen;i++) {
			command.add(""+somaCurrents[i]);
			command.add(""+_classes.get(i));
		}
		
		//
		
		for(int i=0;i<nScen;i++) {
			command.add(""+colorCodeIdcs[i]);
		}
		
		String brianOutputAsString = "";
		if(displayErrStream)
			System.out.println(command);
			
		ProcessBuilder pb = new ProcessBuilder(command);
		try {
			Process p = pb.start();		
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while(true){
				String retVal = in.readLine();				
				if(retVal!=null) {
					brianOutputAsString+=retVal;	
				}else{
					break;
				}	
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}	
		if(displayErrStream)
			System.out.println(brianOutputAsString);
		
	}
	
	public void invoke(String fileNamePfx, String doShow){
		List<String> command = new ArrayList<String>();
		command.add("python3");
	
		command.add(C1_MODULE_onlyV);		
		
		
		command.add(""+nScen);
		command.add(""+nComp);
		
		for(int i=0;i<nScen;i++) {
			command.add(fileNamePfx+"_t"+i);
			for(int j=0;j<nComp;j++)
				command.add(fileNamePfx+"_v"+i+"_"+j);
		}		
		
		command.add(fileNamePfx+"_t_exc");
		for(int j=0;j<nComp;j++)
			command.add(fileNamePfx+"_v_exc_"+j);
		
		command.add(fileNamePfx+"_t_ir");
		for(int j=0;j<nComp;j++)
			command.add(fileNamePfx+"_v_ir_"+j);
		
		for(int i=1;i<nComp;i++) {
			command.add(fileNamePfx+"_t_sp"+i);
			for(int j=0;j<nComp;j++)
				command.add(fileNamePfx+"_v_sp"+i+"_"+j);
		}
		
		for(int i=1;i<nComp;i++) {
			command.add(fileNamePfx+"_t_epsp"+i);
			for(int j=0;j<nComp;j++)
				command.add(fileNamePfx+"_v_epsp"+i+"_"+j);
		}		
		
		command.add(opFile);
		command.add(doShow);
		command.add(""+modelVr);
		command.add(""+modelVpk);
		
		for(int i=0;i<nScen;i++) {
			command.add(""+somaCurrents[i]);
		}
		if(nComp>1) {
			for(int i=0;i<nComp;i++) {
				command.add(""+rheoCurrents[i]);
			}
			for(int i=0;i<nComp;i++) {
				command.add(""+irCurrents[i]);
			}
			for(int i=0;i<nComp-1;i++) {
				command.add(""+spCurrents[i]);
			}
		}
		
		String brianOutputAsString = "";
		if(displayErrStream)
			System.out.println(command);
			
		ProcessBuilder pb = new ProcessBuilder(command);
		try {
			Process p = pb.start();		
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while(true){
				String retVal = in.readLine();				
				if(retVal!=null) {
					brianOutputAsString+=retVal;	
				}else{
					break;
				}	
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}	
		if(displayErrStream)
			System.out.println(brianOutputAsString);
		
	}
	public void invoke2(String fileNamePfx, String doShow){
		List<String> command = new ArrayList<String>();
		command.add("python3");
	
		//command.add(C1_MODULE_onlyV_forportal);		
		command.add(C1_MODULE_onlyV);		

		command.add(""+layoutcode);
		command.add(""+nScen);
		command.add(""+nComp);
		
		int nTotScens = nScen+ (2) + (2*(nComp-1));
		
		for(int i=0;i<nTotScens;i++) {
			command.add(fileNamePfx+"_t"+i);
			for(int j=0;j<nComp;j++)
				command.add(fileNamePfx+"_v"+i+"_"+j);
		}				
	
		command.add(opFile);
		command.add(doShow);
		command.add(""+modelVr);
		command.add(""+modelVpk);
		
		for(int i=0;i<nScen;i++) {
			command.add(""+somaCurrents[i]);
		}
		if(nComp>1) {
			for(int i=0;i<nComp;i++) {
				command.add(""+rheoCurrents[i]);
			}
			for(int i=0;i<nComp;i++) {
				command.add(""+irCurrents[i]);
			}
			for(int i=0;i<nComp-1;i++) {
				command.add(""+spCurrents[i]);
			}
		}
		
		String brianOutputAsString = "";
		if(displayErrStream)
			System.out.println(command);
			
		ProcessBuilder pb = new ProcessBuilder(command);
		try {
			Process p = pb.start();		
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while(true){
				String retVal = in.readLine();				
				if(retVal!=null) {
					brianOutputAsString+=retVal;	
				}else{
					break;
				}	
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}	
		if(displayErrStream)
			System.out.println(brianOutputAsString);
		
	}
	public void invoke_forDisUtil(String fileNamePfx, String doShow){
		List<String> command = new ArrayList<String>();
		command.add("python");
	
		command.add("_2cFromJava_forDisUtil.py");		
		command.add(""+layoutcode);
		command.add(""+nScen);
		command.add(""+nComp);
		
		int nTotScens = nScen;
		
		for(int i=0;i<nTotScens;i++) {
			command.add(fileNamePfx+"_t"+i);			
			command.add(fileNamePfx+"_v"+i);
		}				
	
		command.add("C:\\Users\\sivav\\Projects\\TIMES\\output\\"+opFile);
		command.add(doShow);
		command.add(""+modelVr);
		command.add(""+modelVpk);
		
		for(int i=0;i<nScen;i++) {
			command.add(""+somaCurrents[i]);
		}		
		
		String brianOutputAsString = "";
		if(displayErrStream)
			System.out.println(command);
			
		ProcessBuilder pb = new ProcessBuilder(command);
		try {
			Process p = pb.start();		
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while(true){
				String retVal = in.readLine();				
				if(retVal!=null) {
					brianOutputAsString+=retVal;	
				}else{
					break;
				}	
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}	
		if(displayErrStream)
			System.out.println(brianOutputAsString);
		
	}
	public void setDisplayErrorStream(boolean displayErrStream){
		this.displayErrStream = displayErrStream;
	}
	
	public void setRheos(double[] rheos) {
		for(int i=0;i<rheos.length;i++) {
			rheoCurrents[i]=(int) rheos[i];
		}
	}
	
	public void setIrCurrents(double[] irIs) {
		for(int i=0;i<irIs.length;i++) {
			irCurrents[i]=(int) irIs[i];
		}
	}
	
	public void setSpCurrents(double[] spIs) {
		for(int i=0;i<spIs.length;i++) {
			spCurrents[i]=(int) spIs[i];
		}
	}
	
	public void setlayoutcode(int layoutcode) {
		this.layoutcode= layoutcode;
	}
	
	public void setColorCodeIdcs(int[] color_code_idcs) {
		this.colorCodeIdcs = color_code_idcs;
	}
}
