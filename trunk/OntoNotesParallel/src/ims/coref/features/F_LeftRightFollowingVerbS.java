package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.features.enums.LeftRight;
import ims.coref.features.extractors.TargetSpan;
import ims.coref.features.extractors.TargetSpanExtractor;

public class F_LeftRightFollowingVerbS extends AbstractSingleDataDrivenFeature {
	private static final long serialVersionUID = 1L;
	
	private final TargetSpanExtractor tse;
	private final LeftRight leftRight;
	private final boolean onlyOne;

	protected F_LeftRightFollowingVerbS(TargetSpan ts,LeftRight leftRight,boolean onlyOne, int cutOff) {
		super(ts.toString()+leftRight.toString()+"Verb"+(onlyOne?"OO":"S"), cutOff);
		tse=new TargetSpanExtractor(ts);
		this.leftRight=leftRight;
		this.onlyOne=onlyOne;
	}

	@Override
	String extractStringValue(PairInstance instance, Document d) {
		Span s=tse.getSpan(instance);
		switch(leftRight){
		case Left:	return leftVerbS(s);
		case Right: return rightVerbS(s);
		default: throw new Error("!");
		}
	}

	private String rightVerbS(Span s) {
		if(onlyOne){
			if(s.end+1<s.s.forms.length && s.s.tags[s.end+1].startsWith("V"))
				return s.s.forms[s.end+1];
			else
				return null;
		} else {
			if(!(s.end+1<s.s.forms.length && s.s.tags[s.end+1].startsWith("V")))
				return null;
			StringBuilder sb=new StringBuilder(s.s.forms[s.end+1]);
			for(int i=s.end+2;i<s.s.forms.length && s.s.tags[i].startsWith("V");++i)
				sb.append('_').append(s.s.forms[i]);
			return sb.toString();
		}
	}

	private String leftVerbS(Span s) {
		if(onlyOne){
			if(s.start-1>0 && s.s.tags[s.start-1].startsWith("V"))
				return s.s.forms[s.start-1];
			else
				return null;
		} else {
			if(!(s.start-1>0 && s.s.tags[s.start-1].startsWith("V")))
				return null;
			StringBuilder sb=new StringBuilder(s.s.forms[s.start-1]);
			for(int i=s.start-2;i>0 && s.s.tags[i].startsWith("V");--i)
				sb.append('_').append(s.s.forms[i]);
			return sb.toString();
		}
	}

}
