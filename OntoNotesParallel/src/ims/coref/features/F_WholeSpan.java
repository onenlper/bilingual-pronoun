package ims.coref.features;

import util.Common;
import ims.coref.Parallel;
import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.features.extractors.TargetSpan;
import ims.coref.features.extractors.TargetSpanExtractor;
import ims.coref.features.extractors.TokenTraitExtractor;

public class F_WholeSpan extends AbstractSingleDataDrivenFeature{
	private static final long serialVersionUID = 1L;

	private final TargetSpanExtractor tse;
	private final TokenTraitExtractor tte;
	
	protected F_WholeSpan(TargetSpanExtractor tse,TokenTraitExtractor tte, int cutOff) {
		super(tse.ts.toString()+"WholeSpan"+tte.tt.toString(), cutOff);
		this.tse=tse;
		this.tte=tte;
	}

	@Override
	public String extractStringValue(PairInstance instance,Document d) {
		if(Parallel.zero && this.tse.ts==TargetSpan.Anaphor) {
			return "XXX";
		}
		Span s=tse.getSpan(instance);
		StringBuilder sb=new StringBuilder();
		for(int i=s.start;i<=s.end;++i)
			sb.append(tte.getTrait(s.s, i)).append(" ");
		return sb.toString();
	}

}
