package ec.app.izhikevich.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

public class GeneralFileReader {

	public static double[] readDoublesSepByLine(String fileName) {
		ArrayList<Double> values = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String str = br.readLine();			
			while(str!=null) {
				values.add(Double.parseDouble(str));
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
		return GeneralUtils.listToArrayDouble(values);
	}
	/*
	 * to trim ; from allV files of CARL output
	 */
	public static double[] readAllVCarlOPFile(String fileName) {
		ArrayList<Double> values = new ArrayList<>();
				
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String str = br.readLine();			
			while(str!=null) {
				str = StringUtils.strip(str, ";");
				values.add(Double.parseDouble(str));
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
		return GeneralUtils.listToArrayDouble(values);
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
