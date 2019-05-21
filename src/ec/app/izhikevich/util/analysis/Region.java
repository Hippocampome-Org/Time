package ec.app.izhikevich.util.analysis;

public enum Region {
	DG(1), CA3(2), CA2(3), CA1(4), SUB(5), EC(6);
	
	private int regID;
	
	Region(int reg_id){
		regID = reg_id;
	}
	
	public int getRegID(){
		return regID;
	}
	
	public static Region getRegion(int regIdx) {
		if(regIdx==1) return Region.DG;
		if(regIdx==2) return Region.CA3;
		if(regIdx==3) return Region.CA2;
		if(regIdx==4) return Region.CA1;
		if(regIdx==5) return Region.SUB;
		if(regIdx==6) return Region.EC;
		
		System.out.println("Unknown region ID"+ regIdx);
		System.exit(-1);
		return null;
	}
}
