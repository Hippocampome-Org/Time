package ec.app.izhikevich.spike.labels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import ec.app.izhikevich.evaluator.qualifier.ClassificationParameterID;

public class SpikePatternClass {
	public static final Set<SpikePatternComponent>TRANSIENTS = new HashSet<>(); 
	public static final Set<SpikePatternComponent> SPIKING = new HashSet<>();
	public static final Set<SpikePatternComponent> BURSTING = new HashSet<>();
	public static final Set<SpikePatternComponent> STUTTERING = new HashSet<>();
	//private static final Map<SpikePatternComponent, SpikePatternComponent> SStoTRANSmap = new HashMap<>();
	static{
		TRANSIENTS.add(SpikePatternComponent.D);
		TRANSIENTS.add(SpikePatternComponent.ASP);
		TRANSIENTS.add(SpikePatternComponent.RASP);
		TRANSIENTS.add(SpikePatternComponent.TSTUT);
		TRANSIENTS.add(SpikePatternComponent.TSWB);
		TRANSIENTS.add(SpikePatternComponent.X);
		
		SPIKING.add(SpikePatternComponent.ASP);
		SPIKING.add(SpikePatternComponent.NASP);
		SPIKING.add(SpikePatternComponent.RASP);
		
		STUTTERING.add(SpikePatternComponent.TSTUT);
		BURSTING.add(SpikePatternComponent.TSWB);
		STUTTERING.add(SpikePatternComponent.PSTUT);
		BURSTING.add(SpikePatternComponent.PSWB);
		/*
		 SStoTRANSmap.put(SpikePatternComponent.NASP, SpikePatternComponent.TSTUT);
		//SStoTRANSmap.put(SpikePatternComponent.SLN, SpikePatternComponent.SLN);
		SStoTRANSmap.put(SpikePatternComponent.PSTUT, SpikePatternComponent.TSTUT);
		SStoTRANSmap.put(SpikePatternComponent.PSWB, SpikePatternComponent.TSWB);
		*/
	}
	
	private SpikePatternComponent[] components;	
	private static final int MAX_COMP = 5; //3 is the current max experimental length , pswb addition
	private int length;
	
	/*public float m1_2p=-77;
	public float m1_3p=-77;
	public float m1_4p=-77; 
	public float m2_4p=-77;
*/
	//public float[] m = new float[4];
	public Map<ClassificationParameterID, Double> classificationParameters;
	
	public SpikePatternClass(){
		components = new SpikePatternComponent[MAX_COMP];
		length = 0;
		classificationParameters =  new HashMap<>();
		initClassificationParameters();
		/*for(int i=0;i<4;i++){
			m[i]=-77;
		}*/
	}	
	/*
	 * init with Double NaN: for display purposes
	 */
	private void initClassificationParameters(){
		ClassificationParameterID[] parmIDs = ClassificationParameterID.values();
		for(ClassificationParameterID parmID: parmIDs){
			classificationParameters.put(parmID, Double.NaN);
		}		
	}
	public void addClassificationParameter(ClassificationParameterID parmID, Double value){
		classificationParameters.put(parmID, value);
	}
	public SpikePatternClass(String patternClass, String compDelimitor){
		this();
		StringTokenizer st = new StringTokenizer(patternClass, compDelimitor);
		while(st.hasMoreTokens()){
			components[length++] = SpikePatternComponent.valueOf(st.nextToken());
		}
	}
	public void addComponent(SpikePatternComponent component){
		components[length++]=component;
	}
	public void removeLastAddedComponent(){
		components[length--]=null;		
	}
	public SpikePatternComponent[] getSpikePatternLabel(){
		return this.components;
	}
	public int getLength() {
		return length;
	}
	public boolean contains(SpikePatternComponent component){		
		for(int i=0;i<length;i++){
			if(components[i]==null){
				System.out.println(length+", "+i);
				System.out.println(components[0]+"."+components[1]+"."+components[2]);
			}
			if(components[i].equals(component))
				return true;
		}
		return false;
	}
	public boolean steadyStateReached(){
		if(TRANSIENTS.contains(this.components[length-1])){
			return false;
		}
		return true;
	}
	public boolean containsSWB(){
		for(int i=0;i<length;i++){
			if(BURSTING.contains(this.components[i])){
				return true;
			}			
		}
		return false;		
	}
	public boolean containsSTUT(){
		for(int i=0;i<length;i++){
			if(STUTTERING.contains(this.components[i])){
				return true;
			}			
		}
		return false;		
	}
	public boolean containsSP(){
		for(int i=0;i<length;i++){
			if(SPIKING.contains(this.components[i])){
				return true;
			}			
		}
		return false;		
	}
	public int getnPieceWiseParms() {
		int nPieceWiseParms=0;
		for(int i=0;i<length;i++){
			if(components[i].equals(SpikePatternComponent.ASP)){
				nPieceWiseParms += 2;
				continue;
			}
			if(components[i].equals(SpikePatternComponent.NASP)){
				nPieceWiseParms += 1;
				continue;
			}
			if(components[i].equals(SpikePatternComponent.X)){
				nPieceWiseParms += 1;
			}
		}
	return nPieceWiseParms;
	}
	// ignore X for excel sheet!
	public void display(){
		for(int i=0;i<length;i++){
			if(!components[i].equals(SpikePatternComponent.X)){
				if(!components[i].equals(SpikePatternComponent.EMPTY)){
					System.out.print(components[i]);
					if(TRANSIENTS.contains(components[i])){
						System.out.print(".");
					}
				}else{
					System.out.print("*No_data");
				}
			}			
		}			
	}
	// ignore X for excel sheet!
	public void display(ClassificationParameterID[] parmIDs){
		for(int i=0;i<length;i++){
			if(!components[i].equals(SpikePatternComponent.X)){
				if(!components[i].equals(SpikePatternComponent.EMPTY)){
					System.out.print(components[i]);
					if(TRANSIENTS.contains(components[i])){
						System.out.print(".");
					}
				}else{
					System.out.print("*No_data");
				}
			}			
		}
		System.out.print("\t");
		
		for(ClassificationParameterID parmID:parmIDs){
			if(classificationParameters.get(parmID).isNaN()){
				System.out.print("no data\t");
			}else{
				System.out.print(classificationParameters.get(parmID)+"\t");
			}
		}
		
	}
	/*
	public void displayAllSlopesWeird(){
		for(int i=0;i<length;i++){
			if(!components[i].equals(SpikePatternComponent.X)){
				if(!components[i].equals(SpikePatternComponent.EMPTY)){
					System.out.print(components[i]);
					if(TRANSIENTS.contains(components[i])){
						System.out.print(".");
					}
				}else{
					System.out.print("*No_data");
				}
			}			
		}
		System.out.print("\t");
		boolean found = false;
		for(int i=0;i<length;i++){			
			if(components[i].equals(SpikePatternComponent.FASP) || components[i].equals(SpikePatternComponent.WASP) ){
				System.out.print(components[i].properties.get(PatternFeatureID.sfa_linear_m1)+"\t"
								+components[i].properties.get(PatternFeatureID.n_sfa_isis1)+"\t");	
				found = true;
			}
		}
		if(!found){
			System.out.print("\t\t");
		}
		
		for(int i=0;i<4;i++){
			if(m[i]!=-77)
				System.out.print(m[i]+"\t");
			else
				System.out.print("no data\t");
		}
	}
	*/
	
	
	public boolean equals(SpikePatternClass targetClass){
		if(targetClass.length != this.length)
			return false;
		
		for(int i=0;i<length;i++){
			if(!components[i].equals(targetClass.components[i]))
				return false;
		}
		return true;		
	}
	
	public void replaceWithPSTUT(){
		if(this.contains(SpikePatternComponent.D)){
			length = 0;
			addComponent(SpikePatternComponent.D);
			addComponent(SpikePatternComponent.PSTUT);
		}else{
			length = 0;			
			addComponent(SpikePatternComponent.PSTUT);
		}
	}
	public void replaceWithPSWB(){
		if(this.contains(SpikePatternComponent.D)){
			length = 0;
			addComponent(SpikePatternComponent.D);
			addComponent(SpikePatternComponent.PSWB);
		}else{
			length = 0;			
			addComponent(SpikePatternComponent.PSWB);
		}
	}
	/*public void replaceSSwithTransient(){		
		SpikePatternComponent ss = components[length-1];
		SpikePatternComponent repWith = SStoTRANSmap.get(ss);		
		if(this.contains(SpikePatternComponent.D)){
			length = 0;
			addComponent(SpikePatternComponent.D);
			addComponent(repWith);
		}else{
			length = 0;			
			addComponent(repWith);
		}
						
	}*/
	public String toString(){
		String str = "";
		for(int i=0;i<length;i++){
			str+= components[i]+".";
		}
		return str;
	}
}
