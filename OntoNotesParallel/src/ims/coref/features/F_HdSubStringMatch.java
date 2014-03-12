package ims.coref.features;

import ims.coref.Parallel;
import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.features.enums.Bool;
import ims.coref.features.extractors.SpanToken;
import ims.coref.features.extractors.SpanTokenExtractor;
import ims.coref.features.extractors.TargetSpan;
import ims.coref.features.extractors.TargetSpanExtractor;
import ims.coref.features.extractors.TokenTrait;
import ims.coref.features.extractors.TokenTraitExtractor;

public class F_HdSubStringMatch extends AbstractEnumFeature<Bool> {
	private static final long serialVersionUID = 1L;

	private final TargetSpanExtractor subSpanTse;
	private final SpanTokenExtractor subSte;
	private final TokenTraitExtractor tte;
	
	protected F_HdSubStringMatch(TargetSpan subSpanTs,SpanToken subSt,TokenTrait tt) {
		super(subSpanTs.toString()+subSt.toString()+tt.toString()+"SubStringMatch", Bool.values());
		this.subSpanTse=new TargetSpanExtractor(subSpanTs);
		this.subSte=new SpanTokenExtractor(subSt);
		this.tte=new TokenTraitExtractor(tt);
	}

	@Override
	Bool getVal(PairInstance pi, Document d) {
		if(Parallel.zero ) {
			return Bool.False;
		}
		Span subSpan=subSpanTse.getSpan(pi);
		Span otherSpan=subSpanTse.getOtherSpan(pi);
		int i=subSte.getToken(subSpan);
		if(i<1)
			return null;
		String s=tte.getTrait(subSpan.s, i);
		for(int q=otherSpan.start;q<otherSpan.end;++q){
			if(tte.getTrait(otherSpan.s, q).equals(s))
				return Bool.True;
		}
		return Bool.False;
	}

}
