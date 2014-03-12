package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;

public class Lone_n1 extends AbstractSingleDataDrivenFeature{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected Lone_n1(String name, int cutOff) {
		super("next1", cutOff);
		// TODO Auto-generated constructor stub
	}

	@Override
	String extractStringValue(PairInstance instance, Document d) {
		// TODO Auto-generated method stub
		Span ana = instance.ana;
		
		if(ana.end+1==ana.s.forms.length) {
			return "-";
		} else {
			return ana.s.forms[ana.end+1].toLowerCase(); 
		}
	}
}
