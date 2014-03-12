package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;

public class Lone_xspan extends AbstractSingleDataDrivenFeature{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected Lone_xspan(String name, int cutOff) {
		super("xspan", cutOff);
		// TODO Auto-generated constructor stub
	}

	@Override
	String extractStringValue(PairInstance instance, Document d) {
		// TODO Auto-generated method stub
		Span xspan = instance.ana.getXSpan();
		if(xspan==null) {
			return "no";
		} else {
			return "yes";
		}
	}
}
