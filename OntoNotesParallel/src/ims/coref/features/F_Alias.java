package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.features.enums.Bool;
import ims.coref.lang.Language;

public class F_Alias extends AbstractEnumFeature<Bool>{
	private static final long serialVersionUID = 1L;

	protected F_Alias() {
		super("Alias",Bool.values());
	}

	@Override
	Bool getVal(PairInstance pi,Document d) {
		if(Language.getLanguage(this.lang).isAlias(pi)){
			return Bool.True;
		} else {
			return Bool.False;
		}
	}

}
