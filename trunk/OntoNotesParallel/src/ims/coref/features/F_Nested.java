package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.features.enums.Bool;

public class F_Nested extends AbstractEnumFeature<Bool> {
	private static final long serialVersionUID = 1L;

	protected F_Nested() {
		super("Nested",Bool.values());
	}

	@Override
	Bool getVal(PairInstance pi, Document d) {
		if(areNested(pi.ant,pi.ana))
			return Bool.True;
		else
			return Bool.False;
	}
	
	public static boolean areNested(Span s1,Span s2){
		return s1.s==s2.s && ((s1.start<=s2.start && s1.end>=s2.end) || (s2.start<=s1.start && s2.end>=s1.end));
	}

}
