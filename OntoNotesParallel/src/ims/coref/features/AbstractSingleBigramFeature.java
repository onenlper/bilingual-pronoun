package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;

public class AbstractSingleBigramFeature extends AbstractSingleDataDrivenFeature{
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
	
	private final ISingleFeature f1;
	private final ISingleFeature f2;
	
	protected AbstractSingleBigramFeature(ISingleFeature f1,ISingleFeature f2, int cutOff) {
		super(f1.getName()+"+"+f2.getName(), cutOff);
		this.f1=f1;
		this.f2=f2;
	}

	@Override
	public String extractStringValue(PairInstance instance,Document d) {
		String s1=f1.getStringValue(instance,d);
		String s2=f2.getStringValue(instance,d);
		if(s1==null || s2==null)
			return null;
		return s1+"++"+s2;
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
