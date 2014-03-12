package ims.coref.features;

import util.Common;
import ims.coref.Parallel;
import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.features.enums.Bool;
import ims.coref.features.extractors.TargetSpan;
import ims.coref.lang.Language;

public class F_CleverStringMatch extends AbstractEnumFeature<Bool>{
	private static final long serialVersionUID = 1L;
	
	protected F_CleverStringMatch() {
		super("CleverStringMatch",Bool.values());
	}

	@Override
	Bool getVal(PairInstance pi,Document d) {
		Bool b;
		if(Language.getLanguage(this.lang).cleverStringMatch(pi)){
			b = Bool.True;
		} else {
			b = Bool.False;
		}
		if(Parallel.zero) {
			return Bool.False;
		}
		return b;
	}

}
