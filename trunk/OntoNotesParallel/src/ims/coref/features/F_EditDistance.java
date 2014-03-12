package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.features.enums.Buckets;
import ims.util.EditDistance;

public class F_EditDistance extends AbstractEnumFeature<Buckets>{
	private static final long serialVersionUID = 1L;

	private final AbstractSingleDataDrivenFeature asf1;
	private final AbstractSingleDataDrivenFeature asf2;
	
	protected F_EditDistance(AbstractSingleDataDrivenFeature asf1,AbstractSingleDataDrivenFeature asf2) {
		super(asf1.getName()+asf2.getName()+"EditDistance", Buckets.values());
		this.asf1=asf1;
		this.asf2=asf2;
	}

	@Override
	Buckets getVal(PairInstance pi, Document d) {
		String s1=asf1.getStringValue(pi, d);
		String s2=asf2.getStringValue(pi, d);
		int dist=EditDistance.levenshteinDistance(s1, s2);
		return types[0].getBucket(dist);
	}

}
