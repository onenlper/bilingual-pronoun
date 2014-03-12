package ims.coref.features;

import ims.coref.Parallel;
import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.features.enums.Gender;
import ims.coref.features.enums.Num;
import ims.coref.features.extractors.TargetSpan;
import ims.coref.features.extractors.TargetSpanExtractor;

public class F_Number extends AbstractEnumFeature<Num>{
	private static final long serialVersionUID = 1L;

	private final TargetSpanExtractor tse;
	
	protected F_Number(TargetSpanExtractor tse) {
		super(tse.ts.toString()+"Number",Num.values());
		this.tse=tse;
	}

	@Override
	Num getVal(PairInstance pi,Document d) {
		if(Parallel.zero && tse.ts==TargetSpan.Anaphor) {
			return Num.Unknown;
		}
		Span s=tse.getSpan(pi);
		return s.number;
	}

}
