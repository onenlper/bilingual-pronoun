package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;

import java.util.List;
import java.util.Map;

import de.bwaldvogel.liblinear.FeatureNode;

public class F_Distance extends AbstractSingleFeature {
	private static final long serialVersionUID = 1L;

	protected F_Distance() {
		super("Distance");
	}

	@Override
	public void register(PairInstance instance,Document d) {
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public void contributeFeatureNodes(PairInstance instance, int offset, List<FeatureNode> sink,Document d) {
		int dist=distance(instance);
		FeatureNode fn=new FeatureNode(offset+1,dist);
		sink.add(fn);
	}

	private int distance(PairInstance instance) {
		return Math.abs(instance.ana.s.sentenceIndex-instance.ant.s.sentenceIndex);
	}

	@Override
	public void freeze() {
	}

	@Override
	public String getStringValue(PairInstance instance,Document d) {
		return Integer.toString(distance(instance));
	}

	public Map<String, Integer> getMap(){
		throw new Error("not implemented");
	}
}
