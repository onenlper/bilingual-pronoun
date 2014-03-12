package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import de.bwaldvogel.liblinear.FeatureNode;

public abstract class AbstractMultiDataDrivenFeature extends AbstractDataDrivenFeature implements IMultiFeature {
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
	
	protected AbstractMultiDataDrivenFeature(String name,int cutOff) {
		super(name,cutOff);
	}

	@Override
	public void register(PairInstance instance, Document d) {
		for(String s:getFeatureStrings(instance,d,new ArrayList<String>()))
			registerString(s);
	}

	@Override
	public void contributeFeatureNodes(PairInstance instance, int offset, List<FeatureNode> sink, Document d) {
		SortedSet<String> set=getFeatureStrings(instance,d,new TreeSet<String>());
		for(String s:set){
			if(s==null)
				return;
			Integer index=m.get(s);
			if(index!=null){
				int i=index.intValue()+offset;
				FeatureNode fn=new FeatureNode(i,1.0);
				sink.add(fn);
			}
		}
	}

}
