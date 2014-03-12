package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.bwaldvogel.liblinear.FeatureNode;

public abstract class AbstractEnumFeature<T extends Enum<T>> extends AbstractSingleFeature {
	private static final long serialVersionUID = 1L;

	protected final T[] types;
	
	protected AbstractEnumFeature(String name,T[] types) {
		super(name);
		this.types=types;
	}
	

	@Override
	public void register(PairInstance instance,Document d) {
		//Do nothing
	}

	@Override
	public int size() {
		return types.length;
	}

	@Override
	public void contributeFeatureNodes(PairInstance instance, int offset, List<FeatureNode> sink,Document d) {
		T t=getVal(instance,d);
		if(t==null)
			return;
		int i=1+offset+t.ordinal();
		FeatureNode fn=new FeatureNode(i,1.0);
		sink.add(fn);
	}

	@Override
	public void freeze() {
		//Do nothing
	}

	@Override
	public String getStringValue(PairInstance instance,Document d) {
		T t=getVal(instance,d);
		if(t==null)
			return null;
		else
			return t.toString();
	}
	
	abstract T getVal(PairInstance pi,Document d);
	
	public Map<String, Integer> getMap(){
		HashMap<String,Integer> m=new HashMap<String,Integer>();
		int i=1;
		for(T t:types)
			m.put(t.toString(), i++);
		return m;
	}
}
