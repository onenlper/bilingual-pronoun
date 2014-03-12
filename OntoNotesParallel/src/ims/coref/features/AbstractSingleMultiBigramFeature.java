package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

public class AbstractSingleMultiBigramFeature extends AbstractMultiDataDrivenFeature{

	private final ISingleFeature f1;
	private final IMultiFeature f2;
	
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
	
	private static final long serialVersionUID = 1L;

	protected AbstractSingleMultiBigramFeature(ISingleFeature f1,IMultiFeature f2, int cutOff) {
		super(f1.getName()+"+"+f2.getName(), cutOff);
		this.f1=f1;
		this.f2=f2;
	}

	@Override
	public <T extends Collection<String>> T getFeatureStrings(PairInstance pi,Document d, T container) {
		String s1=f1.getStringValue(pi, d);
		SortedSet<String> ss=f2.getFeatureStrings(pi, d, new TreeSet<String>());
		for(String s:ss)
			container.add(s1+"++"+s);
		return container;
	}

	public boolean loneFea=false;
	
	@Override
	public void setLoneFea(boolean lone) {
		this.loneFea = lone;
	}

	@Override
	public boolean isLoneFea() {
		// TODO Auto-generated method stub
		return loneFea;
	}
}
