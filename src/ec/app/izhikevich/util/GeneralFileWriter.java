package ec.app.izhikevich.util;

import java.io.FileWriter;

public class GeneralFileWriter {

	public static void write(String fileName, double[] array) {
		try{
			FileWriter fw = new FileWriter(fileName);
			for(int i=0;i<array.length;i++){
				fw.write(String.valueOf(array[i])+"\n");
			}
			fw.flush();fw.close();
		}catch(Exception e){
			e.printStackTrace();
		}			
	}
	
	public static void write(String fileName, double[] array1, double[] array2) {
		try{
			FileWriter fw = new FileWriter(fileName);
			for(int i=0;i<array1.length;i++){
				
				fw.write(String.valueOf(array1[i])+","+array2[i]+"\n");
			}
			fw.flush();fw.close();
		}catch(Exception e){
			e.printStackTrace();
		}			
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
