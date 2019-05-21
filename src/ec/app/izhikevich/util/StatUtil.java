package ec.app.izhikevich.util;

import java.util.ArrayList;


public class StatUtil {

	public static double calculateSum(double[] array){
		double sum =0;
		if(array!=null) {
			for(int i=0;i<array.length;i++) {
				sum += array[i];
			}
		}
		return sum;
	}
	
	public static double calculateSumOfSquares(double[] array){
		double sum =0;
		if(array!=null) {
			for(int i=0;i<array.length;i++) {
				sum += (array[i]*array[i]);
			}
		}
		return sum;
	}
	
	public static double calculateSumOfProducts(double[] array1, double[] array2){
		double sum =0;
		if(array1!=null && array2!=null) {
			for(int i=0;i<array1.length;i++) {
				sum += (array1[i]*array2[i]);
			}
		}
		return sum;
	}
	public static double calculateMean(double[] array){
		double sum = 0;
		if(array!=null) {
			for(double a:array) sum += a;
			return sum/(1.0d*array.length);
		} else return 0;
	}
	
	public static double calculateMean(ArrayList<Double> array){
		double sum = 0;
		if(array!=null) {
			for(double a:array) sum += a;
			return sum/(array.size()*1.0d);
		} else return 0;
	}
	
	public static double calculateStandardDeviation(double[] array) {
		if(array!=null) {			
			double mean = calculateMean(array);
			double diff_square_sum = 0;
			for(double a: array) {
				diff_square_sum += (mean - a)*(mean - a);
			} return Math.sqrt(diff_square_sum/array.length);
		}else return 0;
	}
	
	public static double calculateStandardDeviation(double[] array, double mean) {
		if(array!=null) {			
			double diff_square_sum = 0;
			for(double a: array) {
				diff_square_sum += (mean - a)*(mean - a);
			} return Math.sqrt(diff_square_sum/array.length);
		}else return 0;
	}
	
	public static double calculateCoefficientOfVariation(double[] array) {
		if(array!=null) {			
			double mean = calculateMean(array);
			double diff_square_sum = 0;
			for(double a: array) {
				diff_square_sum += (mean - a)*(mean - a);
			} return Math.sqrt(diff_square_sum/array.length)/mean;
		}else return 0;
	}
	
	public static double calculateCoefficientOfVariation(double[] array, double mean) {
		double sd = calculateStandardDeviation(array, mean);
		return sd/mean;
	}
	
	public static double calculateSlope(double x1, double y1, double x2, double y2) {
		return (y2-y1)/(x2-x1);
	}
	
	public static double calculatePerpendicularDistance(double x1, double y1, double m, double b) {
		return Math.abs(y1-(m*x1)-b)/Math.sqrt((m*m)+1);
	}
	
	
	public static double calculateAvgOf0to1NormalizedErrors(double observed, double[] model, double minError, double maxError) {
		if(model==null) return Double.MAX_VALUE;		
		double[] errors =new double[model.length];
		for(int i=0;i<model.length;i++) {
			errors[i] = normalize0to1(Math.abs(model[i] - observed), minError, maxError);
		}		
		double avgError = StatUtil.calculateMean(errors);
		return avgError;
	}
	/*
	 * allows a range for observed
	 */
	public static double calculateAvgOf0to1NormalizedErrors(double observedMin, double observedMax, double[] model, double minError, double maxError) {
		if(model==null) return Double.MAX_VALUE;		
		double[] errors =new double[model.length];
		for(int i=0;i<model.length;i++) {
			errors[i] = normalize0to1(
					calculateDifferenceFromRange(model[i], observedMin, observedMax),
					minError, 
					maxError);
		}		
		double avgError = StatUtil.calculateMean(errors);
		return avgError;
	}
	
	public static double calculate0to1NormalizedError(double observed, double model, double minError, double maxError) {
		double error = normalize0to1(Math.abs(observed - model), minError, maxError);			
		return error;
	}
	/*
	 * normalize using exp rise
	 */
	public static double calculate0to1NormalizedErrorExp(double observed, double model, double expRate) {
		double error = normalize0to1Exp(Math.abs(observed - model),expRate);			
		return error;
	}
	
	
	public static double calculate0to1NormalizedError(double observedMin, double observedMax, double model, double minError, double maxError) {
		double error = normalize0to1(
				calculateDifferenceFromRange(model, observedMin, observedMax), 
				minError, 
				maxError);			
		return error;
	}
	public static double calculate0to1NormalizedErrorExp(double observedMin, double observedMax, double model, double expRate) {
		double error = normalize0to1Exp(
				calculateDifferenceFromRange(model, observedMin, observedMax), 
				expRate);			
		return error;
	}
	public static double normalize0to1(double value, double min, double max) {
		if(GeneralUtils.isCloseEnough(min, max, 0.001)) return 0.5;		
 		return (value-min)/(max-min);
	}
	public static double normalize0to1Exp(double value, double expRate) {
		return (1 - Math.exp(-value/expRate));
	}
	public static double calculateDifferenceFromRange(double value, double min, double max) {
		double difference = 0;
		if(value>=min && value<= max) {
			return difference;
		}
		if(value<min) {
			return min-value;
		}
		if(value>max) {
			return value-max;
		}
		return Double.MAX_VALUE;
	}
	
	public static double calculateSlopeOfRegression(double[] x, double[] y) {
		double slope =0;
		int n = x.length;
		if(n>1) {
			double sumX = calculateSum(x);
			double sumY = calculateSum(y);
			double sumXY = calculateSumOfProducts(x, y);
			double sumOfXSquared = calculateSumOfSquares(x);		
			double SquareOfSumX = sumX*sumX;
					
			slope = ((n*sumXY) - (sumX*sumY)) / ((n*sumOfXSquared) - SquareOfSumX);
		}
		return slope;
	}
	
	
	private static double getSSresidual(double[] reg, double[] res) {
		double ssRes=0;
		int max_length = (reg.length > res.length)? res.length:reg.length;
		for(int i=0; i<max_length; i++) {
			//double tempReg = (i<reg.length)? reg[i] : 0;
			//double tempRes = (i<res.length)? res[i] : 0;
			
			ssRes += (reg[i] - res[i]) * (reg[i] - res[i]);
		}
		
		return ssRes;
	}
	private static double getSSregression(double avgReg, double[] res) {
		double ssReg=0;
		
		for(int i=0; i<res.length; i++) {
			ssReg += (avgReg - res[i]) * (avgReg - res[i]);
		}
		
		return ssReg;
	}
	private static double getSStotal(double[] reg) {
		double meanReg = StatUtil.calculateMean(reg);
		double ssTotal=0;
		for(int i=0; i<reg.length; i++) {
			ssTotal += (reg[i] - meanReg) * (reg[i] - meanReg);
		}
		return ssTotal;
	}
	/*
	 * R2 calculation using ssResidual: (compares individual points)
	 */
	public static double getCoeffOfDetermination2(double[] observed, double[] model) {	
		double meanReg = StatUtil.calculateMean(observed);
		double ssTot = getSStotal(observed);
		double ssReg = getSSregression(meanReg, model);
		
		double rSquared = (ssReg / ssTot);		
		return rSquared;
	}
	/*
	 * R2 calculation using ssRegression (compares with exp average)
	 */
	public static double getCoeffOfDetermination(double[] observed, double[] model) {		
		double ssTot = getSStotal(observed);
		double ssRes = getSSresidual(observed, model);
		
		double rSquared = 1 - (ssRes / ssTot);		
		return rSquared;
	}
	
	public static double calculateAvgOfObsNormalizedErrors(double observed, double[] model) {
		if(model==null) return Double.MAX_VALUE;		
		double[] errors =new double[model.length];
		for(int i=0;i<model.length;i++) {
			if(GeneralUtils.isCloseEnough(observed, model[i], 0.000001))
				errors[i] = 0;
			else
			errors[i] = Math.abs(model[i] - observed) / Math.abs(observed);
		}		
		double avgError = StatUtil.calculateMean(errors);
		return avgError;
	}
	
	public static double calculateObsNormalizedError(double observed, double model) {
		//if(GeneralUtils.isCloseEnough(observed, model, 0.000001)) return 0;
		double error = Math.abs(model - observed);
		
	/*	if(observed < 0.0000001 && model < 0.0000001)
			return 0;		
		
		error = Math.abs(model - observed) / ((Math.abs(observed+model)/2d));
		*/
		
		
		/*else if (model > 0){
			error = Math.abs(model - observed) / Math.abs(model);
		} else {
			error = 0;
		}*/
			//if(error > 100000) System.out.println(model);
		//System.out.println(observed+"\t"+model+"\t"+error);
		//return error;
		
		
		return logNorm(error);
	}
	
	public static double calculateRelativeError(double observed, double model) {
		
		return logNorm(Math.abs(observed - model));
		/*if(observed > 0)
			return Math.abs(observed - model)/observed;
		else
			return Math.abs(observed - model)/((observed+model)/2);*/
		
	}
	
	private static double logNorm(double error){
		return Math.log10(error + 1);
	}
	//allows a range
		public static double calculateObsNormalizedAvgError(double observedMin, double observedMax, double[] model) {
			if(model==null) return Double.MAX_VALUE;		
			double[] errors =new double[model.length];
			double observedMid = (observedMin + observedMax)/2.0;
			for(int i=0;i<model.length;i++) {
				errors[i] = calculateDifferenceFromRange(model[i], observedMin, observedMax) / observedMid;
			}		
			double avgError = StatUtil.calculateMean(errors);
			return avgError;
		}
		//allows a range
			public static double calculateObsNormalizedError(double observedMin, double observedMax, double model) {
				double error;
				//double observedMid = (observedMin + observedMax)/2.0;
				error = calculateDifferenceFromRange(model, observedMin, observedMax);// / observedMid;
				
				return logNorm(error);
			}
			
			public static double[] scaleSimple(double[] values, double factor){
				double[] scaled = new double[values.length];
				for(int i=0;i<scaled.length;i++){
					scaled[i] = values[i]/factor;
				}
				return scaled;
			}
			public static double[] shiftLeftToZeroAndScaleSimple(double[] values, double factor){
				double[] scaled = new double[values.length];
				for(int i=0;i<scaled.length;i++){
					scaled[i] = (values[i]-values[0])/factor;
				}
				return scaled;
			}
			
			public static int log(int x, int base)
			{
			    return (int)Math.floor((Math.log(x) / Math.log(base)));
			}
}
