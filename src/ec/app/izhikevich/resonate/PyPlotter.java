package ec.app.izhikevich.resonate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PyPlotter {

	private String dataFileName;
	public PyPlotter(String dataFileName){
		this.dataFileName = dataFileName;
	}
	
	public void invoke(String pythonScript){
		List<String> command = new ArrayList<String>();
		command.add("python");
		command.add("theory/"+pythonScript);
		command.add(dataFileName);		
		
		ProcessBuilder pb = new ProcessBuilder(command);
		String outputString = "";
		try {
			Process p = pb.start();		
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while(true){
				String retVal = in.readLine();				
				if(retVal!=null) {
					outputString+=retVal;	
				}else{
					break;
				}	
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
		System.out.println(outputString);
	}
	
}
