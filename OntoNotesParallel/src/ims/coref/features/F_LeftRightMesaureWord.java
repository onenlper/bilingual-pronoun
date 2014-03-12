package ims.coref.features;

import ims.coref.Parallel;
import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.features.enums.Bool;
import ims.coref.features.enums.LeftRight;
import ims.coref.features.extractors.TargetSpan;
import ims.coref.features.extractors.TargetSpanExtractor;

public class F_LeftRightMesaureWord extends AbstractSingleDataDrivenFeature {
	private static final long serialVersionUID = 1L;

	private static final String NONE="<none>";
	
	
	private final TargetSpanExtractor tse;
	private final LeftRight leftRight;
	
	protected F_LeftRightMesaureWord(TargetSpan ts,LeftRight leftRight, int cutOff) {
		super(ts.toString()+leftRight.toString()+"MeasureWord", cutOff);
		tse=new TargetSpanExtractor(ts);
		this.leftRight=leftRight;
	}

	@Override
	public String extractStringValue(PairInstance instance, Document d) {
		if(Parallel.zero ) {
			return "XXX";
		}
		
		Span s=tse.getSpan(instance);
		switch(leftRight){
		case Left:  return leftMeasureWord(s);
		case Right: return rightMeasureWord(s);
		default: throw new Error("!");
		}
	}

	private String leftMeasureWord(Span s) {
		for(int i=s.hd-1;i>=s.start;--i){
			if(s.s.tags[i].equals("M"))
				return s.s.forms[i];
		}
		return NONE;
	}

	private String rightMeasureWord(Span s) {
		for(int i=s.hd+1;i<=s.end;++i)
			if(s.s.tags[i].equals("M"))
				return s.s.forms[i];
		return NONE;
	}

}
