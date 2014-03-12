package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.features.enums.ProbabilityBuckets;
import ims.coref.features.extractors.TargetSpanExtractor;

public class F_AnaphoricityBucketed extends AbstractEnumFeature<ProbabilityBuckets> {
	private static final long serialVersionUID = 4562053135929020704L;

	private final TargetSpanExtractor tse;
	
	protected F_AnaphoricityBucketed(TargetSpanExtractor tse) {
		super(tse.ts.toString()+"Anaphoricity", ProbabilityBuckets.values());
		this.tse=tse;
		System.out.println("Initializing "+this.name+" feature. Note that this requires the NonReferential markable extractor!");
	}

	@Override
	ProbabilityBuckets getVal(PairInstance pi, Document d) {
		Span s=tse.getSpan(pi);
		if(s.anaphoricityPr<0)
			return null;
		else
			return ProbabilityBuckets.getBucket(s.anaphoricityPr);
	}

}
