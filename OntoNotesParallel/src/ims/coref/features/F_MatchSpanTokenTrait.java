package ims.coref.features;

import ims.coref.Parallel;
import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.features.enums.Bool;
import ims.coref.features.extractors.SpanToken;
import ims.coref.features.extractors.SpanTokenExtractor;
import ims.coref.features.extractors.TargetSpan;
import ims.coref.features.extractors.TokenTrait;
import ims.coref.features.extractors.TokenTraitExtractor;

public class F_MatchSpanTokenTrait extends AbstractEnumFeature<Bool> {
	private static final long serialVersionUID = 1L;
	
	private final SpanTokenExtractor ste;
	private final TokenTraitExtractor tte;

	protected F_MatchSpanTokenTrait(SpanToken st,TokenTrait tt) {
		super("Match"+st.toString()+tt.toString(), Bool.values());
		ste=new SpanTokenExtractor(st);
		tte=new TokenTraitExtractor(tt);
	}

	@Override
	Bool getVal(PairInstance pi, Document d) {
		if(Parallel.zero) {
			return Bool.False;
		}
		int anaI=ste.getToken(pi.ana);
		int antI=ste.getToken(pi.ant);
		if(anaI<1 || antI<1)
			return null;
		if(tte.getTrait(pi.ana.s, anaI).equals(tte.getTrait(pi.ant.s, antI)))
			return Bool.True;
		else
			return Bool.False;
	}

}
