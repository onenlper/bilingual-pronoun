package ims.ml.liblinear;

import java.util.List;

import de.bwaldvogel.liblinear.FeatureNode;

public interface InstanceSink {

	
	public void sink(int label,List<FeatureNode> nodes);
	public void close();
	public LibLinearModel train();
	
	public double getAllInstance();
	
	public double getPosInstance();
}
