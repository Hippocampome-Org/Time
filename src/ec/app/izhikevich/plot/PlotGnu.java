package ec.app.izhikevich.plot;

public class PlotGnu {
	private static final float[][] dataSetColorSchemes = new float[][] {{0f,0f,0f},
																		{1f,0f,0f},
																		{0f,0f,1f},
																		{0f,1f,0f},
																		{1f,1f,0f},
																		{0f,1f,1f}};
	
	
	
	public PlotGnu(String plotTitle, String xLabel, String yLabel){
		this(plotTitle, xLabel, yLabel, null,null);
	}
	
	public PlotGnu(String plotTitle, 
			String xLabel, String yLabel, 
			String xRange, String yRange){
		
	}
	
	public PlotGnu(String plotTitle, 
			String xLabel, String yLabel, String zLabel, 
			String xRange, String yRange, String zRange){
		
	}
	
	public void addDataSet(double[][] datasetPoints, String datasetTitle){
		
	}
	
	public void plotDataSetPoints(){
		
	}
	
	
	public void savePlot(String fileName){
		
	}
	
	public static void main(String[] args) {
		 
		 double[][] dataset = new double[][]{{1,1},
			 			{2.7,2},{3,3},{4,4}};
		 double[][] dataset2 = new double[][]{{10,10},
		 			{27,20},{33,31},{41,41}}; 
		
		 double[][] dataset3 = new double[][]{{4,10},
		 			{27,40},{33,21},{4,41}}; 
		
		
		 double[][] dataset3d_1 = new double[][]{{1,1,1},
		 			{2.7,2, 4},{3,3,6},{4,4, 8}};
		 double[][] dataset3d_2 = new double[][]{{1,1,10},
		 			{2.7,20, 4},{3,30,6},{4,40, 8}};
		 double[][] dataset3d_3 = new double[][]{{1,1,17},
		 			{27,20, 4},{3,31,6},{4,41, 8}};
		 double[][] dataset3d_4 = new double[][]{{1,1,19},
		 			{2.7,2, 44},{38,3,6},{4,46, 8}};
		 
	    PlotGnu plotter = new PlotGnu("Test", "'test x'", "'test y'", "[-50:50]", "[-50:50]");
	    plotter.addDataSet(dataset, "ds-one");
	    plotter.addDataSet(dataset2, "ds-two");
	    plotter.addDataSet(dataset3, "ds-3");
	  
	    PlotGnu plotter3d = new PlotGnu("Test", 
	    		"'test x'", "'test y'", "'test z'", 
	    		"[-50:50]", "[-50:50]", "[-50:50]");
		  
	    plotter3d.addDataSet(dataset3d_1, "1");
	    plotter3d.addDataSet(dataset3d_2, "2");
	    plotter3d.addDataSet(dataset3d_3, "3");
	    plotter3d.addDataSet(dataset3d_4, "4");
	    
	    
	    plotter3d.plotDataSetPoints();
	        
	}

}
