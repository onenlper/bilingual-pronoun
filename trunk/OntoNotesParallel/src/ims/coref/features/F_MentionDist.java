package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.features.enums.IBuckets;

public class F_MentionDist<T extends Enum<T> & IBuckets<T>> extends AbstractEnumFeature<T> {
	private static final long serialVersionUID = 1L;
	
	protected F_MentionDist(T[] types) {
		super("MentionDist"+types[0].getClass().getSimpleName(), types);
	}

	@Override
	T getVal(PairInstance pi, Document d) {
		int dist=pi.mentionDist;
		return types[0].getBucket(dist);
	}

}
