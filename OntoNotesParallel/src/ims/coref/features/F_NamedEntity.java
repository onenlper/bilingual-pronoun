package ims.coref.features;

import ims.coref.Parallel;
import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.features.extractors.TargetSpan;
import ims.coref.features.extractors.TargetSpanExtractor;

public class F_NamedEntity extends AbstractSingleDataDrivenFeature {
	private static final long serialVersionUID = 1L;
	
	private final TargetSpanExtractor tse;

	protected F_NamedEntity(TargetSpanExtractor tse, int cutOff) {
		super(tse.ts+"NamedEntity", cutOff);
		this.tse=tse;
	}

	@Override
	public String extractStringValue(PairInstance instance, Document d) {
		if(Parallel.zero && tse.ts==TargetSpan.Anaphor) {
			return "XXX";
		}
		Span s=tse.getSpan(instance);
		if(s.ne==null)
			return "<null>";
		else
			return s.ne.getLabel();
	}

}
