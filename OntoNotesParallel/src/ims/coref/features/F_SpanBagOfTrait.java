package ims.coref.features;

import ims.coref.Parallel;
import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.features.enums.Bool;
import ims.coref.features.extractors.TargetSpan;
import ims.coref.features.extractors.TargetSpanExtractor;
import ims.coref.features.extractors.TokenTrait;
import ims.coref.features.extractors.TokenTraitExtractor;

import java.util.Collection;

public class F_SpanBagOfTrait extends AbstractMultiDataDrivenFeature{
	private static final long serialVersionUID = 1L;

	private final TargetSpanExtractor tse;
	private final TokenTraitExtractor tte;
	
	protected F_SpanBagOfTrait(TargetSpan ts, TokenTrait tt,int cutOff) {
		super(ts.toString()+"BagOf"+tt.toString(), cutOff);
		this.tse=new TargetSpanExtractor(ts);
		this.tte=new TokenTraitExtractor(tt);
	}

	@Override
	public <T extends Collection<String>> T getFeatureStrings(PairInstance pi,Document d, T container) {
		if(Parallel.zero && tse.ts == TargetSpan.Anaphor) {
			container.add("XXX");
			return container;
		}
		Span s=tse.getSpan(pi);
		for(int i=s.start;i<=s.end;++i)
			container.add(tte.getTrait(s.s, i));
		return container;
	}

}
