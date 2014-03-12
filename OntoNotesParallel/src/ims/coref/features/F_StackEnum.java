package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.features.enums.Stack;

public class F_StackEnum extends AbstractEnumFeature<Stack>{
	private static final long serialVersionUID = 1L;

	protected F_StackEnum() {
		super("StackEnum", Stack.values());
	}

	@Override
	Stack getVal(PairInstance pi, Document d) {
		Integer antI=d.stackMap.get(pi.ant);
		Integer anaI=d.stackMap.get(pi.ana);
		if(antI==null){
			if(anaI==null)
				return Stack.Neither;
			else
				return Stack.AnaOnly;
		} else if(anaI==null){
			return Stack.AntOnly;
		} else {
			if(antI.equals(anaI))
				return Stack.Coref;
			else
				return Stack.NonCoref;
		}
	}

}
