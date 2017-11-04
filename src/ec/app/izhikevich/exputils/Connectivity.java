package ec.app.izhikevich.exputils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Connectivity {
	private static final String file_pfx="input\\AK\\";
	private static final String file1 = "Netlist.csv";
	
	
	public static HashMap<String, Integer> readNetlist(String fileName, String region){
		HashMap<String, Integer> netList = new HashMap<>();	
		 String str = null;
		 int ln = 0;
		 try{
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			str = br.readLine();
			ln++;
			str = br.readLine();
			ln++;
			while(str!=null) {					
				String[] values = str.split(",",-1);
				if(values[0].equals(region) && identifyRegion(values[2]).equals(region)){
					if(!netList.containsKey(values[1])){
						netList.put(values[1], 1);	
					}else{
						int count = netList.get(values[1]);
						netList.put(values[1], count+1);	
					}	
				}				
				str = br.readLine();
				ln++;
			}
			
			br.close();	
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(str+" "+ln);
		}
		return netList;
		 
	}
	
	public static void displayMap(HashMap<String, Integer> map){
		Iterator it = map.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry pair = (Map.Entry)it.next();
			String nt = (String)pair.getKey();
			int count = (int)pair.getValue();
			System.out.println(nt +"\t"+count);
		}
	}
	
	private static String identifyRegion(String nt){
		String DG = "DG"; 
		String CA3 = "CA3";
		String CA2 = "CA2";
		String CA1 = "CA1";
		String cs = "";
		if(nt.contains(CA3)){
			return CA3;
		}
		if(nt.contains(CA2))
			return CA2;
		if(nt.contains(CA1))
			return CA1;
		if(nt.contains(DG))
			return DG;
		return DG;
		
	}
	public static void main(String[] args) {
		displayMap(readNetlist(file_pfx+file1, "CA1"));

	}

}
