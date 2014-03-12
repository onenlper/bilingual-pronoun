package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.features.enums.Bool;
import ims.coref.features.extractors.TargetSpanExtractor;

public class F_Quoted extends AbstractEnumFeature<Bool>{
	private static final long serialVersionUID = 1L;

	private final TargetSpanExtractor tse;
	
	protected F_Quoted(TargetSpanExtractor tse) {
		super(tse.ts.toString()+"Quoted", Bool.values());
		this.tse=tse;
	}

	@Override
	Bool getVal(PairInstance pi, Document d) {
		Span s=tse.getSpan(pi);
		if(s.isQuoted)
//		if(s.s.quoteCount[s.hd]%2==1)
			return Bool.True;
		else
			return Bool.False;
	}

}
