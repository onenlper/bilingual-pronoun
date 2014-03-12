package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.features.enums.Bool;

public class F_SameSpeaker extends AbstractEnumFeature<Bool>{
	private static final long serialVersionUID = 1L;

	protected F_SameSpeaker() {
		super("SameSpeaker", Bool.values());
	}

	@Override
	Bool getVal(PairInstance pi,Document d) {
		String antSpeaker=pi.ant.s.speaker[pi.ant.hd];
		String anaSpeaker=pi.ana.s.speaker[pi.ana.hd];
		if(antSpeaker.equals("-"))
			return null;
		if(antSpeaker.equals(anaSpeaker))
			return Bool.True;
		else
			return Bool.False;
	}

}
