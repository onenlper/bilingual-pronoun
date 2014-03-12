package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.features.enums.Buckets;

public class F_DistanceBucketed extends AbstractEnumFeature<Buckets>{
	private static final long serialVersionUID = 1L;

	protected F_DistanceBucketed() {
		super("DistanceBucketed",Buckets.values());
	}

	@Override
	Buckets getVal(PairInstance pi,Document d) {
		int dist=distance(pi);
		return types[0].getBucket(dist);
	}

	private int distance(PairInstance instance) {
		return Math.abs(instance.ana.s.sentenceIndex-instance.ant.s.sentenceIndex);
	}
}
