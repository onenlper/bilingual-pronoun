package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.features.extractors.SpanTokenExtractor;
import ims.coref.features.extractors.TargetSpanExtractor;
import ims.coref.features.extractors.TokenTraitExtractor;

public class F_SubCat extends AbstractSingleDataDrivenFeature {
	private static final long serialVersionUID = 1L;

	private final TargetSpanExtractor tse;
	private final SpanTokenExtractor ste;
	private final TokenTraitExtractor tte;
	
	public F_SubCat(TargetSpanExtractor tse, SpanTokenExtractor ste,TokenTraitExtractor tte, int cutOff) {
		super(tse.ts.toString()+ste.st.toString()+tte.tt.toString()+"SubCat",cutOff);
		this.tse=tse;
		this.ste=ste;
		this.tte=tte;
	}

	@Override
	public String extractStringValue(PairInstance instance,Document d) {

		Span s=tse.getSpan(instance);
		int head=ste.getToken(s);
		if(head==-1 || head>=s.s.forms.length)
			return ">none<";
		StringBuilder sb=new StringBuilder();
		for(int i=1;i<s.s.forms.length;++i){
			if(s.s.dt.heads[i]==head)
				sb.append(tte.getTrait(s.s, i)).append(" ");
		}
		return sb.toString();
	}

}
