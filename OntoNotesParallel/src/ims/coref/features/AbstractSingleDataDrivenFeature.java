package ims.coref.features;

import java.util.List;

import de.bwaldvogel.liblinear.FeatureNode;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;

public abstract class AbstractSingleDataDrivenFeature extends AbstractDataDrivenFeature implements ISingleFeature {
	private static final long serialVersionUID = 1L;
	
	@Override
	public void setLang(String lang) {
		// TODO Auto-generated method stub
		this.lang = lang;
	}
	
	@Override
	public String getLang() {
		return this.lang;
	}
	
	public String lang;
	
	protected AbstractSingleDataDrivenFeature(String name,int cutOff) {
		super(name,cutOff);
	}
	
	@Override
	public void register(PairInstance instance,Document d) {
		String s=getStringValue(instance,d);
		registerString(s);
	}

	@Override
	public void contributeFeatureNodes(PairInstance instance, int offset, List<FeatureNode> sink,Document d) {
		String s=getStringValue(instance,d);
		if(s==null)
			return;
		Integer index=m.get(s);
		if(index!=null){
			int i=index.intValue()+offset;
			FeatureNode fn=new FeatureNode(i,1.0);
			sink.add(fn);
		}
	}

	@Override
	public String getStringValue(PairInstance instance,Document d){
//		if(PairInstance.USE_FEATURE_CACHING){
//			String value=instance.getCachedFeature(name);
//			if(value==null){
//				value=extractStringValue(instance,d);
//				instance.addToCache(name, value);
//				return value;
//			} else {
//				return value;
//			}
//		} else
			return extractStringValue(instance,d);
	}
	
	abstract String extractStringValue(PairInstance instance,Document d);
	
}
