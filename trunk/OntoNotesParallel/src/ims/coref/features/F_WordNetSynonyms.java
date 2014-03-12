package ims.coref.features;

import ims.coref.Parallel;
import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.features.enums.Bool;
import ims.coref.util.WordNetInterface;

public class F_WordNetSynonyms extends AbstractEnumFeature<Bool>{
	private static final long serialVersionUID = -6750088950932574825L;

	protected F_WordNetSynonyms() {
		super("WordNetSynonyms", Bool.values());
	}

	@Override
	Bool getVal(PairInstance pi, Document d) {
		if(Parallel.zero) {
			return Bool.False;
		}
		WordNetInterface wni=WordNetInterface.theInstance();
		if(wni==null)
			return null;
		String antHead=pi.ant.s.forms[pi.ant.hd];
		String anaHead=pi.ana.s.forms[pi.ana.hd];
		if(wni.areSynonyms(antHead, anaHead))
			return Bool.True;
		else
			return Bool.False;
	}

}
