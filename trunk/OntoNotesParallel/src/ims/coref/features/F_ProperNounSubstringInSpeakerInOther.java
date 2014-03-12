package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.features.enums.Bool;
import ims.coref.features.extractors.TargetSpan;
import ims.coref.features.extractors.TargetSpanExtractor;

public class F_ProperNounSubstringInSpeakerInOther extends AbstractEnumFeature<Bool>{
	private static final long serialVersionUID = 1L;

	private final TargetSpanExtractor tse;
	
	//TargetSpan refers to where the proper noun is supposed to be is
	protected F_ProperNounSubstringInSpeakerInOther(TargetSpan ts) {
		super(ts.toString()+"ContainsProperNounWithOtherSpeakerSubstringMatch", Bool.values());
		this.tse=new TargetSpanExtractor(ts);
	}

	@Override
	Bool getVal(PairInstance pi, Document d) {
		Span speakerSpan=tse.getOtherSpan(pi);
		String speaker=speakerSpan.s.speaker[speakerSpan.hd];
		if(speaker.equals("-"))
			return null;
		Span properNounSpan=tse.getSpan(pi);
		for(int i=properNounSpan.start;i<=properNounSpan.end;++i)
			if(properNounSpan.s.tags[i].startsWith("NNP") && speaker.contains(properNounSpan.s.forms[i]))
				return Bool.True;
		return Bool.False;
	}

}
