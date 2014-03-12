package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;

public class F_Genre extends AbstractSingleDataDrivenFeature{
	private static final long serialVersionUID = 1L;
	
	protected F_Genre() {
		super("Genre", 0);
	}

	@Override
	public String extractStringValue(PairInstance instance,Document d) {
		return d.genre;
	}

}
