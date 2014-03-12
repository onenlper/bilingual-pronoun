package ims.ml.liblinear;

public class ClassLabel {

	public final int label;
	public final double prob;
	
	public ClassLabel(int label,double prob){
		this.label=label;
		this.prob=prob;
	}
}
