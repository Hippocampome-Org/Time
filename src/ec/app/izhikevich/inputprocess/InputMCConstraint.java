package ec.app.izhikevich.inputprocess;

import java.util.HashMap;

import ec.app.izhikevich.inputprocess.labels.MCConstraintAttributeID;
import ec.app.izhikevich.inputprocess.labels.MCConstraintType;

public class InputMCConstraint {
	
	private MCConstraintType type;	
	private HashMap<MCConstraintAttributeID, Object> attributes;
	private static final MCConstraintAttributeID[] RANGED_ATTRs = {MCConstraintAttributeID.ampa_epsp};
	
	public InputMCConstraint(MCConstraintType type) {
		this.type = type;		
		attributes = new HashMap<>();
	}
	public MCConstraintType getType() {
		return type;
	}
	public void addAttribute(MCConstraintAttributeID attributeID, double val){
		this.attributes.put(attributeID, val);		
	}
	public double getAttribute(MCConstraintAttributeID attributeID){
		return (double)this.attributes.get(attributeID);
	}
	
	public void addAttributeWrange(MCConstraintAttributeID attributeID, double[] val){
		this.attributes.put(attributeID, val);		
	}
	public double[] getAttributeWrange(MCConstraintAttributeID attributeID){
		return (double[])this.attributes.get(attributeID);
	}
	
	public boolean doesRangedAttrListContain(MCConstraintAttributeID attributeID){
		for(int i=0;i<RANGED_ATTRs.length;i++){
			if(attributeID.equals(RANGED_ATTRs[i]))
				return true;
		}
		return false;
	}
		
}
