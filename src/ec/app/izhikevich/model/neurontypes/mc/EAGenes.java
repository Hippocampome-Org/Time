package ec.app.izhikevich.model.neurontypes.mc;

import java.util.HashMap;
import java.util.Map;

import ec.app.izhikevich.evaluator.ModelEvaluatorMC;
import ec.app.izhikevich.inputprocess.labels.ModelParameterID;
import ec.app.izhikevich.starter.ECJStarterV2;
import ec.app.izhikevich.util.GeneralUtils;

public final class EAGenes {
	
	private static Map<ModelParameterID, Integer[]> parmToGeneIdxMap;
	public static int geneLength;
	public static int nComps;
	public static int nCurrents;
	
	private double[] genes;
	public static boolean iso;
	private static boolean IS_DEND_GENES_RELATIVE = false;
	public static void setupEAGene(int n_Comps, int n_Other){		
		nComps = n_Comps;
		if(ECJStarterV2.iso_comp) {
			IS_DEND_GENES_RELATIVE = true;
			ModelEvaluatorMC.EVAL_MC_FOR_MC=false;
		}
		
		nCurrents = n_Other;
		buildMap();
	}
	
	
	private static void buildMap(){
		parmToGeneIdxMap = new HashMap<ModelParameterID, Integer[]>();			
		int offset = 0;
		ModelParameterID[] parameters = ModelParameterID.values();
		for(ModelParameterID parm: parameters) {
			if(parm.equals(ModelParameterID.VR)) {
				parmToGeneIdxMap.put(parm, getIndices(offset, 1));	//VR should be same for all compartments
				offset += 1;
			}else
			if(parm.equals(ModelParameterID.G)) {
				parmToGeneIdxMap.put(parm, getIndices(offset, nComps-1));
				offset += (nComps-1);
			}else
			if(parm.equals(ModelParameterID.P)) {
				parmToGeneIdxMap.put(parm, getIndices(offset, nComps-1));
				offset += (nComps-1);
			}else
			if(parm.equals(ModelParameterID.W)) {
				parmToGeneIdxMap.put(parm, getIndices(offset, nComps-1));
				offset += (nComps-1);
			}else
			if(parm==ModelParameterID.I) {
				parmToGeneIdxMap.put(parm, getIndices(offset, nCurrents));
				offset += nCurrents;
			}else
				if(parm==ModelParameterID.I_dur) 
				{
					parmToGeneIdxMap.put(parm, getIndices(offset, nCurrents));
					offset += nCurrents;
				}else{
				parmToGeneIdxMap.put(parm, getIndices(offset, nComps));
				offset += nComps;
			}
		}
		geneLength = offset;
	}
	
	public EAGenes(double[] genes, boolean iso) {
		this.genes = genes;
		this.iso = iso;
	}
	
	public void setIso(boolean iso) {
		this.iso = iso;
	}
	private static Integer[] getIndices(int startIdx, int length) {
		if(length<1) return null;
		Integer[] indices = new Integer[length];
		for(int i=0;i<length;i++) {
			indices[i] = startIdx;
			startIdx++;
		}
		return indices;
	}
	
	private double[] getParmValues(ModelParameterID parm) {
		Integer[] idces = parmToGeneIdxMap.get(parm);
		if(idces==null) return null;
		double[] parmVals = new double[idces.length];
		for(int i=0;i<idces.length;i++) {
			parmVals[i] = this.genes[idces[i]];
		}
		return parmVals;
	}
	
	public double[] getK(){
		double[] genes= getParmValues(ModelParameterID.K);	
		if(IS_DEND_GENES_RELATIVE)
			for(int i=1;i<nComps;i++)
				if(!iso)
					genes[i] = genes[0]+genes[i];
				else
					genes[i] = genes[0];
		return genes;
	}
	
	public double[] getA(){
		double[] genes= getParmValues(ModelParameterID.A);	
		if(IS_DEND_GENES_RELATIVE)
			for(int i=1;i<nComps;i++)
				if(!iso)
					genes[i] = genes[0]+genes[i];
				else
					genes[i] = genes[0];		
		return genes;	
	}
	
	public double[] getB(){
		double[] genes= getParmValues(ModelParameterID.B);	
		if(IS_DEND_GENES_RELATIVE)
		for(int i=1;i<nComps;i++)
			if(!iso)
				genes[i] = genes[0]+genes[i];
			else
				genes[i] = genes[0];	
		return genes;
	}
	public double[] getD(){
		double[] genes= getParmValues(ModelParameterID.D);	
		if(IS_DEND_GENES_RELATIVE)
		for(int i=1;i<nComps;i++)
			if(!iso)
				genes[i] = genes[0]+genes[i];
			else
				genes[i] = genes[0];		
		return genes;
	}
	
	public double[] getCM(){
		double[] genes= getParmValues(ModelParameterID.CM);	
		if(IS_DEND_GENES_RELATIVE)
		for(int i=1;i<nComps;i++)
			if(!iso)
				genes[i] = genes[0]+genes[i];
			else
				genes[i] = genes[0];		
		return genes;	
	}
	public double getVR(){
		return getParmValues(ModelParameterID.VR)[0];	
	}
	public double[] getVMIN(){
		double vr = getVR();		
		double[] genes= getParmValues(ModelParameterID.VMIN);	
		
		if(IS_DEND_GENES_RELATIVE)
		for(int i=1;i<nComps;i++)
			if(!iso)
				genes[i] = genes[0]+genes[i];
			else
				genes[i] = genes[0];			
		
		for(int i=0;i<nComps;i++)
			genes[i] = vr+genes[i];	
		
		return 	genes;
	}
	public double[] getVT(){		
		double vr = getVR();
		double[] genes= getParmValues(ModelParameterID.VT);	
		
		if(IS_DEND_GENES_RELATIVE)
		for(int i=1;i<nComps;i++)
			if(!iso)
				genes[i] = genes[0]+genes[i];
			else
				genes[i] = genes[0];
		
		for(int i=0;i<nComps;i++)
			genes[i] = genes[i]+vr;
		return 	genes;	
	}
	public double[] getVPEAK(){
		double vr = getVR();
		double[] vpeak = getParmValues(ModelParameterID.VPEAK); //this is offset from vr for soma; offset from somavpeak for dendrite
		//float somVpeak = vpeak[0];
		vpeak[0] = vpeak[0] + vr;		
		for(int i=1;i<nComps;i++)
			if(!iso)
				vpeak[i] = vpeak[0]-vpeak[i];
			else
				vpeak[i] = vpeak[0];
		return 	vpeak;	
	}
	
	
	public double[] getG(){
		double[] genes= getParmValues(ModelParameterID.G);	
		
		if(IS_DEND_GENES_RELATIVE)
		for(int i=1;i<nComps-1;i++)
			genes[i] = genes[0]+genes[i];	
		
		return genes;		
	}
	
	public double[] getP(){
		return getParmValues(ModelParameterID.P);	
	}
	
	public double[] getW(){
		return getParmValues(ModelParameterID.W);	
	}
	
	public double[] getI(){
		return getParmValues(ModelParameterID.I);	
	}
	
	public double[] getIDurs(){
		return getParmValues(ModelParameterID.I_dur);	
	}
	
	public static Integer[] getIndices(ModelParameterID parameter) {
		return parmToGeneIdxMap.get(parameter);
	}
	
	public static void displayMap(){
		ModelParameterID[] parameters = ModelParameterID.values();
		for(ModelParameterID parm: parameters) {
			Integer[] indices = getIndices(parm);
			if(indices==null) continue;
			System.out.print(parm.name()+"\t");
			for(int i=0;i<indices.length;i++) {
				System.out.print(indices[i]+", ");
			}
			System.out.println();
		}	
	}
	public void display(){
		for(int i=0;i<genes.length;i++){
			System.out.print(genes[i]+",");
		}
	}
	
	public static void main(String[] args) {
		displayMap();
	}
}
