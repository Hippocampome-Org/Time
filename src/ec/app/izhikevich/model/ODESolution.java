package ec.app.izhikevich.model;

import java.util.ArrayList;

import ec.app.izhikevich.util.GeneralUtils;

public class ODESolution {
	private static final double dvdtSpikeThresh = 6d;
	
		private double[] time;
		private double[] voltage;
		private double[] voltageDerivs;
		private double[] spikeTimes;
		private double[] recoveryUW;
		
		public ODESolution(double[] time, double[] voltage, ArrayList<Double> spikeTimes) {
			this.setTime(time);
			this.setVoltage(voltage);		
			this.setSpikeTimes(GeneralUtils.listToArrayDouble(spikeTimes));
			this.setRecoveryU(null);
		}
		public ODESolution(double[] time, double[] voltage, double[] recovery_U, ArrayList<Double> spikeTimes) {
			this.setTime(time);
			this.setVoltage(voltage);		
			this.setSpikeTimes(GeneralUtils.listToArrayDouble(spikeTimes));
			this.setRecoveryU(recovery_U);
		}
		/*public ODESolution(double[] time, double[] voltage, double[] recovery_U, double[] vDerivs, ArrayList<Double> spikeTimes) {
			this.setTime(time);
			this.setVoltage(voltage);		
			this.setVoltageDerivs(vDerivs);
			this.setSpikeTimes(GeneralUtils.listToArrayDouble(spikeTimes));
			
			GeneralUtils.displayArray(this.spikeTimes); System.out.println("length "+this.spikeTimes.length);
			double[] newspiketimes = calculateSpikeTimes();
			GeneralUtils.displayArray(newspiketimes); System.out.println("length "+newspiketimes.length);
			
			this.setRecoveryU(recovery_U);
		}*/
		public ODESolution(double[] time, double[] voltage, double[] spikeTimes) {
			this.setTime(time);
			this.setVoltage(voltage);
			this.setSpikeTimes(spikeTimes);
			this.setRecoveryU(null);
		}
		
		public ODESolution(double[] time, double[] voltage, double[] vDerivs, boolean extractSpikeTimes) {
			
			this.setTime(time);
			this.setVoltage(voltage);
			this.setVoltageDerivs(vDerivs);
			if(!extractSpikeTimes)
				this.setSpikeTimes(calculateSpikeTimes());
			else {
				this.setSpikeTimes(calculateSpikeTimes());
			}
			this.setRecoveryU(null);
		}
		public ODESolution(double[] time, double[] voltage, double[] recoveryW, double[] vDerivs, boolean extractSpikeTimes) {
			
			this.setTime(time);
			this.setVoltage(voltage);
			this.setVoltageDerivs(vDerivs);
			if(!extractSpikeTimes)
				this.setSpikeTimes(calculateSpikeTimes());
			else {
				this.setSpikeTimes(calculateSpikeTimes());
			}
			this.setRecoveryU(recoveryW);
		}
		
		public double[] calculateSpikeTimes() {
			ArrayList<Double> st = new ArrayList<>();
			//GeneralUtils.displayArrayVertical(voltageDerivs);
			boolean thresh_crossed = false;
			for(int i=0;i<voltageDerivs.length;i++) {
				if(voltageDerivs[i] > dvdtSpikeThresh) {
					thresh_crossed = true;
				}
				
				if(thresh_crossed) {
					if(voltageDerivs[i] - dvdtSpikeThresh < 0) {
						st.add(time[i]);
						thresh_crossed = false;
					}
				}
			}
			
			return GeneralUtils.listToArrayDouble(st);
		}
		
		public double[] getTime() {
			return time;
		}
		public void setTime(double[] time) {
			this.time = time;
		}
		public double[] getSpikeTimes() {
			return spikeTimes;
		}
		public void setSpikeTimes(double[] spikeTimes) {
			this.spikeTimes = spikeTimes;
		}
		public double[] getVoltage() {
			return voltage;
		}
		public void setVoltage(double[] voltage) {
			this.voltage = voltage;
		}
		public double[] getVoltageDerivs() {
			return voltageDerivs;
		}
		public void setVoltageDerivs(double[] voltageDerivs) {
			this.voltageDerivs = voltageDerivs;
		}
		public void display(){
			System.out.print("time:\t");GeneralUtils.displayArray(time);
			System.out.print("voltage:\t"); GeneralUtils.displayArray(voltage);
			System.out.print("spikeTimes:\t"); GeneralUtils.displayArray(spikeTimes);
		}

		public double[] getRecoveryU() {
			return recoveryUW;
		}

		public void setRecoveryU(double[] recoveryU) {
			this.recoveryUW = recoveryU;
		}
	
}
