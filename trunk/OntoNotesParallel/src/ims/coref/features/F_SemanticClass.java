package ims.coref.features;

import ims.coref.Parallel;
import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.features.enums.Gender;
import ims.coref.features.enums.SemanticClass;
import ims.coref.features.extractors.TargetSpan;
import ims.coref.features.extractors.TargetSpanExtractor;

public class F_SemanticClass extends AbstractEnumFeature<SemanticClass>{
	private static final long serialVersionUID = 1L;

	private final TargetSpanExtractor tse;
	
	protected F_SemanticClass(TargetSpanExtractor tse) {
		super(tse.ts.toString()+"SemanticClass", SemanticClass.values());
		this.tse=tse;
	}

	@Override
	SemanticClass getVal(PairInstance pi,Document d) {
		if(Parallel.zero && tse.ts==TargetSpan.Anaphor) {
			return SemanticClass.Unknown;
		}
		Span s=tse.getSpan(pi);
		return s.semanticClass;
	}
}
