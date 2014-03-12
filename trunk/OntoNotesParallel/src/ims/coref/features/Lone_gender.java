package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;

public class Lone_gender extends AbstractSingleDataDrivenFeature{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected Lone_gender(String name, int cutOff) {
		super("gender", cutOff);
		// TODO Auto-generated constructor stub
	}

	@Override
	String extractStringValue(PairInstance instance, Document d) {
		// TODO Auto-generated method stub
		Span ana = instance.ana;
		return ana.gender.name();
	}
}
