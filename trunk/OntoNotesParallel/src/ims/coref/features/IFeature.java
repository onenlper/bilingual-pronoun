package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import de.bwaldvogel.liblinear.FeatureNode;

public interface IFeature extends Serializable{
	public static final char UP='^';
	public static final char DOWN='_';

	void register(PairInstance instance,Document d);

	int size();

	void contributeFeatureNodes(PairInstance instance, int offset, List<FeatureNode> sink,Document d);

	void freeze();

	Map<String, Integer> getMap();
	
	String getName();
	
	public void setLang(String lang);
	
	public String getLang();
	
	public void setLoneFea(boolean loneFea);
	
	public boolean isLoneFea();
}
