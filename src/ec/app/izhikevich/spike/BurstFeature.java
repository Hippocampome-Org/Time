package ec.app.izhikevich.spike;

import java.util.ArrayList;
import java.util.HashMap;

import ec.app.izhikevich.inputprocess.labels.BurstFeatureID;


public class BurstFeature {
	
	
	private double totWeight;
	private ArrayList<HashMap<BurstFeatureID, Double>> value;
	private HashMap<BurstFeatureID, Double> featWeight;
	
	public BurstFeature(ArrayList<HashMap<BurstFeatureID, Double>> value, 
			HashMap<BurstFeatureID, Double> featWeight,
			double totWeight) {	
		this.value = value;
		this.featWeight = featWeight;
		this.totWeight = totWeight;
	}
	
	public double getTotalWeight() {
		return totWeight;
	}
	public void setTotalWeight(double weight) {
		this.totWeight = weight;
	}
	public ArrayList<HashMap<BurstFeatureID, Double>> getValue() {
		return value;
	}
	public void setValue(ArrayList<HashMap<BurstFeatureID, Double>> value) {
		this.value = value;
	}
	
	public double[] getAllBurstFeature(BurstFeatureID featureID){
		double[] feature = new double[value.size()];
		for(int i=0;i<feature.length;i++){
			feature[i]=value.get(i).get(featureID);
		}
		return feature;
	}
	public int[] getAllNSpikes(){
		int[] feature = new int[value.size()];
		for(int i=0;i<feature.length;i++){
			feature[i]=(int)(double)value.get(i).get(BurstFeatureID.nspikes);
		}
		return feature;
	}
	public double getFeatureWeight(BurstFeatureID feature){
		return this.featWeight.get(feature);
	}
}
