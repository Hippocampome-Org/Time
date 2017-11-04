package ec.app.izhikevich.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
/*
 * Reads sample parm set
 */
public class ParmSetReader {

	public static void main(String[] args) {
		String file_name = "input/parms_set.txt";
		
		try {
			FileReader fr = new FileReader(new File(file_name));
			BufferedReader br = new BufferedReader(fr);
			
			while(true) {				
			
				String line = br.readLine();
				if(line==null) break;
				StringTokenizer st = new StringTokenizer(line, ",");
				
				while(st.hasMoreTokens()) {
					String parm = st.nextToken();
					StringTokenizer st2 = new StringTokenizer(parm, "=");
					st2.nextToken();
					System.out.print(st2.nextToken() +"\t");
				}
				System.out.println();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
