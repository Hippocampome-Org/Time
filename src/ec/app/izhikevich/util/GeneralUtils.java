package ec.app.izhikevich.util;

import java.util.ArrayList;

import ec.util.MersenneTwisterFast;

public class GeneralUtils {

	public static void shuffleArray(int[] ar)
	  {
	    MersenneTwisterFast rnd = new MersenneTwisterFast();
	    for (int i = ar.length - 1; i > 0; i--)
	    {
	      int index = rnd.nextInt(i + 1);
	      // Simple swap
	      int a = ar[index];
	      ar[index] = ar[i];
	      ar[i] = a;
	    }
	  }
	
	public static boolean isCloseEnough(double value1, double value2, double threshold) {
		if(Math.abs(value2 - value1) <= threshold) return true;
		else return false;
	}
	public static ArrayList<Double> arrayToListDouble(double[] array){
		ArrayList<Double> list = new ArrayList<>();		
		for(int i=0;i<array.length;i++){
			list.add(array[i]);	
		}
		return list;
	}
	
	public static double[] listToArrayDouble(ArrayList<Double> list) {
		double[] array = new double[list.size()];
		int i=0;
		for(double element: list) {
			array[i++] = element;
		}
		return array;
	}
	
	public static void displayArray(double[] array) {
		for(double d:array) {
			System.out.print(formatOneDecimal(d)+"\t");
		}System.out.println();
	}
	public static void displayArray(boolean[] array) {
		for(boolean b:array) {
			System.out.print(b+"\t");
		}System.out.println();
	}
	public static void displayArrayVertical(double[] array) {
		for(double d:array) {
			System.out.print(formatThreeDecimal(d)+"\n");
		}System.out.println();
	}
	public static void displayArray(float[] array) {
		for(float d:array) {
			System.out.print(formatTwoDecimal(d)+"\t");
		}System.out.println();
	}
	public static void displayArrayUnformatWithComma(double[] array) {
		for(double d:array) {
			System.out.print(d+", ");
		}System.out.println();
	}
	public static void displayArrayUnformatWithSpace(double[] array) {
		for(double d:array) {
			System.out.print(d+"\t");
		}System.out.println();
	}
	public static void displayArrayUnformatWithSpace(double[] array, int idx1, int idx2) {
		
		System.out.print(array[idx1]+"\t"+array[idx2]+"\t");
		System.out.println();
	}
	public static String flattenArray(double[] array) {
		if (array==null) return "NULL";
		String string = "";
		for(double d:array) {
			string +=formatTwoDecimal(d)+", ";
		}return string;
	}
	
	public static void displayArray(int[] array) {
		for(int d:array) {
			System.out.print(d+"\t");
		}System.out.println();
	}
	public static void displayArraySameLine(double[] array) {
		for(double d:array) {
			System.out.print(d+"\t");
		}
	}

	
	public static void displayArray(double[][] array2d) {
		for(double[] array1d:array2d) {
			displayArray(array1d);
		}
	}
	public static void displayArray(float[][] array2d) {
		for(float[] array1d:array2d) {
			displayArray(array1d);
		}
	}
	public static void display2DArrayVertical(double[][] array2d) {
		for(int i=0;i<array2d[array2d.length-1].length;i++)
		{
			
			String col1 = "";
			String col2 = "";
			if(i<array2d[0].length){
				col1 += formatTwoDecimal(array2d[0][i]);
			}
			if(i<array2d[1].length){
				col2 += formatTwoDecimal(array2d[1][i]);
			};
			
					
			System.out.print(col1 +"\t"+col2+"\n");//+formatTwoDecimal(array2d[2][i])+"\n");
		}
	}
	public static void display2DArrayVerticalUnRounded(double[][] array2d) {
		for(int i=0;i<array2d[array2d.length-1].length;i++)
		{
			
			String col1 = "";
			String col2 = "";
			if(i<array2d[0].length){
				col1 += (array2d[0][i]);
			}
			if(i<array2d[1].length){
				col2 += (array2d[1][i]);
			};
			
					
			System.out.print(col1 +"\t"+col2+"\n");//+formatTwoDecimal(array2d[2][i])+"\n");
		}
	}
	public static String formatOneDecimal(double string) {
		return String.format("%.1f", string);
	}
	public static String formatTwoDecimal(double string) {
		return String.format("%.2f", string);
	}
	public static String formatThreeDecimal(double string) {
		return String.format("%.3f", string);
	}
	public static double findMin(double[] array) {
		double min = Double.MAX_VALUE;
		if(array!=null) {
		for(int i=0;i<array.length;i++) {
			if(array[i] < min) min = array[i];
		}
		}
		return min;
	}
	
	public static float findMin(float[] array) {
		float min = Float.MAX_VALUE;
		if(array!=null) {
		for(int i=0;i<array.length;i++) {
			if(array[i] < min) min = array[i];
		}
		}
		return min;
	}
	public static double findMax(double[] array) {
		double max = -Double.MAX_VALUE;
		if(array!=null) {
		for(int i=0;i<array.length;i++) {
			if(array[i] > max) max = array[i];
		}
		}
		return max;
	}
	public static int findMaxIdx(double[] array) {
		double max = -Double.MAX_VALUE;
		int max_id = -7;
		if(array!=null) {
		for(int i=0;i<array.length;i++) {
			if(array[i] > max) {
				max = array[i];
				max_id = i;
			}
		}
		}
		return max_id;
	}
	public static int findMaxIdx(float[] array) {
		float max = -Float.MAX_VALUE;
		int max_id = -7;
		if(array!=null) {
		for(int i=0;i<array.length;i++) {
			if(array[i] > max) {
				max = array[i];
				max_id = i;
			}
		}
		}
		return max_id;
	}
	
	public static double[] roundOff(double[] array){
		double[] integerArray = new double[array.length];
		for(int i=0;i<array.length;i++){			
			integerArray[i] = Math.ceil(array[i]);
		}
		return integerArray;
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
 
	public static double[] floatToDouble(float[] floatArray){
		double[] doubleArray = null;
		
		if(floatArray!=null) {
			doubleArray = new double[floatArray.length];
			for(int i=0;i<floatArray.length;i++){
				doubleArray[i] = floatArray[i];
			}
		}
		return doubleArray;
	}
	
	public static boolean doesContain(String str, String subStr){
		if(str.contains(subStr)){
			return true;
		}
		return false;
	}
	public static double[] getFirstNelements(double[] array, int n) {			
		double[] firstNIsis = null;
		if(array!=null) {
			int max_length = (array.length < n)? array.length : n;
			firstNIsis = new double[max_length];
			for(int i=0; i<firstNIsis.length; i++) {
				firstNIsis[i] = array[i];
			}
		}
		return firstNIsis;
	}
	public static double[] getLastNelements(double[] array, int startIdx){
		double[] isis = null;
		if(array!=null){
			isis = new double[array.length-startIdx];
			for(int i=0; i<array.length; i++) {				
				if(i>=startIdx){
					isis[i-startIdx] = array[i];	
				}												
			}
		}		
		return isis;
	}
	public static void main(String[] args){
		String str = "ASP. ; TSTUT.ASP";
		String sub = "TSTUTs";
		
		System.out.println(doesContain(str, sub));
		
		double[] array = new double[]{10.1, 12.135, 15.5678};
		displayArray(roundOff(array));
	}
}
