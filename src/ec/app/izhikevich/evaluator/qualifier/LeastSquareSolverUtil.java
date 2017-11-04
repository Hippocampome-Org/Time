package ec.app.izhikevich.evaluator.qualifier;

import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.commons.math3.util.Pair;

import ec.app.izhikevich.util.GeneralFileReader;
import ec.app.izhikevich.util.GeneralUtils;
import ec.app.izhikevich.util.StatUtil;
public class LeastSquareSolverUtil {

	double[] X;
	double[] Y;
	
	boolean timer;
	long time;
	public LeastSquareSolverUtil(double[] x, double[] y){
		this.X = x;
		this.Y = y;
		timer = false;
	}
	public LeastSquareSolverUtil(double[] x, double[] y, boolean timer){
		this.X = x;
		this.Y = y;
		this.timer = timer;
		this.time = 0;
	}
	public SolverResultsStat solveFor2ParmsSimpleRegression(double initM1, double initC1){
		long timeStart = 0;
		if(timer) timeStart = System.nanoTime();
		
		SimpleRegression sr = new SimpleRegression(true);		
		for(int i=0;i<X.length;i++){
			sr.addData(X[i],Y[i]);
		}
		double[] fitY = new double[X.length];
		for(int i=0;i<fitY.length;i++){
			fitY[i] = sr.predict(X[i]);
		}
		SolverResultsStat stat = new SolverResultsStat(sr.getSlope(), sr.getIntercept(), fitY );
		
		if(timer)  {
			  TimeUnit unit = TimeUnit.MILLISECONDS;
			  time += unit.convert(System.nanoTime()-timeStart, TimeUnit.NANOSECONDS);
			  stat.setTimeTaken(time);
		  }
		  
		  return stat;		
	}
	public SolverResultsStat solveFor1Parm(double initC1){	
		long timeStart = 0;
		if(timer) timeStart = System.nanoTime();
		
		_1ParmFunction _1parmfn = new _1ParmFunction();
		
		
		 double c = Double.MAX_VALUE;
		 double[] points = null;
		 SolverResultsStat stat = null;
		 LeastSquaresOptimizer.Optimum optimum = null;
		 
			try{
				LeastSquaresProblem problem = new LeastSquaresBuilder().
                        start(new double[] { initC1 }).
                        model(_1parmfn).
                        target(Y).
                        lazyEvaluation(false).
                        maxEvaluations(1000).
                        maxIterations(1000).
                        build();
				
				optimum = new LevenbergMarquardtOptimizer().optimize(problem);		  
				 
				 c = optimum.getPoint().getEntry(0);
				 points = getAbsDouble(optimum.getResiduals());
				 stat = new SolverResultsStat(c,points);
				 stat.setSolverStats(optimum.getRMS(), optimum.getEvaluations(), optimum.getIterations());
				  
			}catch(TooManyEvaluationsException e){
				stat = new SolverResultsStat(0,points);
				stat.setSolverStats(-1,-1,-1);
			}
			
			
			  
		  if(timer)  {
			  TimeUnit unit = TimeUnit.MILLISECONDS;
			  time += unit.convert(System.nanoTime()-timeStart, TimeUnit.NANOSECONDS);
			  stat.setTimeTaken(time);
		  }
		  
		  return stat;		        
	}
	public SolverResultsStat solveFor2Parms(double initM1, double initC1){	
		long timeStart = 0;
		if(timer) timeStart = System.nanoTime();
		
		_2ParmFunction _2parmfn = new _2ParmFunction();
		LeastSquaresProblem problem = new LeastSquaresBuilder().
	                                start(new double[] { initM1, initC1 }).
	                                model(_2parmfn).
	                                target(Y).
	                                lazyEvaluation(false).
	                                maxEvaluations(1000).
	                                maxIterations(1000).
	                                build();
		
		double m, c;
		double[] points = null;
		SolverResultsStat stat = null;
		
		try{
			LeastSquaresOptimizer.Optimum optimum = new LevenbergMarquardtOptimizer().
					  optimize(problem);
			  m = optimum.getPoint().getEntry(0);
			  c = optimum.getPoint().getEntry(1);
			 
			  points = getAbsDouble(optimum.getResiduals());
			  stat = new SolverResultsStat(m,c,points);
			  stat.setSolverStats(optimum.getRMS(), optimum.getEvaluations(), optimum.getIterations());
			
		}catch(Exception e){
			//e.printStackTrace();
			//GeneralUtils.displayArray(Y);
			
			stat = new SolverResultsStat(0,0, points);
			stat.setSolverStats(-1,-1,-1);
		}
		 
		  
		  if(timer)  {
			  TimeUnit unit = TimeUnit.MILLISECONDS;
			  time += unit.convert(System.nanoTime()-timeStart, TimeUnit.NANOSECONDS);
			  stat.setTimeTaken(time);
		  }
		  
		  return stat;		        
	}
	public SolverResultsStat solveFor3Parms(double initM1, double initC1, double initC2){	
		long timeStart = 0;
		if(timer) timeStart = System.nanoTime();
		_3ParmFunction _3parmfn = new _3ParmFunction();
		LeastSquaresProblem problem = new LeastSquaresBuilder().
	                                start(new double[] { initM1, initC1, initC2 }).
	                                model(_3parmfn).
	                                target(Y).
	                                lazyEvaluation(false).
	                                maxEvaluations(1000).
	                                maxIterations(1000).
	                                build();
		double m1, c1, c2;
		double[] points = null;
		SolverResultsStat stat = null;
		
		try{
			LeastSquaresOptimizer.Optimum optimum = new LevenbergMarquardtOptimizer().
					  optimize(problem);
			m1 = optimum.getPoint().getEntry(0);
			c1 = optimum.getPoint().getEntry(1);
			c2 = optimum.getPoint().getEntry(2);
			
			points = getAbsDouble(optimum.getResiduals());
			
			stat = new SolverResultsStat(m1,c1, c2, points, _3parmfn.getBreakPoint());
			stat.setSolverStats(optimum.getRMS(), optimum.getEvaluations(), optimum.getIterations());
			
		}catch(Exception e){
			//e.printStackTrace();
			//GeneralUtils.displayArray(Y);
						
			stat = new SolverResultsStat(0,0,0, points, -1);
			stat.setSolverStats(-1,-1,-1);
		}
		
		 
		  if(timer)  {
			  TimeUnit unit = TimeUnit.MILLISECONDS;
			  time += unit.convert(System.nanoTime()-timeStart, TimeUnit.NANOSECONDS);
			  stat.setTimeTaken(time);
		  }
		  return stat;		        
	}
	
	public SolverResultsStat solveFor4Parms(double initM1, double initC1, double initM2, double initC2){	
		long timeStart = 0;
		if(timer) timeStart = System.nanoTime();
		_4ParmFunction _4parmfn = new _4ParmFunction();
		LeastSquaresProblem problem = new LeastSquaresBuilder().
	                                start(new double[] { initM1, initC1, initM2, initC2 }).
	                                model(_4parmfn).
	                                target(Y).
	                                lazyEvaluation(false).
	                                maxEvaluations(1000).
	                                maxIterations(1000).
	                                build();
		 
		  
		  double m1, m2, c1, c2;
		  double[] points = null;
		  SolverResultsStat stat = null;
			
			try{
				LeastSquaresOptimizer.Optimum optimum = new LevenbergMarquardtOptimizer().
						  optimize(problem);
				m1 = optimum.getPoint().getEntry(0);
				  c1 = optimum.getPoint().getEntry(1);
				  m2 = optimum.getPoint().getEntry(2);
				  c2 = optimum.getPoint().getEntry(3);
				  
				 points = getAbsDouble(optimum.getResiduals());
				
				 stat = new SolverResultsStat(m1,c1, m2, c2, points, _4parmfn.getBreakPoint());
				 stat.setSolverStats(optimum.getRMS(), optimum.getEvaluations(), optimum.getIterations());
				
			}catch(Exception e){
				//e.printStackTrace();
				//GeneralUtils.displayArray(Y);
				
				
				stat = new SolverResultsStat(0,0,0,0, points, -1);
				stat.setSolverStats(-1,-1,-1);
			}
		  if(timer)  {
			  TimeUnit unit = TimeUnit.MILLISECONDS;
			  time += unit.convert(System.nanoTime()-timeStart, TimeUnit.NANOSECONDS);
			  stat.setTimeTaken(time);
		  }
		  return stat;		        
	}
	
	public SolverResultsStat solveForLog(double initX){	
		long timeStart = 0;
		if(timer) timeStart = System.nanoTime();
		
		LogFunction logFn = new LogFunction();
		LeastSquaresProblem problem = new LeastSquaresBuilder().
	                                start(new double[] { initX }).
	                                model(logFn).
	                                target(Y).
	                                lazyEvaluation(false).
	                                maxEvaluations(1000).
	                                maxIterations(1000).
	                                build();
		  LeastSquaresOptimizer.Optimum optimum = new LevenbergMarquardtOptimizer().
				  optimize(problem);		  
		 
		  double c = optimum.getPoint().getEntry(0);
		  double[] points = getAbsDouble(optimum.getResiduals());
		 
		  SolverResultsStat stat = new SolverResultsStat(c,points);
		  stat.setSolverStats(optimum.getRMS(), optimum.getEvaluations(), optimum.getIterations());
		  
		  if(timer)  {
			  TimeUnit unit = TimeUnit.MILLISECONDS;
			  time += unit.convert(System.nanoTime()-timeStart, TimeUnit.NANOSECONDS);
			  stat.setTimeTaken(time);
		  }
		  
		  return stat;		        
	}
	
	public static void main(String[] args) {
		String fileName = "input/other_test/X.txt";
		double[] X = GeneralFileReader.readDoublesSepByLine(fileName);
		fileName = "input/other_test/Y.txt";
		double[] Y = GeneralFileReader.readDoublesSepByLine(fileName);
	//	GeneralUtils.displayArray(Y);
		
		double scaleFactor = GeneralUtils.findMin(Y);
		X = StatUtil.shiftLeftToZeroAndScaleSimple(X, scaleFactor);
		Y = StatUtil.scaleSimple(Y, scaleFactor);
		
		LeastSquareSolverUtil l = new LeastSquareSolverUtil(X,Y,true);
		SolverResultsStat stat1 = l.solveFor1Parm(1);
		SolverResultsStat stat2 = l.solveFor2Parms(0, 1);
		SolverResultsStat stat3 = l.solveFor3Parms(stat2.getM1(), 1, 1);
		SolverResultsStat stat4 = l.solveFor4Parms(stat3.getM1(), 1, 0, 1);
		StatAnalyzer.display_stats = true;
		
		double[] stat = new double[4];
		System.out.println("1 - 2:");
		if(!StatAnalyzer.isSignificantImprovement(stat1.getFitResidualsAbs(), stat3.getFitResidualsAbs(), 1, 3, stat))
		{
			System.out.println("1 - 3:");
			if(!StatAnalyzer.isSignificantImprovement(stat1.getFitResidualsAbs(), stat3.getFitResidualsAbs(), 1, 3, stat)){
				System.out.println("1 - 4:");
				if(!StatAnalyzer.isSignificantImprovement(stat1.getFitResidualsAbs(), stat4.getFitResidualsAbs(), 1, 4, stat)){
					System.out.println("1p - NASP\n\n");
				}
			}
			System.out.println("1p - NASP");
		} else{
			System.out.println("2 - 3:");
			if(!StatAnalyzer.isSignificantImprovement(stat2.getFitResidualsAbs(), stat3.getFitResidualsAbs(), 2, 3, stat)){
				System.out.println("2 - 4:");
				if(!StatAnalyzer.isSignificantImprovement(stat2.getFitResidualsAbs(), stat4.getFitResidualsAbs(), 2, 4, stat)){
					System.out.println("2p - ASP\n\n");	
				}
				System.out.println("2p - ASP");
			} else{
				System.out.println("3 - 4:");
				if(!StatAnalyzer.isSignificantImprovement(stat3.getFitResidualsAbs(), stat4.getFitResidualsAbs(), 3, 4, stat)){
					System.out.println("3p - ASP.NASP\n\n");	
				}else{
					System.out.println("4p - ASP.ASP\n\n");	
				}
			}
		} 
				
		
	/*
		if(StatAnalyzer.isSignificantImprovement(stat2.getFitResidualsAbs(), stat4.getFitResidualsAbs())){
			System.out.println("2p-4p : yes!");
		}else{
			System.out.println("2p-4p : no!");
		}
		*/
		System.out.println("1p: "); stat1.display();
		System.out.println("\n2p: "); stat2.display();
		System.out.println("\n3p: "); stat3.display();
		System.out.println("\n4p: "); stat4.display();
	}
	
	private double[] getAbsDouble(RealVector vectorOfPoints){
		double[] absPoints = new double[vectorOfPoints.getDimension()];
		for(int i=0;i<absPoints.length;i++){
			absPoints[i] = Math.abs(vectorOfPoints.getEntry(i));
		}
		return absPoints;
	}

class _1ParmFunction implements MultivariateJacobianFunction{
		@Override
		public Pair<RealVector, RealMatrix> value(final RealVector parms) {
	        double c = parms.getEntry(0);
	        
	        RealVector value = new ArrayRealVector(X.length);
	        RealMatrix jacobian = new Array2DRowRealMatrix(X.length, 1);

	        for (int i = 0; i < X.length; ++i) {		        	  
	            double modelY =c;
	            value.setEntry(i, modelY);	            
	            // derivative with respect c
	            jacobian.setEntry(i, 0, 1);
	        }
	        return new Pair<RealVector, RealMatrix>(value, jacobian);
	    }    
		
	}	
class LogFunction implements MultivariateJacobianFunction{
	@Override
	public Pair<RealVector, RealMatrix> value(final RealVector parms) {
        double x = parms.getEntry(0);
        
        RealVector value = new ArrayRealVector(X.length);
        RealMatrix jacobian = new Array2DRowRealMatrix(X.length, 1);

        for (int i = 0; i < X.length; ++i) {		        	  
            double modelY = Math.log(1+x);
            value.setEntry(i, modelY);	            
            // derivative with respect c
            jacobian.setEntry(i, 0, (1d/(1d+x)));
        }
        return new Pair<RealVector, RealMatrix>(value, jacobian);
    }    
	
}	
class _2ParmFunction implements MultivariateJacobianFunction{
	@Override
	public Pair<RealVector, RealMatrix> value(final RealVector parms) {

        double m = parms.getEntry(0);
        double c = parms.getEntry(1);
        
        RealVector value = new ArrayRealVector(X.length);
        RealMatrix jacobian = new Array2DRowRealMatrix(X.length, 2);

        for (int i = 0; i < X.length; ++i) {		        	  
            double modelY = m*X[i]+c;
            value.setEntry(i, modelY);
            // derivative with respect to m
            jacobian.setEntry(i, 0, X[i]);
            // derivative with respect c
            jacobian.setEntry(i, 1, 1);
        }
        return new Pair<RealVector, RealMatrix>(value, jacobian);
    }    
	
}
class _3ParmFunction implements MultivariateJacobianFunction{
	private int breakPoint;
	
	@Override
	public Pair<RealVector, RealMatrix> value(final RealVector parms) {

        double m1 = parms.getEntry(0);
        double c1 = parms.getEntry(1);
        double c2 = parms.getEntry(2);
        
        RealVector value = new ArrayRealVector(X.length);
        RealMatrix jacobian = new Array2DRowRealMatrix(X.length, 3);

        breakPoint = -1;
        for (int i = 0; i < X.length; ++i) {		        	  
            double modelY_1 = m1*X[i]+c1;
            double modelY_2 = c2;
            
            if(modelY_1 < modelY_2 || GeneralUtils.isCloseEnough(modelY_1, modelY_2, 0.00001)){
            	/*if(GeneralUtils.isCloseEnough(modelY_1, modelY_2, 0.1)){
            		if(breakPoint<0){
                		breakPoint = i;
                	}
            	}*/
            	 value.setEntry(i, modelY_1); 
            	 // derivative of Y1 with respect to m1
                 jacobian.setEntry(i, 0, X[i]);
                 // derivative with respect c1
                 jacobian.setEntry(i, 1, 1);
                 // derivative with respect c2
                 jacobian.setEntry(i, 2, 0);
            }else{
            /*	if(GeneralUtils.isCloseEnough(modelY_1, modelY_2, 0.1)){
            		if(breakPoint<0){
                		breakPoint = i;
                	}
            	}*/
            	if(breakPoint<0){
            		breakPoint = i;
            	}
            	value.setEntry(i, modelY_2); 
            	// derivative of Y2 with respect to m1
                jacobian.setEntry(i, 0, 0);
                // derivative with respect c1
                jacobian.setEntry(i, 1, 0);
                // derivative with respect c2
                jacobian.setEntry(i, 2, 1);
            }
           
           
        }
        return new Pair<RealVector, RealMatrix>(value, jacobian);
    }

	public int getBreakPoint() {
		return breakPoint;
	}   
	
}

class _4ParmFunction implements MultivariateJacobianFunction{
	private int breakPoint;
	public int getBreakPoint() {
		return breakPoint;
	}   
	
	@Override
	public Pair<RealVector, RealMatrix> value(final RealVector parms) {

        double m1 = parms.getEntry(0);
        double c1 = parms.getEntry(1);
        double m2 = parms.getEntry(2);
        double c2 = parms.getEntry(3);
        
        RealVector value = new ArrayRealVector(X.length);
        RealMatrix jacobian = new Array2DRowRealMatrix(X.length, 4);

        breakPoint = -1;
        for (int i = 0; i < X.length; ++i) {		        	  
            double modelY_1 = m1*X[i]+c1;
            double modelY_2 = m2*X[i]+c2;
            
            if(modelY_1 < modelY_2 || GeneralUtils.isCloseEnough(modelY_1, modelY_2, 0.00001)){
            	/*if(GeneralUtils.isCloseEnough(modelY_1, modelY_2, 0.1)){
            		if(breakPoint<0){
                		breakPoint = i;
                	}
            	}*/
            	 value.setEntry(i, modelY_1); 
            	 // derivative of Y1 with respect to m1
                 jacobian.setEntry(i, 0, X[i]);
                 // derivative with respect c1
                 jacobian.setEntry(i, 1, 1);
                 // derivative with respect m2
                 jacobian.setEntry(i, 2, 0);
                 // derivative with respect c2
                 jacobian.setEntry(i, 3, 0);
            }else{
            	//if(GeneralUtils.isCloseEnough(modelY_1, modelY_2, 0.1)){
            		if(breakPoint<0){
                		breakPoint = i;
                	}
            	//}
            	value.setEntry(i, modelY_2); 
            	// derivative of Y2 with respect to m1
                jacobian.setEntry(i, 0, 0);
                // derivative with respect c1
                jacobian.setEntry(i, 1, 0);
                // derivative with respect m2
                jacobian.setEntry(i, 2, X[i]);
                // derivative with respect c2
                jacobian.setEntry(i, 3, 1);
            }
           
           
        }
        return new Pair<RealVector, RealMatrix>(value, jacobian);
    }    
	
}
}


