package ims.coref.features;

public abstract class AbstractFeature implements IFeature {
	private static final long serialVersionUID = 1L;

	final String name;
	
	protected AbstractFeature(String name){
		this.name=name;
	}
	
	public String getName(){
		return name;
	}
	
	public String toString(){
		return name+" size: "+size();
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
