package ims.coref;

public class Eva {

	public double hit;
	public double gold;
	public double sys;

	public void printStat(String message) {
		// TODO
		double rec = hit / gold;
		double pre = hit / sys;
		double f = 2 * rec * pre / (rec + pre);
		System.out.println(message);
		System.out.format("Rec: %f = %f / %f \n", rec, hit, gold);
		System.out.format("Pre: %f = %f / %f \n", pre, hit, sys);
		System.out.format("F-1: %f \n", f);
		System.out.println("======");
	}
}
