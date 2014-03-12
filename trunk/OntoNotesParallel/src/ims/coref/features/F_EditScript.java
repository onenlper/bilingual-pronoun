package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.util.EditDistance;

public class F_EditScript extends AbstractSingleDataDrivenFeature {
	private static final long serialVersionUID = 1L;
	
	private final AbstractSingleDataDrivenFeature asf1;
	private final AbstractSingleDataDrivenFeature asf2;
	private final boolean reverse;
	
	protected F_EditScript(AbstractSingleDataDrivenFeature asf1,AbstractSingleDataDrivenFeature asf2, int cutOff,boolean reverse) {
		super(asf1.getName()+asf2.getName()+(reverse?"Reverse":"")+"EditScript", cutOff);
		this.asf1=asf1;
		this.asf2=asf2;
		this.reverse=reverse;
	}

	@Override
	public String extractStringValue(PairInstance instance, Document d) {
		String s1=asf1.getStringValue(instance, d);
		String s2=asf2.getStringValue(instance, d);
		String editScript=EditDistance.editScript(reverse?(new StringBuilder(s1)).reverse().toString():s1, reverse?(new StringBuilder(s2)).reverse().toString():s2);		
		return editScript;
	}

}
