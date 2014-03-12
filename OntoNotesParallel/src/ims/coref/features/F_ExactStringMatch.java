package ims.coref.features;

import ims.coref.Parallel;
import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.features.enums.Bool;

public class F_ExactStringMatch extends AbstractEnumFeature<Bool>{
	private static final long serialVersionUID = 1L;

	protected F_ExactStringMatch() {
		super("ExactStringMatch",Bool.values());
	}

	@Override
	Bool getVal(PairInstance pi,Document d) {
		if(exactStringMatch(pi.ant,pi.ana))
			return Bool.True;
		else
			return Bool.False;
	}
	
	public static boolean exactStringMatch(Span a,Span b){
		if(Parallel.zero) {
			return false;
		}
		if(a.size()!=b.size())
			return false;
		for(int i=0,size=a.size();i<size;++i){
			if(!a.s.forms[a.start+i].equals(b.s.forms[b.start+i]))
				return false;
		}
		return true;
	}

}
