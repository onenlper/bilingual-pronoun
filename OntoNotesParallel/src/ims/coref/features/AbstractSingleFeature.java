package ims.coref.features;

public abstract class AbstractSingleFeature extends AbstractFeature implements ISingleFeature {
	private static final long serialVersionUID = 1L;
	
	@Override
	public void setLang(String lang) {
		this.lang = lang;
	}
	
	@Override
	public String getLang() {
		return this.lang;
	}
	
	public String lang;
	
	protected AbstractSingleFeature(String name) {
		super(name);
	}
	
}
