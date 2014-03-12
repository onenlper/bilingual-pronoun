package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;

public class F_XExist extends AbstractSingleDataDrivenFeature{
	private static final long serialVersionUID = 1L;
	
	protected F_XExist() {
		super("XExist", 0);
	}

	@Override
	public String extractStringValue(PairInstance instance,Document d) {
		Span ant = instance.ant;
		Span ana = instance.ana;
		return ant.xSpanType + "#" + ana.xSpanType;
	}

}
