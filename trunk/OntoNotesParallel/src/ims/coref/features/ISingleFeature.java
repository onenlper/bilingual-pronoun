package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;

public interface ISingleFeature extends IFeature {

	String getStringValue(PairInstance instance,Document d);
	
}
