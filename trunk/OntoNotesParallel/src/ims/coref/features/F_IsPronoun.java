package ims.coref.features;

import ims.coref.Parallel;
import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.features.enums.Bool;
import ims.coref.features.enums.Gender;
import ims.coref.features.extractors.TargetSpan;
import ims.coref.features.extractors.TargetSpanExtractor;

public class F_IsPronoun extends AbstractEnumFeature<Bool>{
	private static final long serialVersionUID = 1L;

	private final TargetSpanExtractor tse;
	
	protected F_IsPronoun(TargetSpanExtractor tse) {
		super(tse.ts.toString()+"Pronoun", Bool.values());
		this.tse=tse;
	}

	@Override
	Bool getVal(PairInstance pi,Document d) {
		if(Parallel.zero && tse.ts==TargetSpan.Anaphor) {
			return Bool.True;
		}
		Span s=tse.getSpan(pi);
		if(s.isPronoun)
			return Bool.True;
		else
			return Bool.False;
	}

}
