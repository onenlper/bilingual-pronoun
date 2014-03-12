package ims.coref.features;

import util.Common;
import ims.coref.Parallel;
import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.features.extractors.SpanTokenExtractor;
import ims.coref.features.extractors.TargetSpan;
import ims.coref.features.extractors.TargetSpanExtractor;
import ims.coref.features.extractors.TokenTraitExtractor;

public class F_GenericSpanTokenTraitFeature extends AbstractSingleDataDrivenFeature{
	private static final long serialVersionUID = 1L;

	private final TargetSpanExtractor tse;
	private final SpanTokenExtractor ste;
	private final TokenTraitExtractor tte;
	
	protected F_GenericSpanTokenTraitFeature(TargetSpanExtractor tse,SpanTokenExtractor ste,TokenTraitExtractor tte, int cutOff) {
		super(tse.ts.toString()+ste.st.toString()+tte.tt.toString(), cutOff);
		this.tse=tse;
		this.ste=ste;
		this.tte=tte;
	}

	@Override
	public String extractStringValue(PairInstance instance,Document d) {
		if(Parallel.zero && this.tse.ts==TargetSpan.Anaphor) {
			return "XXX";
		}
		Span s=tse.getSpan(instance);
		int to=ste.getToken(s);
		String tr=tte.getTrait(s.s, to);
		
		return tr;
	}

}
