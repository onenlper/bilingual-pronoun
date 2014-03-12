package ims.coref.features;

import ims.coref.data.Document;

import ims.coref.data.PairInstance;

import java.util.Collection;

public interface IMultiFeature extends IFeature {
	
	<T extends Collection<String>> T getFeatureStrings(PairInstance pi,Document d,T container);
	
}
