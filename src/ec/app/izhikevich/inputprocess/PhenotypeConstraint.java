package ec.app.izhikevich.inputprocess;

import java.util.HashMap;

import ec.app.izhikevich.inputprocess.labels.PhenotypeConstraintAttributeID;
import ec.app.izhikevich.inputprocess.labels.PhenotypeConstraintType;

public class PhenotypeConstraint {
	
	private PhenotypeConstraintType type;	
	private HashMap<PhenotypeConstraintAttributeID, Object> attributes;
	
	public PhenotypeConstraint(PhenotypeConstraintType type) {
		this.type = type;		
		attributes = new HashMap<>();
	}
	public PhenotypeConstraintType getType() {
		return type;
	}
	public void addAttribute(PhenotypeConstraintAttributeID attributeID, double val){
		this.attributes.put(attributeID, val);		
	}
	public double getAttribute(PhenotypeConstraintAttributeID attributeID){
		return (double)this.attributes.get(attributeID);
	}
	
	public void addAttributeWrange(PhenotypeConstraintAttributeID attributeID, double[] val){
		this.attributes.put(attributeID, val);		
	}
	
		
}
