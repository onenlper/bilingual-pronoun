package ims.coref.features;

import java.util.Random;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.features.enums.Bool;

public class F_Rand extends AbstractEnumFeature<Bool>{
	private static final long serialVersionUID = -4628717198272273746L;

	private final Random rand=new Random(0xfff9af);
	
	protected F_Rand(){
		super("RAND", Bool.values());
	}

	@Override
	Bool getVal(PairInstance pi, Document d) {
		if(rand.nextBoolean())
			return Bool.True;
		else
			return Bool.False;
	}

}
