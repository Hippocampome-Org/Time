package ec.app.izhikevich.evaluator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;





public class test {

	public static void main(String[] args) {
		Map<String, ArrayList<Double>> pcarevals = new HashMap<>();
		
		String fileName = "C:\\Users\\Siva\\TIMES\\theory\\bif.dat";
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String str = br.readLine();
			
			while(str!=null){
				StringTokenizer st = new StringTokenizer(str);
				String I=""; 
				double u;
				
				while(st.hasMoreTokens()) {
					I = st.nextToken();
					u = Double.parseDouble(st.nextToken());
					if(pcarevals.containsKey(I)){
						
					}
					//pcarevals.put(I, u);
				}
				br.close();
				
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
