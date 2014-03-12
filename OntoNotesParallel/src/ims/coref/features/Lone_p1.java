package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;

public class Lone_p1 extends AbstractSingleDataDrivenFeature{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected Lone_p1(String name, int cutOff) {
		super("previous1", cutOff);
		// TODO Auto-generated constructor stub
	}

	@Override
	String extractStringValue(PairInstance instance, Document d) {
		// TODO Auto-generated method stub
		Span ana = instance.ana;
		if(ana.start==0) {
			return "_";
		} else {
			return ana.s.forms[ana.start-1].toLowerCase(); 
		}
	}
}
