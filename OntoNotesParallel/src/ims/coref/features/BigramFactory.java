package ims.coref.features;

public class BigramFactory {

	
	
	public static IFeature getBigram(IFeature f1,IFeature f2,int cutOff){
		if(f1 instanceof ISingleFeature){
			if(f2 instanceof ISingleFeature){
				return getBigram((ISingleFeature) f1,(ISingleFeature) f2,cutOff);
			} else if(f2 instanceof IMultiFeature){
				return getBigram((ISingleFeature) f1,(IMultiFeature) f2,cutOff);
			}
		} else if(f1 instanceof IMultiFeature){
			if(f2 instanceof ISingleFeature){
				return getBigram((ISingleFeature) f2,(IMultiFeature) f1,cutOff);
			} else if(f2 instanceof IMultiFeature){
				return getBigram((IMultiFeature) f1,(IMultiFeature) f2,cutOff);
			}
		}
		throw new RuntimeException("Failed to combine features "+f1.getName()+" and "+f2.getName());
	}
	
	public static IFeature getBigram(ISingleFeature f1,ISingleFeature f2,int cutOff){
		return new AbstractSingleBigramFeature(f1,f2,cutOff);
	}

	public static IFeature getBigram(ISingleFeature f1,IMultiFeature f2,int cutOff){
		return new AbstractSingleMultiBigramFeature(f1,f2,cutOff);
	}
	
	public static IFeature getBigram(IMultiFeature f1,IMultiFeature f2,int cutOff){
		return new AbstractMultiBigramFeature(f1,f2,cutOff);
	}
	
}
