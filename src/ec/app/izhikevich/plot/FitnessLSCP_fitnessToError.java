package ec.app.izhikevich.plot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

public class FitnessLSCP_fitnessToError {
	static String fileNamePfx = "C:/gnu/10_2_3_b1_2003/";
	static String fileName = fileNamePfx+"fit_z.dat";
	static int nLines = 101;
	static double[][] holder = new double[nLines][];
	public static void main(String[] args) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			for(int i=0;i<nLines;i++){
				int j=0;
				String str = br.readLine();			
				StringTokenizer st = new StringTokenizer(str, " ");
				holder[i] = new double[st.countTokens()];
				
				while(st.hasMoreTokens()){
					holder[i][j++]=Double.parseDouble(st.nextToken());
				}
			}			
			br.close();
			
			flipFitToError();
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileNamePfx+"err_z.dat"));
			for(int i=0;i<holder.length;i++){
				for(int j=0;j<holder[i].length;j++){
					bw.write(holder[i][j]+" ");
				}
				bw.write("\n");
			}
			bw.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	static void flipFitToError(){
		for(int i=0;i<holder.length;i++){
			for(int j=0;j<holder[i].length;j++){
				
					holder[i][j] = -1*holder[i][j];
				
			}			
		}
	}

}
