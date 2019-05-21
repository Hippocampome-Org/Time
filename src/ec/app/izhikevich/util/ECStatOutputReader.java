package ec.app.izhikevich.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ECStatOutputReader {

	//public static final String fileNamePrefix = "output/job.";
	
	
	public static float readBestFitness(String fileName, int lineNo) {
		float fitness = Float.MAX_VALUE;
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String str = null;
			for(int i=0;i<lineNo;i++) {
				str = br.readLine();
			}
			StringTokenizer st = new StringTokenizer(str);
			String token = null;
			while(st.hasMoreTokens()) {
				token = st.nextToken();
				token = st.nextToken();
				fitness = Float.parseFloat(token);
			}
			br.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fitness;
	}
	/*
	 * when fitness line is not given, simply read the last but one line for fitness 
	 */
	public static float readBestFitness(String fileName) {
		float fitness = Float.MAX_VALUE;
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String str = br.readLine();
			String lastLine = null;
			String lastButOne = null;
			String lastButTwo = null;
			while(str!=null) {
				str = br.readLine();
				lastButTwo = lastButOne;
				lastButOne=lastLine;
				lastLine=str;				
			}
			//System.out.println(lastButTwo);
			StringTokenizer st = new StringTokenizer(lastButTwo);
			String token = null;
			while(st.hasMoreTokens()) {
				token = st.nextToken();
				token = st.nextToken();
				fitness = Float.parseFloat(token);
			}
			br.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fitness;
	}
	
	public static double[][] readBestSolutionS(String fileName, int nParms, int NSols) {
		double[][] parms = new double[NSols][nParms];
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String str = br.readLine();
			
			String[] lineHolders = new String[NSols*4];			
			
			//System.out.println("**test** Nsols: "+NSols);
			
			while(str!=null) {
				str = br.readLine();
				for(int i=1;i<lineHolders.length;i++){
					lineHolders[i-1]=lineHolders[i];
				}
				lineHolders[lineHolders.length-1]=str;				
			}
			
			
					
			for(int i=0;i<NSols;i++){
				StringTokenizer st = new StringTokenizer(lineHolders[i*4 + 2]);			
				String token = null;
				
				int ii=0;
				while(st.hasMoreTokens()) {
					token = st.nextToken();
					parms[i][ii++] = Double.parseDouble(token);
				}
			}
			br.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return parms;
	}
	public static int findNGen(String fileName) {
		int nGen = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String str = br.readLine();			
			while(str!=null) {
				str = br.readLine();				
				nGen++;				
			}			
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nGen;
	}
	/*
	 * reads best solution from _full output of ECJ
	 */
	public static float[] readBestSolution(String fileName, int lineNo, int nParms) {
		float[] parms = new float[nParms];
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String str = null;
			for(int i=0;i<lineNo;i++) {
				str = br.readLine();
			}
			StringTokenizer st = new StringTokenizer(str);
			String token = null;
			int i=0;
			while(st.hasMoreTokens()) {
				token = st.nextToken();
				parms[i++] = Float.parseFloat(token);
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return parms;
	}
/*
 * when fitline not given
 */
	public static double[] readBestSolution(String fileName, int nParms) {
		double[] parms = new double[nParms];
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String str = br.readLine();
			String lastLine = null;
			String lastButOne = null;
			String lastButTwo = null;
			while(str!=null) {
				str = br.readLine();
				lastButTwo = lastButOne;
				lastButOne=lastLine;
				lastLine=str;				
			}
			//System.out.println(lastButOne+"--"+lastButTwo);
			StringTokenizer st = new StringTokenizer(lastButOne);			
			String token = null;
			int i=0;
			while(st.hasMoreTokens()) {
				token = st.nextToken();
				parms[i++] = Double.parseDouble(token);
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return parms;
	}
	
	public static double[][] readParetoBestSolutions(String fileName, int nSolns, int nParms) {
		double[][] parms = new double[nSolns][nParms];
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String str = " ";
			while(!str.equals(" PARETO FRONTS")) {
				str = br.readLine();
			}
			
			int readSlnCnt=-1;
			while(readSlnCnt <nSolns-1){
				str = br.readLine();
				StringTokenizer st = new StringTokenizer(str);
				String token = null;
				int i=0;
				while(st.hasMoreTokens()) {
					token = st.nextToken();
					if(i==0){
						if(!isNumeric(token)) break;
						else readSlnCnt++;
					}										
					parms[readSlnCnt][i++] = Double.parseDouble(token);
				}
				//System.out.println(readSlnCnt);
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return parms;
	}
	
	public static double[][] readParetoFronts(String filName, int nObj) {
		double[][] frontsArray = null;
		ArrayList<Double[]> fronts = new ArrayList<>();
	
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(filName));
			//System.out.println(files[0]);
			String str = br.readLine();
			while(str != null){
				Double[] line = new Double[nObj];				
				StringTokenizer st = new StringTokenizer(str);
				String token = null;
				while(st.hasMoreTokens()) {
					for(int o=0;o<nObj;o++){
						token = st.nextToken();
						line[o] = Double.parseDouble(token);
					}					
					fronts.add(line);
				}				
				str = br.readLine();
				//System.out.println(line[0]+"\t"+line[1]);
			}
			
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		frontsArray = new double[fronts.size()][nObj];
		for(int i=0;i<frontsArray.length;i++)
		{
			for(int o=0;o<nObj;o++){
				frontsArray[i][o]=fronts.get(i)[o];
			}
		}
		return frontsArray;
	}		
		
	public static void main(String[] args){
		String fileName = "output\\10_2_3\\B3\\2-043\\mcmp1\\job.0.Full";
		
		//ECStatOutputReader.readBestFitnessES(fileName, 50, 5);
	}
	
	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    double d = Double.parseDouble(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}

}
