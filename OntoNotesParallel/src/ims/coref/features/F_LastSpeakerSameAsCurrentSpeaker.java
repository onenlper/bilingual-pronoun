package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.features.enums.Bool;
import ims.coref.features.extractors.TargetSpan;
import ims.coref.features.extractors.TargetSpanExtractor;


public class F_LastSpeakerSameAsCurrentSpeaker extends AbstractEnumFeature<Bool>{
	private static final long serialVersionUID = 3198064988935330562L;

	private final TargetSpanExtractor tse;
	
	protected F_LastSpeakerSameAsCurrentSpeaker(TargetSpan ts) {
		super(ts.toString()+"SpeakerSameAsLastSpeaker", Bool.values());
		this.tse=new TargetSpanExtractor(ts);
	}

	@Override
	Bool getVal(PairInstance pi, Document d) {
		Span s=tse.getSpan(pi);
		String spanSpeaker=s.s.speaker[s.hd];
		String lastSpeaker=s.s.lastSpeaker;
		if(spanSpeaker.equals("-") && (lastSpeaker==null || lastSpeaker.equals("-")))
			return null;
		if(spanSpeaker.equals(lastSpeaker))
			return Bool.True;
		else
			return Bool.False;
	}
}
