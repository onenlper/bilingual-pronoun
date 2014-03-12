package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;

public class Lone_genra extends AbstractSingleDataDrivenFeature{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected Lone_genra(String name, int cutOff) {
		super("genra", cutOff);
		// TODO Auto-generated constructor stub
	}

	@Override
	String extractStringValue(PairInstance instance, Document d) {
		// TODO Auto-generated method stub
		return instance.ana.getText();
	}
}
