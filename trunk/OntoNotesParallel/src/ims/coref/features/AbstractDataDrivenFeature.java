package ims.coref.features;

import ims.util.MutableInt;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractDataDrivenFeature extends AbstractFeature {
	private static final long serialVersionUID = 1L;

//	Map<String,MutableInt> counts=new TreeMap<String,MutableInt>();
	Map<String,MutableInt> counts=new HashMap<String,MutableInt>();
	Map<String,Integer> m;
	final int cutOff;
	
	protected AbstractDataDrivenFeature(String name,int cutOff) {
		super(name+(cutOff>1?"-"+cutOff:""));
		this.cutOff=cutOff;
	}

	@Override
	public int size() {
		return m.size();
	}

	@Override
	public void freeze() {
		if(m!=null || counts==null)
			throw new RuntimeException("Called freeze twice!");
		int index=1;
		String[] strings=counts.keySet().toArray(new String[counts.size()]);
		Arrays.sort(strings);
		m=new HashMap<String,Integer>();
		for(String s:strings){
			if(counts.get(s).getValue()>=cutOff)
				m.put(s, index++);
		}
		
		//Assumes ordered map.
//		for(Entry<String,MutableInt> e:counts.entrySet()){
//			if(e.getValue().getValue()>=cutOff){
//				m.put(e.getKey(), index);
//				++index;
//			}
//		}
		counts=null;
	}

	public Map<String, Integer> getMap(){
		return m;
	}

	protected void registerString(String s){
		if(s==null)
			return;
		MutableInt mi=counts.get(s);
		if(mi==null){
			counts.put(s,new MutableInt(1));
		} else {
			mi.increment();
		}
	}
}
