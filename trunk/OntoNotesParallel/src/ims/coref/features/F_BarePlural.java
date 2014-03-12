package ims.coref.features;

import ims.coref.Parallel;
import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.features.enums.Bool;
import ims.coref.features.extractors.TargetSpan;
import ims.coref.features.extractors.TargetSpanExtractor;

public class F_BarePlural extends AbstractEnumFeature<Bool> {
	private static final long serialVersionUID = 1L;
	
	private final TargetSpanExtractor tse;
	
	protected F_BarePlural(TargetSpanExtractor tse) {
		super(tse.ts.toString()+"BarePlural", Bool.values());
		this.tse=tse;
	}

	@Override
	Bool getVal(PairInstance pi, Document d) {
		if(Parallel.zero && tse.ts==TargetSpan.Anaphor) {
			return Bool.False;
		}
		Span s=tse.getSpan(pi);
		if(s.start==s.end && s.s.tags[s.start].equals("NNS"))
			return Bool.True;
		else
			return Bool.False;
	}
}
